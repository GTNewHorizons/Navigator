package com.gtnewhorizons.navigator.api.journeymap.waypoints;

import java.awt.Color;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.internal.journeymap.v6.JourneyMapV6WaypointManager;

/**
 * JourneyMap waypoint bridge that uses the legacy model on JourneyMap 5 and delegates to the v2 API on JourneyMap 6.
 */
public class JMWaypointManager extends WaypointManager {

    private journeymap.client.model.Waypoint jmWaypoint;
    private final WaypointManager v6Delegate;

    public JMWaypointManager(InteractableLayerManager layerManager) {
        super(layerManager, SupportedMods.JourneyMap);
        v6Delegate = Util.isJourneyMapV6Installed() ? new JourneyMapV6WaypointManager(layerManager) : null;
    }

    @Override
    public void clearActiveWaypoint() {
        if (v6Delegate != null) {
            v6Delegate.clearActiveWaypoint();
            return;
        }
        jmWaypoint = null;
    }

    @Override
    public boolean hasWaypoint() {
        return v6Delegate != null ? v6Delegate.hasWaypoint() : jmWaypoint != null;
    }

    /** @return JourneyMap 5 waypoint, or {@code null}; not used for the JourneyMap 6 delegate */
    public journeymap.client.model.Waypoint getJmWaypoint() {
        return jmWaypoint;
    }

    @Override
    public void updateActiveWaypoint(Waypoint waypoint) {
        if (v6Delegate != null) {
            v6Delegate.updateActiveWaypoint(waypoint);
            return;
        }
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
