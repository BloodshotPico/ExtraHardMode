package me.ryanhamshire.ExtraHardMode.service;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.MockExtraHardMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiWorldConfig.class, JavaPlugin.class, PluginLogger.class})
public class TestMultiWorldConfig
{
    ExtraHardMode plugin = new MockExtraHardMode().get();

    //Because MultiworldConfig has a constructor and is an interface
    private class Mock extends MultiWorldConfig{
        public Mock (ExtraHardMode plugin){super(plugin);}
        @Override public void load (){}
        @Override public void starting (){}
        @Override public void closing (){}
    }

    MultiWorldConfig module = new Mock(plugin);

    /**
     * Test if normal retrieval of nodes which are present int the config is possible
     */
    @Test
    public void testLoadNodeValidInput()
    {
        FileConfiguration config = getBasicConfig();

        assertEquals    (false, module.loadNode(config, MockConfigNode.BOOL_TRUE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.BOOL_TRUE, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (false, module.loadNode(config, MockConfigNode.BOOL_FALSE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.BOOL_FALSE, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (4, module.loadNode(config, MockConfigNode.INT_0, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INT_0, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (9, module.loadNode(config, MockConfigNode.INT_9, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.INT_0, false).getStatusCode() == MultiWorldConfig.Status.OK);

        assertEquals    (MultiWorldConfig.Mode.INHERIT.name(), (String) module.loadNode(config, MockConfigNode.STR_0, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.STR_0, false).getStatusCode() == MultiWorldConfig.Status.INHERITS);
    }

    /**
     * Make sure that when a node is not found in the config that we get the default value back (not null).
     * and that the Status is NOT_FOUND
     */
    @Test
    public void testLoadNodeNotFound()
    {
        FileConfiguration config = getBasicConfig();

        assertEquals    (MockConfigNode.NOTFOUND_BOOL.getDefaultValue(), module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (MockConfigNode.NOTFOUND_DOUBLE.getDefaultValue(), module.loadNode(config, MockConfigNode.NOTFOUND_DOUBLE, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_DOUBLE, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (MockConfigNode.NOTFOUND_INT.getDefaultValue(), module.loadNode(config, MockConfigNode.NOTFOUND_INT, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_INT, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (MockConfigNode.NOTFOUND_STR.getDefaultValue(), module.loadNode(config, MockConfigNode.NOTFOUND_STR, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_STR, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);

        assertEquals    (MockConfigNode.NOTFOUND_LIST.getDefaultValue(), module.loadNode(config, MockConfigNode.NOTFOUND_LIST, false).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_LIST, false).getStatusCode() == MultiWorldConfig.Status.NOT_FOUND);



        assertEquals    (true, module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, true).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_BOOL, true).getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        assertEquals    (1, module.loadNode(config, MockConfigNode.NOTFOUND_INT, true).getContent());
        assertTrue      (module.loadNode(config, MockConfigNode.NOTFOUND_INT, true).getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

    }

    /**
     * Make sure that the Status returned is Status.INHERITS for all nodes with "inherit" as value
     * and that "inherit" is the value returned (lowercase)
     */
    @Test
    public void testLoadNodeInherited()
    {
        FileConfiguration config = getBasicConfig();
        String inherit = MultiWorldConfig.Mode.INHERIT.name().toLowerCase();
        MultiWorldConfig.Response response;

        response = module.loadNode(config, MockConfigNode.INHERITS_BOOL, false);
        assertEquals    (inherit, response.getContent());
        assertTrue      (response.getStatusCode() == MultiWorldConfig.Status.INHERITS);

        response = module.loadNode(config, MockConfigNode.INHERITS_INT, false);
        assertEquals    (inherit, response.getContent());
        assertTrue      (response.getStatusCode() == MultiWorldConfig.Status.INHERITS);

        response = module.loadNode(config, MockConfigNode.INHERITS_DOUBLE, false);
        assertEquals    (inherit, response.getContent());
        assertTrue      (response.getStatusCode() == MultiWorldConfig.Status.INHERITS);

        response = module.loadNode(config, MockConfigNode.INHERITS_STR, false);
        assertEquals    (MultiWorldConfig.Mode.INHERIT.name(), response.getContent()); //only for strings the expected output is the same as the input
        assertTrue      (response.getStatusCode() == MultiWorldConfig.Status.INHERITS);

        response = module.loadNode(config, MockConfigNode.INHERITS_LIST, false);
        assertEquals    (inherit, response.getContent());
        assertTrue      (response.getStatusCode() == MultiWorldConfig.Status.INHERITS);
    }

    /**
     * Throw everything at the method
     */
    @Test
    public void testValPercent()
    {
        MultiWorldConfig.Response response;

        response = module.validateInt(MockConfigNode.INT_PERC_1, -123);
        assertEquals (0, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_PERC_1, 42);
        assertEquals (42, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);

        response = module.validateInt(MockConfigNode.INT_PERC_1, 0);
        assertEquals (0, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);

        response = module.validateInt(MockConfigNode.INT_PERC_1, 100);
        assertEquals (100, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);

        response = module.validateInt(MockConfigNode.INT_PERC_1, 101);
        assertEquals (100, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_PERC_1, 1032);
        assertEquals (100, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);
    }

    /**
     * Test the customBounds method through the health subtype
     * Allowed values are 1-20
     */
    @Test
    public void testCustomBounds()
    {
        MultiWorldConfig.Response response;

        response = module.validateInt(MockConfigNode.INT_HP_1, -123);
        assertEquals (1, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_HP_1, 0);
        assertEquals (1, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_HP_1, 23);
        assertEquals (20, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_HP_1, 12);
        assertEquals (12, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);
    }

    /**
     * Valid numbers are only positive including 0
     */
    @Test
    public void testValNaturalNumbers()
    {
        MultiWorldConfig.Response response;

        response = module.validateInt(MockConfigNode.INT_NN_1, -123);
        assertEquals (0, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_NN_1, -1);
        assertEquals (0, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.ADJUSTED);

        response = module.validateInt(MockConfigNode.INT_NN_1, 123);
        assertEquals (123, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);

        response = module.validateInt(MockConfigNode.INT_NN_1, 42);
        assertEquals (42, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);

        response = module.validateInt(MockConfigNode.INT_NN_1, 1);
        assertEquals (1, response.getContent());
        assertTrue(response.getStatusCode() == MultiWorldConfig.Status.OK);
    }

    /**
     * Get an example config:
     * <pre>
     * BOOL_TRUE = false
     * BOOL_FALSE = false
     * INT_0 = 4
     * INT_9 = 9
     * STR_0 = inherit
     *
     * NOTFOUND_X = all not set in the config
     * INHERITS_X = all "inherit"
     * </pre>
     * @return a config
     */
    public FileConfiguration getBasicConfig ()
    {
        FileConfiguration configuration = new YamlConfiguration();

        //normal values
        configuration.set (MockConfigNode.BOOL_TRUE.getPath(), false);
        configuration.set (MockConfigNode.BOOL_FALSE.getPath(), false);
        configuration.set (MockConfigNode.INT_0.getPath(), 4);
        configuration.set (MockConfigNode.INT_9.getPath(), 9);
        configuration.set (MockConfigNode.STR_0.getPath(), MultiWorldConfig.Mode.INHERIT.name());

        //inherited values
        configuration.set (MockConfigNode.INHERITS_BOOL.getPath(),  MultiWorldConfig.Mode.INHERIT.name());
        configuration.set (MockConfigNode.INHERITS_INT.getPath(),   MultiWorldConfig.Mode.INHERIT.name());
        configuration.set (MockConfigNode.INHERITS_DOUBLE.getPath(),MultiWorldConfig.Mode.INHERIT.name().toLowerCase());//just to test if that also works
        configuration.set (MockConfigNode.INHERITS_STR.getPath(),   MultiWorldConfig.Mode.INHERIT.name());
        configuration.set (MockConfigNode.INHERITS_LIST.getPath(),  MultiWorldConfig.Mode.INHERIT.name().toLowerCase());

        return configuration;
    }

}