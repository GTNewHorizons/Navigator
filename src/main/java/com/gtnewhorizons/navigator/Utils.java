package com.gtnewhorizons.navigator;

import cpw.mods.fml.common.Loader;

public class Utils {

    public static boolean isJourneyMapInstalled() {
        return Loader.isModLoaded("journeymap");
    }

    public static boolean isXaerosWorldMapInstalled() {
        return Loader.isModLoaded("XaeroWorldMap");
    }

    public static boolean isXaerosMinimapInstalled() {
        return Loader.isModLoaded("XaeroMinimap");
    }

    public static boolean isVoxelMapInstalled() {
        try {
            // If a LiteLoader mod is present cannot be checked by calling Loader#isModLoaded.
            // Instead, we check if the VoxelMap main class is present.
            Class.forName("com.thevoxelbox.voxelmap.litemod.LiteModVoxelMap");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static int coordBlockToChunk(int blockCoord) {
        return blockCoord < 0 ? -((-blockCoord - 1) >> 4) - 1 : blockCoord >> 4;
    }

    public static int coordChunkToBlock(int chunkCoord) {
        return chunkCoord < 0 ? -((-chunkCoord) << 4) : chunkCoord << 4;
    }

    public static double journeyMapScaleToLinear(final int jzoom) {
        return Math.pow(2, jzoom);
    }
}
