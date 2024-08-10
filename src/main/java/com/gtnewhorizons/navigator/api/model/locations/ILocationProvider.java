package com.gtnewhorizons.navigator.api.model.locations;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.gtnewhorizons.navigator.api.util.Util;

public interface ILocationProvider {

    int getDimensionId();

    double getBlockX();

    double getBlockZ();

    default long toLong() {
        return CoordinatePacker.pack(getChunkX(), getDimensionId(), getChunkZ());
    }

    default int getChunkX() {
        return Util.coordBlockToChunk((int) getBlockX());
    }

    default int getChunkZ() {
        return Util.coordBlockToChunk((int) getBlockZ());
    }

}
