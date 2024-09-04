package com.gtnewhorizons.navigator.api.util;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;

import cpw.mods.fml.common.Loader;

public class Util {

    private static final boolean isJourneyMapLoaded;
    private static final boolean isXaeroWorldMapLoaded;
    private static final boolean isXaeroMinimapLoaded;
    private static boolean isVoxelMapLoaded;

    static {
        isJourneyMapLoaded = Loader.isModLoaded("journeymap");
        isXaeroWorldMapLoaded = Loader.isModLoaded("XaeroWorldMap");
        isXaeroMinimapLoaded = Loader.isModLoaded("XaeroMinimap");
        isVoxelMapLoaded = false;
        try {
            Class.forName("com.thevoxelbox.voxelmap.litemod.LiteModVoxelMap");
            isVoxelMapLoaded = true;
        } catch (Exception e) {
            // Ignore
        }
    }

    public static boolean isJourneyMapInstalled() {
        return isJourneyMapLoaded;
    }

    public static boolean isXaerosWorldMapInstalled() {
        return isXaeroWorldMapLoaded;
    }

    public static boolean isXaerosMinimapInstalled() {
        return isXaeroMinimapLoaded;
    }

    public static boolean isVoxelMapInstalled() {
        return isVoxelMapLoaded;
    }

    public static int coordBlockToChunk(int blockCoord) {
        return blockCoord < 0 ? -((-blockCoord - 1) >> 4) - 1 : blockCoord >> 4;
    }

    public static int coordChunkToBlock(int chunkCoord) {
        return chunkCoord < 0 ? -((-chunkCoord) << 4) : chunkCoord << 4;
    }

    public static long packChunkToLocation(int chunkX, int chunkZ) {
        return CoordinatePacker.pack(chunkX, 0, chunkZ);
    }

    public static double journeyMapScaleToLinear(final int jzoom) {
        return Math.pow(2, jzoom);
    }

    public static boolean isKeyPressed(KeyBinding key) {
        return key.isPressed() || Keyboard.isKeyDown(key.getKeyCode());
    }
}
