package com.gtnewhorizons.navigator.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gtnewhorizons.navigator.Utils;
import com.gtnewhorizons.navigator.api.journeymap.buttons.JMLayerButton;
import com.gtnewhorizons.navigator.api.journeymap.render.JMLayerRenderer;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.buttons.LayerButton;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
import com.gtnewhorizons.navigator.api.xaero.buttons.XaeroLayerButton;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.mixins.late.journeymap.FullscreenAccessor;

import journeymap.client.render.map.GridRenderer;

public class NavigatorApi {

    public static final List<ButtonManager> buttonManagers = new ArrayList<>();
    public static final List<LayerManager> layerManagers = new ArrayList<>();
    public static final List<LayerButton> layerButtons = new ArrayList<>();
    public static final List<LayerRenderer> layerRenderers = new ArrayList<>();
    public static final List<WaypointManager> waypointManagers = new ArrayList<>();

    // Register the logical button
    public static void registerSharedButtonManager(ButtonManager customManager) {
        buttonManagers.add(customManager);
    }

    // Register the logical layer
    public static void registerSharedLayerManager(LayerManager customLayer) {
        layerManagers.add(customLayer);
    }

    // Register visualization for logical button in JourneyMap
    public static void registerJourneyMapButton(JMLayerButton customButton) {
        if (Utils.isJourneyMapInstalled()) {
            layerButtons.add(customButton);
        }
    }

    // Add the JourneyMap renderer for a layer
    public static void registerJourneyMapRenderer(JMLayerRenderer customRenderer) {
        if (Utils.isJourneyMapInstalled()) {
            layerRenderers.add(customRenderer);
        }
    }

    public static void registerWaypointManager(WaypointManager waypointManager) {
        waypointManagers.add(waypointManager);
    }

    // Register visualization for logical button in Xaero's World Map
    public static void registerXaeroMapButton(XaeroLayerButton customButton) {
        if (Utils.isXaerosWorldMapInstalled()) {
            layerButtons.add(customButton);
        }
    }

    // Add the Xaero's World Map renderer for a layer
    public static void registerXaeroMapRenderer(XaeroLayerRenderer customRenderer) {
        if (Utils.isXaerosWorldMapInstalled()) {
            layerRenderers.add(customRenderer);
        }
    }

    public static LayerRenderer getActiveLayer() {
        return layerRenderers.stream()
            .filter(LayerRenderer::isLayerActive)
            .findFirst()
            .orElse(null);
    }

    public void openJourneyMapAt(int blockX, int blockZ, int zoom) {
        final GridRenderer gridRenderer = FullscreenAccessor.getGridRenderer();
        assert gridRenderer != null;
        gridRenderer.center(gridRenderer.getMapType(), blockX, blockZ, zoom);
    }

    public void openJourneyMapAt(int blockX, int blockZ) {
        final GridRenderer gridRenderer = FullscreenAccessor.getGridRenderer();
        this.openJourneyMapAt(blockX, blockZ, gridRenderer.getZoom());
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
