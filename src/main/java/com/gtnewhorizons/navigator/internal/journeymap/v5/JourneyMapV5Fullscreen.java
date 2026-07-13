package com.gtnewhorizons.navigator.internal.journeymap.v5;

import com.gtnewhorizons.navigator.mixins.late.journeymap.FullscreenAccessor;

import journeymap.client.render.map.GridRenderer;

public final class JourneyMapV5Fullscreen {

    private JourneyMapV5Fullscreen() {}

    public static void centerOn(int blockX, int blockZ, int zoom) {
        GridRenderer renderer = FullscreenAccessor.getGridRenderer();
        if (renderer == null) return;

        int targetZoom = zoom == -1 ? renderer.getZoom() : zoom;
        renderer.center(renderer.getMapType(), blockX, blockZ, targetZoom);
    }
}
