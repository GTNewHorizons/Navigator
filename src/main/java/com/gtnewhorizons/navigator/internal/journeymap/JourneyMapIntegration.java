package com.gtnewhorizons.navigator.internal.journeymap;

import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.internal.journeymap.v5.JourneyMapV5Fullscreen;
import com.gtnewhorizons.navigator.internal.journeymap.v6.JourneyMapV6Plugin;

public final class JourneyMapIntegration {

    private JourneyMapIntegration() {}

    public static void centerOn(int blockX, int blockZ, int zoom) {
        if (Util.isJourneyMapV6Installed()) {
            JourneyMapV6Plugin.centerOn(blockX, blockZ);
        } else if (Util.isJourneyMapV5Installed()) {
            JourneyMapV5Fullscreen.centerOn(blockX, blockZ, zoom);
        }
    }
}
