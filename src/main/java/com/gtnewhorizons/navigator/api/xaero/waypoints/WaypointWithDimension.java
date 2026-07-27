package com.gtnewhorizons.navigator.api.xaero.waypoints;

import xaero.common.minimap.waypoints.Waypoint;

/** Xaero waypoint that disables itself whenever the player is outside its owning dimension. */
public class WaypointWithDimension extends Waypoint {

    private final int dimID;
    private int currentDim;

    public WaypointWithDimension(int x, int y, int z, String name, String symbol, int color, int dimID) {
        super(x, y, z, name, symbol, color);
        this.dimID = dimID;
        this.currentDim = dimID;
    }

    /** Updates the dimension used by {@link #isDisabled()}. */
    public void notifyDimension(int newDimID) {
        currentDim = newDimID;
    }

    /** @return dimension containing this waypoint */
    public int getDimID() {
        return dimID;
    }

    @Override
    public boolean isDisabled() {
        return super.isDisabled() || currentDim != dimID;
    }
}
