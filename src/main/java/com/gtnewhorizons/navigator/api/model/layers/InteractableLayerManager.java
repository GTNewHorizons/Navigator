package com.gtnewhorizons.navigator.api.model.layers;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

public abstract class InteractableLayerManager extends LayerManager {

    protected final Map<SupportedMods, WaypointManager> waypointManagers = new EnumMap<>(SupportedMods.class);

    protected Waypoint activeWaypoint = null;

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
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The {@link LayerRenderer} implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(InteractableLayerManager manager, SupportedMods mod);

    /**
     * @param manager This layer manager
     * @param mod     The mod to add the waypoint manager for
     * @return The {@link WaypointManager} implementation for the mod or null if none
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
        getVisibleLocations().forEach(element -> element.onWaypointUpdated(waypoint));
        waypointManagers.values()
            .forEach(translator -> translator.updateActiveWaypoint(waypoint));
    }

    public void clearActiveWaypoint() {
        activeWaypoint = null;
        getVisibleLocations().forEach(IWaypointAndLocationProvider::onWaypointCleared);
        waypointManagers.values()
            .forEach(WaypointManager::clearActiveWaypoint);
    }

    public boolean hasActiveWaypoint() {
        return activeWaypoint != null;
    }

    public @Nullable WaypointManager getWaypointManager(SupportedMods map) {
        return waypointManagers.get(map);
    }

    @Override
    public final void updateElement(ILocationProvider location) {
        if (location instanceof IWaypointAndLocationProvider waypointLoc) {
            if (hasActiveWaypoint()) {
                waypointLoc.onWaypointUpdated(activeWaypoint);
            }
            updateElement(waypointLoc);
        }
    }

    public void updateElement(IWaypointAndLocationProvider location) {}

    @Override
    @SuppressWarnings("unchecked")
    public Collection<IWaypointAndLocationProvider> getVisibleLocations() {
        return (Collection<IWaypointAndLocationProvider>) super.getVisibleLocations();
    }
}
