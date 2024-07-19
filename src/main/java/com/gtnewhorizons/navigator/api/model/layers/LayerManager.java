package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

public abstract class LayerManager {

    private final ButtonManager buttonManager;

    protected boolean forceRefresh = false;
    private List<? extends ILocationProvider> visibleElements = new ArrayList<>();
    protected final Map<SupportedMods, LayerRenderer> layerRenderer = new EnumMap<>(SupportedMods.class);
    private int miniMapWidth = 0;
    private int miniMapHeight = 0;
    private int fullscreenMapWidth = 0;
    private int fullscreenMapHeight = 0;
    private SupportedMods openModGui;

    public LayerManager(ButtonManager buttonManager) {
        this.buttonManager = buttonManager;
        for (SupportedMods mod : SupportedMods.values()) {
            if (!mod.isEnabled()) continue;

            LayerRenderer renderer = addLayerRenderer(this, mod);
            if (renderer == null) continue;
            layerRenderer.put(mod, renderer);
        }
    }

    /**
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The {@link LayerRenderer} implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod);

    protected abstract List<? extends ILocationProvider> generateVisibleElements(int minBlockX, int minBlockZ,
        int maxBlockX, int maxBlockZ);

    public boolean isLayerActive() {
        return buttonManager.isActive();
    }

    public void activateLayer() {
        buttonManager.activate();
    }

    public void deactivateLayer() {
        buttonManager.deactivate();
    }

    public void toggleLayer() {
        buttonManager.toggle();
    }

    public void forceRefresh() {
        forceRefresh = true;
    }

    public final void onGuiOpened(SupportedMods mod) {
        openModGui = mod;
        onOpenMap();
    }

    public final void onGuiClosed(SupportedMods mod) {
        openModGui = SupportedMods.NONE;
        onCloseMap();
    }

    public void onOpenMap() {}

    public void onCloseMap() {}

    public final SupportedMods getOpenModGui() {
        return openModGui;
    }

    protected boolean needsRegenerateVisibleElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        return true;
    }

    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockRadius) {
        recacheMiniMap(centerBlockX, centerBlockZ, blockRadius, blockRadius);
    }

    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        miniMapWidth = blockWidth;
        miniMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    public void recacheFullscreenMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        fullscreenMapWidth = blockWidth;
        fullscreenMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    private void recacheVisibleElements(int centerBlockX, int centerBlockZ) {
        final int radiusBlockX = (Math.max(miniMapWidth, fullscreenMapWidth) + 1) >> 1;
        final int radiusBlockZ = (Math.max(miniMapHeight, fullscreenMapHeight) + 1) >> 1;

        final int minBlockX = centerBlockX - radiusBlockX;
        final int minBlockZ = centerBlockZ - radiusBlockZ;
        final int maxBlockX = centerBlockX + radiusBlockX;
        final int maxBlockZ = centerBlockZ + radiusBlockZ;

        checkAndUpdateElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ);
    }

    protected void checkAndUpdateElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        if (forceRefresh || needsRegenerateVisibleElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ)) {
            visibleElements = generateVisibleElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ);
            layerRenderer.values()
                .forEach(layer -> layer.updateVisibleElements(visibleElements));
            forceRefresh = false;
        }
    }

    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    public LayerRenderer getLayerRenderer(SupportedMods map) {
        return layerRenderer.get(map);
    }

    /**
     * Whether the layer is enabled for the corresponding mod.
     *
     * @param mod the mod checking if it is enabled
     * @return true if there is a layer implementation for the mod, false otherwise
     */
    public boolean isEnabled(SupportedMods mod) {
        return layerRenderer.containsKey(mod);
    }
}
