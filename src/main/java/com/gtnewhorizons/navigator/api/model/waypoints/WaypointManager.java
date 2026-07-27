package com.gtnewhorizons.navigator.api.model.waypoints;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;

/** Bridges one interactable layer's active waypoint into a specific map mod. */
public abstract class WaypointManager {

    protected final InteractableLayerManager manager;
    protected final SupportedMods mod;

    public WaypointManager(InteractableLayerManager layerManager, SupportedMods mod) {
        this.manager = layerManager;
        this.mod = mod;
    }

    /** Clears the map-specific waypoint owned by this manager. */
    public abstract void clearActiveWaypoint();

    /** Creates or updates the map-specific waypoint from Navigator's value. */
    public abstract void updateActiveWaypoint(Waypoint waypoint);

    /** @return whether this manager currently owns a map-specific waypoint */
    public abstract boolean hasWaypoint();
}
