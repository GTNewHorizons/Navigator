package com.gtnewhorizons.navigator.config;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = "navigator", category = "modules")
public class ModuleConfig {

    @Config.Comment("Enable JourneyMap module")
    @Config.DefaultBoolean(true)
    public static boolean enableJourneyMapModule;

    @Config.Comment("Enable Xaero's World Map module")
    @Config.DefaultBoolean(true)
    public static boolean enableXaeroWorldMapModule;

    @Config.Comment("Enable Xaero's Minimap module")
    @Config.DefaultBoolean(true)
    public static boolean enableXaeroMinimapModule;
}
