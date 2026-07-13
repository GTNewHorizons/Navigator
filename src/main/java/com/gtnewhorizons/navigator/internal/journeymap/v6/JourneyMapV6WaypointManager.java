package com.gtnewhorizons.navigator.internal.journeymap.v6;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.common.util.BlockPos;
import journeymap.api.v2.common.waypoint.WaypointFactory;

public final class JourneyMapV6WaypointManager extends WaypointManager {

    private journeymap.api.v2.common.waypoint.Waypoint journeyMapWaypoint;

    public JourneyMapV6WaypointManager(InteractableLayerManager layerManager) {
        super(layerManager, SupportedMods.JourneyMap);
    }

    @Override
    public void clearActiveWaypoint() {
        IClientAPI api = JourneyMapV6Plugin.getApi();
        if (api != null && journeyMapWaypoint != null) {
            api.removeWaypoint(Navigator.MODID, journeyMapWaypoint);
        }
        journeyMapWaypoint = null;
    }

    @Override
    public void updateActiveWaypoint(Waypoint waypoint) {
        if (matches(waypoint)) return;

        clearActiveWaypoint();
        IClientAPI api = JourneyMapV6Plugin.getApi();
        if (api == null) return;

        journeyMapWaypoint = WaypointFactory.createWaypoint(
            Navigator.MODID,
            new BlockPos(waypoint.blockX, waypoint.blockY, waypoint.blockZ),
            waypoint.label,
            waypoint.dimensionId,
            false);
        journeyMapWaypoint.setColor(waypoint.color);
        api.addWaypoint(Navigator.MODID, journeyMapWaypoint);
    }

    @Override
    public boolean hasWaypoint() {
        return journeyMapWaypoint != null;
    }

    private boolean matches(Waypoint waypoint) {
        return journeyMapWaypoint != null && waypoint.blockX == journeyMapWaypoint.getX()
            && waypoint.blockY == journeyMapWaypoint.getY()
            && waypoint.blockZ == journeyMapWaypoint.getZ()
            && journeyMapWaypoint.getDimensions()
                .contains(String.valueOf(waypoint.dimensionId))
            && waypoint.label.equals(journeyMapWaypoint.getName())
            && waypoint.color == journeyMapWaypoint.getColor();
    }
}
