package com.gtnewhorizons.navigator.api.xaero.waypoints;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

public class XaeroWaypointManager extends WaypointManager {

    private WaypointWithDimension xWaypoint;

    public XaeroWaypointManager(WaypointProviderManager layerManager) {
        super(layerManager, SupportedMods.XaeroMiniMap);
    }

    @Override
    public void clearActiveWaypoint() {
        xWaypoint = null;
    }

    @Override
    public boolean hasWaypoint() {
        return xWaypoint != null;
    }

    public WaypointWithDimension getXWaypoint() {
        return xWaypoint;
    }

    @Override
    public void updateActiveWaypoint(Waypoint waypoint) {
        if (!hasWaypoint() || waypoint.blockX != xWaypoint.getX()
            || waypoint.blockY != xWaypoint.getY()
            || waypoint.blockZ != xWaypoint.getZ()
            || waypoint.dimensionId != xWaypoint.getDimID()) {
            xWaypoint = new WaypointWithDimension(
                waypoint.blockX,
                waypoint.blockY,
                waypoint.blockZ,
                waypoint.label,
                getSymbol(waypoint),
                15,
                waypoint.dimensionId);
        }
    }

    protected String getSymbol(Waypoint waypoint) {
        return waypoint.label.substring(0, 1);
    }
}
