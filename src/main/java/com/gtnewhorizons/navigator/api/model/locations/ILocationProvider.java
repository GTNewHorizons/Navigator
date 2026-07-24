package com.gtnewhorizons.navigator.api.model.locations;

import com.gtnewhorizons.navigator.api.util.Util;

/**
 * Logical element that can be cached and shown by a Navigator layer.
 * <p>
 * The default identity is its packed chunk position, so it is appropriate for chunk-grid data and one element per
 * chunk. Implementations that represent a larger element may override {@link #toLong()} with another stable key.
 */
public interface ILocationProvider {

    /** @return dimension containing this element */
    int getDimensionId();

    /** @return world block X coordinate; fractional positions are allowed */
    double getBlockX();

    /** @return world block Z coordinate; fractional positions are allowed */
    double getBlockZ();

    /**
     * Returns the stable identity used by renderer caches and removal operations.
     *
     * @return packed chunk identity by default
     */
    default long toLong() {
        return Util.packChunkToLocation(getChunkX(), getChunkZ());
    }

    /** @return chunk X containing {@link #getBlockX()} */
    default int getChunkX() {
        return Util.coordBlockToChunk((int) Math.floor(getBlockX()));
    }

    /** @return chunk Z containing {@link #getBlockZ()} */
    default int getChunkZ() {
        return Util.coordBlockToChunk((int) Math.floor(getBlockZ()));
    }

}
