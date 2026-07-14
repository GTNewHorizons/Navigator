package com.gtnewhorizons.navigator.api.model;

import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.config.ModuleConfig;

/** Map integrations supported by Navigator and their process-lifetime availability. */
public enum SupportedMods {

    JourneyMap(Util.isJourneyMapInstalled() && ModuleConfig.enableJourneyMapModule),
    XaeroWorldMap(Util.isXaerosWorldMapInstalled() && ModuleConfig.enableXaeroWorldMapModule),
    XaeroMiniMap(Util.isXaerosMinimapInstalled() && ModuleConfig.enableXaeroMinimapModule),
    NONE(false);

    private final boolean enabled;

    SupportedMods(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether the corresponding mod is installed and its Navigator module is enabled.
     *
     * @return integration availability cached during class initialization
     */
    public boolean isEnabled() {
        return enabled;
    }
}
