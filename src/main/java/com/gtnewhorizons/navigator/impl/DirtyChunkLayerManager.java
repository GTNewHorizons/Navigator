package com.gtnewhorizons.navigator.impl;

import javax.annotation.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
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
    public void updateElement(IWaypointAndLocationProvider location) {
        DirtyChunkLocation loc = (DirtyChunkLocation) location;
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return;
        World world = server.worldServerForDimension(loc.getDimensionId());

        boolean dirty = world.getChunkFromChunkCoords(loc.getChunkX(), loc.getChunkZ()).isModified;
        loc.setDirty(dirty);
    }

    @Override
    protected ILocationProvider generateLocation(int chunkX, int chunkZ, int dim) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) return null;
        World world = server.worldServerForDimension(dim);
        IChunkProvider chunkProvider = world.getChunkProvider();

        if (!chunkProvider.chunkExists(chunkX, chunkZ)) {
            return null;
        }
        boolean dirty = world.getChunkFromChunkCoords(chunkX, chunkZ).isModified;
        return new DirtyChunkLocation(chunkX, chunkZ, dim, dirty);
    }
}
