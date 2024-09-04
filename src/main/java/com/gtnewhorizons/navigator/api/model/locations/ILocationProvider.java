package com.gtnewhorizons.navigator.api.model.locations;

import com.gtnewhorizons.navigator.api.util.Util;

public interface ILocationProvider {

    int getDimensionId();

    double getBlockX();

    double getBlockZ();

    default long toLong() {
        return Util.packChunkToLocation(getChunkX(), getChunkZ());
    }

    default int getChunkX() {
        return Util.coordBlockToChunk((int) getBlockX());
    }

    default int getChunkZ() {
        return Util.coordBlockToChunk((int) getBlockZ());
    }

}
