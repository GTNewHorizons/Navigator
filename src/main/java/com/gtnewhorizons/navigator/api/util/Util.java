package com.gtnewhorizons.navigator.api.util;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.gtnewhorizons.navigator.api.journeymap.JourneyMapVersion;

import cpw.mods.fml.common.Loader;

/** Cached optional-mod detection, coordinate conversion, packing, zoom, and key helpers. */
public class Util {

    private static final boolean isXaeroWorldMapLoaded;
    private static final boolean isXaeroMinimapLoaded;
    private static final boolean isNEILoaded;
    private static boolean isVoxelMapLoaded;

    static {
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

    /** @return whether any supported JourneyMap generation is installed */
    public static boolean isJourneyMapInstalled() {
        return JourneyMapVersion.get() != JourneyMapVersion.NONE;
    }

    /** @return whether the installed JourneyMap uses the legacy v5 API */
    public static boolean isJourneyMapV5Installed() {
        return JourneyMapVersion.get() == JourneyMapVersion.V5;
    }

    /** @return whether the installed JourneyMap exposes the v2 API used by the 1.7.10 v6 backport */
    public static boolean isJourneyMapV6Installed() {
        return JourneyMapVersion.get() == JourneyMapVersion.V6;
    }

    /** @return cached JourneyMap generation */
    public static JourneyMapVersion getJourneyMapVersion() {
        return JourneyMapVersion.get();
    }

    /** @return whether Xaero's World Map is installed */
    public static boolean isXaerosWorldMapInstalled() {
        return isXaeroWorldMapLoaded;
    }

    /** @return whether Xaero's Minimap is installed */
    public static boolean isXaerosMinimapInstalled() {
        return isXaeroMinimapLoaded;
    }

    /** @return whether the supported VoxelMap class is present */
    public static boolean isVoxelMapInstalled() {
        return isVoxelMapLoaded;
    }

    /** @return whether NotEnoughItems is installed */
    public static boolean isNEIInstalled() {
        return isNEILoaded;
    }

    /**
     * Converts a block coordinate to its containing chunk with correct negative-coordinate flooring.
     */
    public static int coordBlockToChunk(int blockCoord) {
        return blockCoord < 0 ? -((-blockCoord - 1) >> 4) - 1 : blockCoord >> 4;
    }

    /** Converts a chunk coordinate to the block coordinate of its minimum edge. */
    public static int coordChunkToBlock(int chunkCoord) {
        return chunkCoord < 0 ? -((-chunkCoord) << 4) : chunkCoord << 4;
    }

    /** Packs chunk X/Z into Navigator's location key format. */
    public static long packChunkToLocation(int chunkX, int chunkZ) {
        return CoordinatePacker.pack(chunkX, 0, chunkZ);
    }

    /** Converts JourneyMap's exponential zoom step to a linear scale. */
    public static double journeyMapScaleToLinear(final int jzoom) {
        return Math.pow(2, jzoom);
    }

    /**
     * @return whether a key binding emitted a press or is currently held
     */
    public static boolean isKeyPressed(KeyBinding key) {
        return key.isPressed() || Keyboard.isKeyDown(key.getKeyCode());
    }
}
