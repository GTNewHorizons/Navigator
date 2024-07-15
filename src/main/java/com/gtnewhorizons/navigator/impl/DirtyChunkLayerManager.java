package com.gtnewhorizons.navigator.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.api.xaero.waypoints.XaeroWaypointManager;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkRenderer;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkRenderer;

public class DirtyChunkLayerManager extends InteractableLayerManager {

    public static final DirtyChunkLayerManager INSTANCE = new DirtyChunkLayerManager();

    public DirtyChunkLayerManager() {
        super(new DirtyChunkButtonManager());
    }

    @Override
    protected @Nullable LayerRenderer addLayerRenderer(InteractableLayerManager manager, SupportedMods mod) {
        return switch (mod) {
            case XaeroWorldMap -> new XaeroDirtyChunkRenderer(manager);
            case JourneyMap -> new JMDirtyChunkRenderer(manager);
            default -> null;
        };
    }

    @Nullable
    @Override
    protected WaypointManager addWaypointManager(InteractableLayerManager manager, SupportedMods mod) {
        return switch (mod) {
            case XaeroWorldMap -> new XaeroWaypointManager(manager);
            case JourneyMap -> new JMWaypointManager(manager);
            default -> null;
        };
    }

    @Override
    protected List<? extends IWaypointAndLocationProvider> generateVisibleElements(int minBlockX, int minBlockZ,
        int maxBlockX, int maxBlockZ) {
        final int minX = Util.coordBlockToChunk(minBlockX);
        final int minZ = Util.coordBlockToChunk(minBlockZ);
        final int maxX = Util.coordBlockToChunk(maxBlockX);
        final int maxZ = Util.coordBlockToChunk(maxBlockZ);
        final EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        final int playerDimensionId = player.dimension;

        ArrayList<DirtyChunkLocation> dirtyChunks = new ArrayList<>();

        if (MinecraftServer.getServer() == null || MinecraftServer.getServer()
            .worldServerForDimension(playerDimensionId) == null) {
            return dirtyChunks;
        }

        World w = MinecraftServer.getServer()
            .worldServerForDimension(playerDimensionId);
        IChunkProvider chunkProvider = w.getChunkProvider();

        for (int chunkX = minX; chunkX <= maxX; chunkX++) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                if (!chunkProvider.chunkExists(chunkX, chunkZ)) {
                    continue;
                }
                final boolean dirty = w.getChunkFromChunkCoords(chunkX, chunkZ).isModified;
                dirtyChunks.add(new DirtyChunkLocation(chunkX, chunkZ, playerDimensionId, dirty));
            }
        }

        return dirtyChunks;
    }
}
