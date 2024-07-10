package com.gtnewhorizons.navigator.config;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = "navigator")
public class GeneralConfig {

    @Config.Comment("Enable debug layers")
    @Config.DefaultBoolean(false)
    public static boolean enableDebugLayers;

}
