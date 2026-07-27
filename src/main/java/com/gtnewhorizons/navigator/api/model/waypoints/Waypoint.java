package com.gtnewhorizons.navigator.api.model.waypoints;

/** Immutable map-neutral waypoint value published by a waypoint-capable location. */
public class Waypoint {

    public final int blockX;
    public final int blockY;
    public final int blockZ;
    public final int dimensionId;
    public final String label;
    public final int color;

    /**
     * @param blockX      world block X
     * @param blockY      world block Y
     * @param blockZ      world block Z
     * @param dimensionId dimension containing the waypoint
     * @param label       display label
     * @param color       RGB color
     */
    public Waypoint(int blockX, int blockY, int blockZ, int dimensionId, String label, int color) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.dimensionId = dimensionId;
        this.label = label;
        this.color = color;
    }
}
