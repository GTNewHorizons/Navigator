package com.gtnewhorizons.navigator.api.event;

import com.gtnewhorizons.navigator.api.model.layers.LayerManager;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * Published when a layer requests cache and render-output synchronization.
 * <p>
 * The event is posted on FML's common event bus. Consumers should call {@link LayerManager#forceRefresh()} instead of
 * posting it directly.
 */
public final class LayerRefreshEvent extends Event {

    private final LayerManager layerManager;

    public LayerRefreshEvent(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    /** @return layer whose cache and render output need synchronization */
    public LayerManager getLayerManager() {
        return layerManager;
    }
}
