package com.gtnewhorizons.navigator.api.xaero.waypoints;

import java.util.Map;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

import xaero.common.minimap.waypoints.WaypointsManager;

public class XaeroWaypointManager extends WaypointManager {

    public static int lastId;
    protected final int waypointId;
    private WaypointWithDimension xWaypoint;
    private String symbol = "";

    public XaeroWaypointManager(InteractableLayerManager layerManager) {
        super(layerManager, SupportedMods.XaeroMiniMap);
        waypointId = lastId++;
    }

    public XaeroWaypointManager(InteractableLayerManager layerManager, String symbol) {
        this(layerManager);
        this.symbol = symbol;
    }

    @Override
    public void clearActiveWaypoint() {
        xWaypoint = null;
        getCustomWaypoints().remove(waypointId);
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
            getCustomWaypoints().put(waypointId, xWaypoint);
        }
    }

    protected Map<Integer, xaero.common.minimap.waypoints.Waypoint> getCustomWaypoints() {
        return WaypointsManager.getCustomWaypoints(Navigator.MODID);
    }

    protected String getSymbol(Waypoint waypoint) {
        if (!symbol.isEmpty()) {
            return symbol;
        }

        return waypoint.label.substring(0, 1);
    }
}
