package com.gtnewhorizons.navigator.api.model;

import com.gtnewhorizons.navigator.Utils;

public enum SupportedMods {

    JourneyMap(Utils.isJourneyMapInstalled()),
    XaeroWorldMap(Utils.isXaerosWorldMapInstalled()),
    XaeroMiniMap(Utils.isXaerosMinimapInstalled());

    // TODO: Open Map at here

    private final boolean enabled;

    SupportedMods(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
