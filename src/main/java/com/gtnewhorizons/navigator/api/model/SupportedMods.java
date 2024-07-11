package com.gtnewhorizons.navigator.api.model;

import com.gtnewhorizons.navigator.api.util.Util;

public enum SupportedMods {

    JourneyMap(Util.isJourneyMapInstalled()),
    XaeroWorldMap(Util.isXaerosWorldMapInstalled()),
    XaeroMiniMap(Util.isXaerosMinimapInstalled());

    private final boolean enabled;

    SupportedMods(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
