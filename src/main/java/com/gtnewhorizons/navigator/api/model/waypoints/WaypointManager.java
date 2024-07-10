package com.gtnewhorizons.navigator.api.model.waypoints;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;

public abstract class WaypointManager {

    public WaypointManager(WaypointProviderManager layerManager, SupportedMods map) {
        layerManager.registerWaypointManager(map, this);
    }

    public abstract void clearActiveWaypoint();

    public abstract void updateActiveWaypoint(Waypoint waypoint);

    public abstract boolean hasWaypoint();
}
