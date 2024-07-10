package com.gtnewhorizons.navigator.api.journeymap.waypoints;

import java.awt.Color;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

public class JMWaypointManager extends WaypointManager {

    private journeymap.client.model.Waypoint jmWaypoint;

    public JMWaypointManager(WaypointProviderManager layerManager) {
        super(layerManager, SupportedMods.JourneyMap);
    }

    @Override
    public void clearActiveWaypoint() {
        jmWaypoint = null;
    }

    @Override
    public boolean hasWaypoint() {
        return jmWaypoint != null;
    }

    public journeymap.client.model.Waypoint getJmWaypoint() {
        return jmWaypoint;
    }

    @Override
    public void updateActiveWaypoint(Waypoint waypoint) {
        if (!hasWaypoint() || waypoint.blockX != jmWaypoint.getX()
            || waypoint.blockY != jmWaypoint.getY()
            || waypoint.blockZ != jmWaypoint.getZ()
            || !jmWaypoint.getDimensions()
                .contains(waypoint.dimensionId)) {
            jmWaypoint = new journeymap.client.model.Waypoint(
                waypoint.label,
                waypoint.blockX,
                waypoint.blockY,
                waypoint.blockZ,
                new Color(waypoint.color),
                journeymap.client.model.Waypoint.Type.Normal,
                waypoint.dimensionId);
        }
    }
}
