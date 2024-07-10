package com.gtnewhorizons.navigator;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Config {

    public static final String CAT_GENERAL = "general";
    public static Configuration config;

    public static boolean enableDebugLayers;
    public static boolean enableJourneyMapModule;
    public static boolean enableXaeroWorldMapModule;
    public static boolean enableXaeroMinimapModule;

    static void init(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        syncConfig();
        FMLCommonHandler.instance()
            .bus()
            .register(new Config());
    }

    private static void syncConfig() {

        enableJourneyMapModule = config
            .getBoolean("enableJourneyMapModule", CAT_GENERAL, true, "Enable JourneyMap module");
        enableXaeroWorldMapModule = config
            .getBoolean("enableXaeroWorldMapModule", CAT_GENERAL, true, "Enable Xaero's World Map module");
        enableXaeroMinimapModule = config
            .getBoolean("enableXaeroMinimapModule", CAT_GENERAL, true, "Enable Xaero's Minimap module");

        // TODO: remember to change default to false plz
        enableDebugLayers = config.getBoolean("enableDebugLayers", CAT_GENERAL, true, "Enable debug layers");
        config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(Navigator.MODID)) {
            syncConfig();
        }
    }
}
