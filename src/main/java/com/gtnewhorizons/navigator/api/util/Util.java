package com.gtnewhorizons.navigator.api.util;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.gtnewhorizons.navigator.api.journeymap.JourneyMapVersion;

import cpw.mods.fml.common.Loader;

public class Util {

    private static final JourneyMapVersion journeyMapVersion;
    private static final boolean isXaeroWorldMapLoaded;
    private static final boolean isXaeroMinimapLoaded;
    private static final boolean isNEILoaded;
    private static boolean isVoxelMapLoaded;

    static {
        journeyMapVersion = JourneyMapVersion.get();
        isXaeroWorldMapLoaded = Loader.isModLoaded("XaeroWorldMap");
        isXaeroMinimapLoaded = Loader.isModLoaded("XaeroMinimap");
        isNEILoaded = Loader.isModLoaded("NotEnoughItems");
        isVoxelMapLoaded = false;
        try {
            Class.forName("com.thevoxelbox.voxelmap.litemod.LiteModVoxelMap");
            isVoxelMapLoaded = true;
        } catch (Exception e) {
            // Ignore
        }
    }

    public static boolean isJourneyMapInstalled() {
        return journeyMapVersion != JourneyMapVersion.NONE;
    }

    public static boolean isJourneyMapV5Installed() {
        return journeyMapVersion == JourneyMapVersion.V5;
    }

    public static boolean isJourneyMapV6Installed() {
        return journeyMapVersion == JourneyMapVersion.V6;
    }

    public static JourneyMapVersion getJourneyMapVersion() {
        return journeyMapVersion;
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

    public static boolean isNEIInstalled() {
        return isNEILoaded;
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
