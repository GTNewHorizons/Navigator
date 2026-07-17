package com.gtnewhorizons.navigator.api.journeymap;

import com.gtnewhorizons.navigator.Navigator;

import cpw.mods.fml.common.Loader;

/**
 * JourneyMap major version detection.
 * <p>
 * Done with class check because we need information early for mixins.
 */
public enum JourneyMapVersion {

    NONE,
    V5,
    V6;

    private static final JourneyMapVersion CURRENT = detect();

    /** @return detected JourneyMap version, or {@link #NONE} */
    public static JourneyMapVersion get() {
        return CURRENT;
    }

    private static JourneyMapVersion detect() {
        if (!Loader.isModLoaded("journeymap")) return NONE;
        ClassLoader cl = JourneyMapVersion.class.getClassLoader();
        if (cl.getResource("journeymap/api/v2/client/IClientAPI.class") != null) return V6;
        if (cl.getResource("journeymap/client/render/map/GridRenderer.class") != null) return V5;
        Navigator.LOG.error("JourneyMap is loaded but matches no known version.");
        return NONE;
    }
}
