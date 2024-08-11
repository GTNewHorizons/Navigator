package com.gtnewhorizons.navigator.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.mixins.late.journeymap.FullscreenAccessor;

import journeymap.client.render.map.GridRenderer;

public final class NavigatorApi {

    public static final double CHUNK_WIDTH = 16;
    public static final KeyBinding ACTION_KEY = new KeyBinding(
        "navigator.key.action",
        Keyboard.KEY_DELETE,
        Navigator.MODNAME);

    public static final List<LayerManager> layerManagers = new ArrayList<>();

    /**
     * @param layerManager The {@link LayerManager} to register.
     */
    public static void registerLayerManager(LayerManager layerManager) {
        layerManagers.add(layerManager);
    }

    public static List<LayerRenderer> getActiveRenderersFor(SupportedMods mod) {
        return layerManagers.stream()
            .filter(LayerManager::isLayerActive)
            .map(layerManager -> layerManager.getLayerRenderer(mod))
            .collect(Collectors.toList());
    }

    public static List<LayerRenderer> getActiveRenderersByPriority(SupportedMods mod) {
        List<LayerRenderer> list = getActiveRenderersFor(mod);
        list.sort(Comparator.comparingInt(LayerRenderer::getRenderPriority));
        return list;
    }

    public static List<LayerManager> getEnabledLayers(SupportedMods mod) {
        return layerManagers.stream()
            .filter(layerManager -> layerManager.isEnabled(mod))
            .collect(Collectors.toList());
    }

    public static List<ButtonManager> getEnabledButtons(SupportedMods mod) {
        return layerManagers.stream()
            .filter(layerManager -> layerManager.isEnabled(mod))
            .map(LayerManager::getButtonManager)
            .distinct()
            .collect(Collectors.toList());
    }

    public static List<ButtonManager> getDistinctButtons() {
        return layerManagers.stream()
            .map(LayerManager::getButtonManager)
            .distinct()
            .collect(Collectors.toList());
    }

    public static List<InteractableLayerManager> getInteractableLayers() {
        return layerManagers.stream()
            .filter(layerManager -> layerManager instanceof InteractableLayerManager)
            .map(layerManager -> (InteractableLayerManager) layerManager)
            .collect(Collectors.toList());
    }

    public void openJourneyMapAt(@Nullable LayerManager layer, int blockX, int blockZ, int zoom) {
        if (!Util.isJourneyMapInstalled()) return;
        final GridRenderer gridRenderer = FullscreenAccessor.getGridRenderer();
        if (gridRenderer == null) return;

        if (layer != null) layer.activateLayer();
        if (zoom == -1) zoom = gridRenderer.getZoom();
        gridRenderer.center(gridRenderer.getMapType(), blockX, blockZ, zoom);
    }

    public void openJourneyMapAt(@Nullable LayerManager layer, int blockX, int blockZ) {
        this.openJourneyMapAt(layer, blockX, blockZ, -1);
    }
}
