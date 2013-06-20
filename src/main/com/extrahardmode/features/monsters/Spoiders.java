/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.features.monsters;

import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.config.RootConfig;
import com.extrahardmode.config.RootNode;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.task.WebCleanupTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Changes to Spoiders include:
 *
 * More spiders in caves
 */
public class Spoiders implements Listener
{
    private final ExtraHardMode plugin;
    private final RootConfig CFG;

    public Spoiders(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
    }

    /**
     * When a creature spawns
     *
     * More spiders in caves
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();
        EntityType entityType = entity.getType();

        final int spiderBonusSpawnPercent = CFG.getInt(RootNode.BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT, world.getName());

        // FEATURE: more spiders underground
        if (entityType == EntityType.ZOMBIE && world.getEnvironment() == World.Environment.NORMAL && location.getBlockY() < world.getSeaLevel() - 5)
        {
            if (plugin.random(spiderBonusSpawnPercent))
            {
                event.setCancelled(true);
                entityType = EntityType.SPIDER;
                EntityHelper.spawn(location, entityType);
            }
        }
    }

    /**
     * When an Entity dies
     *
     * spiders drop web on death
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean spidersDropWebOnDeath= CFG.getBoolean(RootNode.SPIDERS_DROP_WEB_ON_DEATH, world.getName());

        //TODO EhmSpiderDropWebEvent
        // FEATURE: spiders drop web on death
        if (spidersDropWebOnDeath)
        {
            //Reduce amount of web dropped by spiders which die in caves from environmental damage
            if (entity instanceof Spider && (!EntityHelper.isLootLess(entity) || plugin.getRandom().nextInt(3) == 1))
            {
                // random web placement
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 9;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 9;

                Location[] locations = new Location[4];

                locations[0] = entity.getLocation().add(random1, 0, random2);
                locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
                locations[2] = entity.getLocation().add(-random1 / 2, 0, -random2);
                locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

                List<Block> changedBlocks = new ArrayList<Block>();
                for (Location location : locations)
                {
                    Block block = location.getBlock();

                    // don't replace anything solid with web
                    if (block.getType() != Material.AIR)
                        continue;

                    // only place web on the ground, not hanging up in the air
                    for (int i = 0; i < 5 || block.getY() < 0; i++)
                    {
                        if (block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
                            block = block.getRelative(BlockFace.DOWN);
                    }

                    // only place web if Block is empty
                    if (block.getRelative(BlockFace.DOWN).getType() != Material.AIR)
                    {
                        // don't place next to cactus, because it will break the
                        // cactus
                        Block[] adjacentBlocks = new Block[]{block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                                block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH)};

                        boolean nextToCactus = false;
                        for (Block adjacentBlock : adjacentBlocks)
                        {
                            if (adjacentBlock.getType() == Material.CACTUS)
                            {
                                nextToCactus = true;
                                break;
                            }
                        }

                        if (!nextToCactus)
                        {
                            block.setType(Material.WEB);
                            changedBlocks.add(block);
                        }
                    }
                }

                // any webs placed above sea level will be automatically cleaned up
                // after a short time
                if (entity.getLocation().getBlockY() >= entity.getLocation().getWorld().getSeaLevel() - 5)
                {
                    WebCleanupTask task = new WebCleanupTask(changedBlocks);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * 30);
                }
            }
        }
    }
}
