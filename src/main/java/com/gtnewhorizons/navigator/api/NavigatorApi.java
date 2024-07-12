package com.gtnewhorizons.navigator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.journeymap.buttons.JMLayerButton;
import com.gtnewhorizons.navigator.api.journeymap.render.JMLayerRenderer;
import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.buttons.LayerButton;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.api.xaero.buttons.XaeroLayerButton;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.waypoints.XaeroWaypointManager;
import com.gtnewhorizons.navigator.mixins.late.journeymap.FullscreenAccessor;

import journeymap.client.render.map.GridRenderer;

public class NavigatorApi {

    public static final double CHUNK_WIDTH = 16;

    public static final List<ButtonManager> buttonManagers = new ArrayList<>();
    public static final List<LayerManager> layerManagers = new ArrayList<>();
    public static final List<LayerButton> layerButtons = new ArrayList<>();
    public static final List<LayerRenderer> layerRenderers = new ArrayList<>();
    public static final List<WaypointManager> waypointManagers = new ArrayList<>();

    /**
     * @param buttonManager The {@link ButtonManager} to register
     *                      Only one needs to be registered per layer regardless of how many mods are supported
     */
    public static void registerButtonManager(ButtonManager buttonManager) {
        buttonManagers.add(buttonManager);
    }

    /**
     * @param layerManager The {@link LayerManager} to register.
     *                     Only one needs to be registered per layer regardless of how many mods are supported
     */
    public static void registerLayerManager(LayerManager layerManager) {
        layerManagers.add(layerManager);
    }

    /**
     * @param layerButton The LayerButton to register.
     *                    <p>
     *                    Should be an instance of {@link JMLayerButton} or {@link XaeroLayerButton}
     *                    Both mods can be registered at the same time and will be handled accordingly
     */
    public static void registerLayerButton(LayerButton layerButton) {
        layerButtons.add(layerButton);
    }

    /**
     * @param layerRenderer The LayerRenderer to register.
     *                      <p>
     *                      Should be an instance of {@link JMLayerRenderer} or {@link XaeroLayerRenderer}
     *                      Both mods can be registered at the same time and will be handled accordingly
     */
    public static void registerLayerRenderer(LayerRenderer layerRenderer) {
        layerRenderers.add(layerRenderer);
    }

    /**
     * @param waypointManager The {@link WaypointManager} to register.
     *                        <p>
     *                        Should be an instance of {@link JMWaypointManager} or {@link XaeroWaypointManager}
     *                        Both mods can be registered at the same time and will be handled accordingly
     */
    public static void registerWaypointManager(WaypointManager waypointManager) {
        waypointManagers.add(waypointManager);
    }

    public static @Nullable LayerRenderer getActiveLayer() {
        return layerRenderers.stream()
            .filter(LayerRenderer::isLayerActive)
            .findFirst()
            .orElse(null);
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

    public static List<JMLayerRenderer> getJourneyMapLayerRenderers() {
        return layerRenderers.stream()
            .filter(JMLayerRenderer.class::isInstance)
            .map(JMLayerRenderer.class::cast)
            .collect(Collectors.toList());
    }

    public static List<XaeroLayerRenderer> getXaeroLayerRenderers() {
        return layerRenderers.stream()
            .filter(XaeroLayerRenderer.class::isInstance)
            .map(XaeroLayerRenderer.class::cast)
            .collect(Collectors.toList());
    }

    public static List<XaeroLayerButton> getXaeroButtons() {
        return layerButtons.stream()
            .filter(XaeroLayerButton.class::isInstance)
            .map(XaeroLayerButton.class::cast)
            .collect(Collectors.toList());
    }
}
