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
import com.gtnewhorizons.navigator.internal.journeymap.JourneyMapIntegration;

/**
 * Client-side entry point for registering layers and querying the integrations Navigator currently exposes.
 * <p>
 * Register each {@link LayerManager} once during client initialization. Map mods are optional; a manager is only given
 * renderers for integrations that are both installed and enabled in Navigator's configuration.
 */
public final class NavigatorApi {

    /** Width of a Minecraft chunk in blocks. */
    public static final double CHUNK_WIDTH = 16;

    /** Shared action key used by interactable render steps. Defaults to {@code DELETE}. */
    public static final KeyBinding ACTION_KEY = new KeyBinding(
        "navigator.key.action",
        Keyboard.KEY_DELETE,
        Navigator.MODNAME);

    /** Registered layer managers. Consumers should use {@link #registerLayerManager(LayerManager)}. */
    public static final List<LayerManager> layerManagers = new ArrayList<>();

    /**
     * Registers a layer manager with every applicable map integration.
     * <p>
     * This method does not deduplicate registrations. Registration schedules the layer's initial cache and render
     * synchronization.
     *
     * @param layerManager The {@link LayerManager} to register.
     */
    public static void registerLayerManager(LayerManager layerManager) {
        layerManagers.add(layerManager);
        layerManager.forceRefresh();
    }

    /**
     * Returns active renderers for a map integration in registration order.
     *
     * @param mod map integration requesting renderers
     * @return a new list of renderers belonging to active layers
     */
    public static List<LayerRenderer> getActiveRenderersFor(SupportedMods mod) {
        return layerManagers.stream()
            .filter(layerManager -> layerManager.isEnabled(mod))
            .filter(LayerManager::isLayerActive)
            .map(layerManager -> layerManager.getLayerRenderer(mod))
            .collect(Collectors.toList());
    }

    /**
     * Returns active renderers sorted by ascending {@link LayerRenderer#getRenderPriority()}.
     *
     * @param mod map integration requesting renderers
     * @return a new sorted list
     */
    public static List<LayerRenderer> getActiveRenderersByPriority(SupportedMods mod) {
        List<LayerRenderer> list = getActiveRenderersFor(mod);
        list.sort(Comparator.comparingInt(LayerRenderer::getRenderPriority));
        return list;
    }

    /**
     * @param mod map integration to query
     * @return registered layers with a renderer for {@code mod}
     */
    public static List<LayerManager> getEnabledLayers(SupportedMods mod) {
        return layerManagers.stream()
            .filter(layerManager -> layerManager.isEnabled(mod))
            .collect(Collectors.toList());
    }

    /**
     * @param mod map integration to query
     * @return distinct buttons belonging to layers enabled for {@code mod}
     */
    public static List<ButtonManager> getEnabledButtons(SupportedMods mod) {
        return layerManagers.stream()
            .filter(layerManager -> layerManager.isEnabled(mod))
            .map(LayerManager::getButtonManager)
            .distinct()
            .collect(Collectors.toList());
    }

    /** @return every distinct registered button */
    public static List<ButtonManager> getDistinctButtons() {
        return getDistinctButtons(null);
    }

    /**
     * @param toExclude optional button to omit
     * @return every other distinct registered button
     */
    public static List<ButtonManager> getDistinctButtons(ButtonManager toExclude) {
        return layerManagers.stream()
            .map(LayerManager::getButtonManager)
            .distinct()
            .filter(buttonManager -> !buttonManager.equals(toExclude))
            .collect(Collectors.toList());
    }

    /** @return all registered managers that support interaction */
    public static List<InteractableLayerManager> getInteractableLayers() {
        return layerManagers.stream()
            .filter(layerManager -> layerManager instanceof InteractableLayerManager)
            .map(layerManager -> (InteractableLayerManager) layerManager)
            .collect(Collectors.toList());
    }

    /**
     * Opens JourneyMap centered on a block position and optionally activates a layer first.
     *
     * @param layer  layer to activate, or {@code null}
     * @param blockX world block X coordinate
     * @param blockZ world block Z coordinate
     * @param zoom   JourneyMap zoom, or {@code -1} to keep its current/default zoom
     */
    public void openJourneyMapAt(@Nullable LayerManager layer, int blockX, int blockZ, int zoom) {
        if (layer != null) layer.activateLayer();
        JourneyMapIntegration.centerOn(blockX, blockZ, zoom);
    }

    /**
     * Opens JourneyMap centered on a block position without changing its zoom.
     *
     * @param layer  layer to activate, or {@code null}
     * @param blockX world block X coordinate
     * @param blockZ world block Z coordinate
     */
    public void openJourneyMapAt(@Nullable LayerManager layer, int blockX, int blockZ) {
        this.openJourneyMapAt(layer, blockX, blockZ, -1);
    }
}
