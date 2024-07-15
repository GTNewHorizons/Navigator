package com.gtnewhorizons.navigator.api.model.waypoints;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;

public abstract class WaypointManager {

    protected final InteractableLayerManager manager;
    protected final SupportedMods mod;

    public WaypointManager(InteractableLayerManager layerManager, SupportedMods mod) {
        this.manager = layerManager;
        this.mod = mod;
    }

    public abstract void clearActiveWaypoint();

    public abstract void updateActiveWaypoint(Waypoint waypoint);

    public abstract boolean hasWaypoint();
}
