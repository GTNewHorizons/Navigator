package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

public abstract class InteractableLayerManager extends LayerManager {

    private List<? extends IWaypointAndLocationProvider> visibleElements = new ArrayList<>();
    private final Map<SupportedMods, WaypointManager> waypointManagers = new EnumMap<>(SupportedMods.class);

    protected Waypoint activeWaypoint = null;

    public InteractableLayerManager(ButtonManager buttonManager) {
        super(buttonManager);
        for (SupportedMods mod : SupportedMods.values()) {
            WaypointManager waypointManager = addWaypointManager(this, mod);
            if (waypointManager != null) {
                waypointManagers.put(mod, waypointManager);
            }
        }
    }

    /**
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The layer renderer implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(InteractableLayerManager manager, SupportedMods mod);

    /**
     * @param manager This layer manager
     * @param mod     The mod to add the waypoint manager for
     * @return The waypoint manager implementation for the mod or null if none
     */
    protected @Nullable WaypointManager addWaypointManager(InteractableLayerManager manager, SupportedMods mod) {
        return null;
    }

    @Nullable
    @Override
    protected final LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod) {
        return addLayerRenderer(this, mod);
    }

    public void setActiveWaypoint(Waypoint waypoint) {
        activeWaypoint = waypoint;
        visibleElements.forEach(element -> element.onWaypointUpdated(waypoint));
        waypointManagers.values()
            .forEach(translator -> translator.updateActiveWaypoint(waypoint));
    }

    public void clearActiveWaypoint() {
        activeWaypoint = null;
        visibleElements.forEach(IWaypointAndLocationProvider::onWaypointCleared);
        waypointManagers.values()
            .forEach(WaypointManager::clearActiveWaypoint);
    }

    public boolean hasActiveWaypoint() {
        return activeWaypoint != null;
    }

    public @Nullable WaypointManager getWaypointManager(SupportedMods map) {
        return waypointManagers.get(map);
    }

    protected abstract List<? extends IWaypointAndLocationProvider> generateVisibleElements(int minBlockX,
        int minBlockZ, int maxBlockX, int maxBlockZ);

    @Override
    protected void checkAndUpdateElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        if (forceRefresh || needsRegenerateVisibleElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ)) {
            visibleElements = generateVisibleElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ);

            if (hasActiveWaypoint()) {
                for (IWaypointAndLocationProvider element : visibleElements) {
                    element.onWaypointUpdated(activeWaypoint);
                }
            }

            layerRenderer.values()
                .forEach(layer -> layer.updateVisibleElements(visibleElements));
            forceRefresh = false;
        }
    }
}
