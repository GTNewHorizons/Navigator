package com.gtnewhorizons.navigator.api.model.layers;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

/**
 * Layer manager that supports clickable render steps and an optional active waypoint.
 * <p>
 * Plain {@link ILocationProvider} elements may be interactive. Waypoint synchronization is only applied to elements
 * that implement {@link IWaypointAndLocationProvider}.
 */
public abstract class InteractableLayerManager extends LayerManager {

    protected final Map<SupportedMods, WaypointManager> waypointManagers = new EnumMap<>(SupportedMods.class);

    protected Waypoint activeWaypoint = null;

    /**
     * @param buttonManager shared logical button controlling this layer
     */
    public InteractableLayerManager(ButtonManager buttonManager) {
        super(buttonManager);
        for (SupportedMods mod : SupportedMods.values()) {
            if (!mod.isEnabled()) continue;

            WaypointManager waypointManager = addWaypointManager(this, mod);
            if (waypointManager != null) {
                waypointManagers.put(mod, waypointManager);
            }
        }
    }

    /**
     * Creates an interactable or normal renderer for an installed integration.
     *
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The {@link LayerRenderer} implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(InteractableLayerManager manager, SupportedMods mod);

    /**
     * Optionally creates the bridge used to publish Navigator's active waypoint to a map mod.
     *
     * @param manager This layer manager
     * @param mod     The mod to add the waypoint manager for
     * @return The {@link WaypointManager} implementation for the mod or null if none
     */
    protected @Nullable WaypointManager addWaypointManager(InteractableLayerManager manager, SupportedMods mod) {
        return null;
    }

    /**
     * Updates a cached waypoint-capable location.
     * <p>
     * Plain locations bypass this overload. If information changes outside this method, call {@link #forceRefresh()}.
     *
     * @param location The location to update
     */
    public void updateElement(IWaypointAndLocationProvider location) {}

    @Nullable
    @Override
    protected final LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod) {
        return addLayerRenderer(this, mod);
    }

    /**
     * Sets the active waypoint, updates visible waypoint-capable locations, and synchronizes map waypoint managers.
     *
     * @param waypoint new active waypoint
     */
    public void setActiveWaypoint(Waypoint waypoint) {
        activeWaypoint = waypoint;
        getVisibleLocations().forEach(element -> {
            if (element instanceof IWaypointAndLocationProvider waypointLoc) {
                waypointLoc.onWaypointUpdated(waypoint);
            }
        });
        waypointManagers.values()
            .forEach(translator -> translator.updateActiveWaypoint(waypoint));
    }

    /** Clears the active waypoint and notifies visible waypoint-capable locations and map managers. */
    public void clearActiveWaypoint() {
        activeWaypoint = null;
        getVisibleLocations().forEach(element -> {
            if (element instanceof IWaypointAndLocationProvider waypointLoc) {
                waypointLoc.onWaypointCleared();
            }
        });
        waypointManagers.values()
            .forEach(WaypointManager::clearActiveWaypoint);
    }

    /** @return whether this manager currently has an active waypoint */
    public boolean hasActiveWaypoint() {
        return activeWaypoint != null;
    }

    /**
     * @param map map integration
     * @return its waypoint manager, or {@code null}
     */
    public @Nullable WaypointManager getWaypointManager(SupportedMods map) {
        return waypointManagers.get(map);
    }

    @Override
    public final void updateElement(ILocationProvider location) {
        if (location instanceof IWaypointAndLocationProvider waypointLoc) {
            if (hasActiveWaypoint()) {
                waypointLoc.onWaypointUpdated(activeWaypoint);
            } else if (waypointLoc.isActiveAsWaypoint()) {
                waypointLoc.onWaypointCleared();
            }
            updateElement(waypointLoc);
        }
    }

}
