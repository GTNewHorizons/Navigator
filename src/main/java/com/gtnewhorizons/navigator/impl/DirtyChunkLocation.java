package com.gtnewhorizons.navigator.impl;

import com.gtnewhorizons.navigator.Utils;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;

public class DirtyChunkLocation implements IWaypointAndLocationProvider {

    private final int blockX;
    private final int blockZ;
    private final int dimensionId;
    private final boolean dirty;

    private boolean isActiveAsWaypoint;

    public DirtyChunkLocation(int chunkX, int chunkZ, int dimensionId, boolean dirty) {
        blockX = Utils.coordChunkToBlock(chunkX);
        blockZ = Utils.coordChunkToBlock(chunkZ);
        this.dimensionId = dimensionId;
        this.dirty = dirty;
    }

    public double getBlockX() {
        return blockX + 0.5;
    }

    public double getBlockZ() {
        return blockZ + 0.5;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public Waypoint toWaypoint() {
        return new Waypoint(blockX, 64, blockZ, getDimensionId(), "Example Waypoint", 0xFFFFFF);
    }

    @Override
    public boolean isActiveAsWaypoint() {
        return isActiveAsWaypoint;
    }

    @Override
    public void onWaypointCleared() {
        isActiveAsWaypoint = false;
    }

    @Override
    public void onWaypointUpdated(Waypoint waypoint) {
        isActiveAsWaypoint = waypoint.dimensionId == dimensionId && waypoint.blockX == blockX
            && waypoint.blockZ == blockZ;
    }
}
