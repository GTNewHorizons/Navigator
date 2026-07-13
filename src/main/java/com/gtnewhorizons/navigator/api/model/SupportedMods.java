package com.gtnewhorizons.navigator.api.model;

import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.config.ModuleConfig;

public enum SupportedMods {

    JourneyMap(Util.isJourneyMapInstalled() && ModuleConfig.enableJourneyMapModule),
    XaeroWorldMap(Util.isXaerosWorldMapInstalled() && ModuleConfig.enableXaeroWorldMapModule),
    XaeroMiniMap(Util.isXaerosMinimapInstalled() && ModuleConfig.enableXaeroMinimapModule),
    NONE(false);

    private final boolean enabled;

    SupportedMods(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
