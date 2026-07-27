package com.gtnewhorizons.navigator.config;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = "navigator")
public class GeneralConfig {

    @Config.Comment("Enable debug layers")
    @Config.DefaultBoolean(false)
    public static boolean enableDebugLayers;

    @Config.Comment("Keep the fullscreen map search text when reopening the map")
    @Config.DefaultBoolean(true)
    public static boolean rememberSearchText;

}
