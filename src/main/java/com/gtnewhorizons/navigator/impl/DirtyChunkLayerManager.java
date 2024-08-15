package com.gtnewhorizons.navigator.impl;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalInteractableRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;
import com.gtnewhorizons.navigator.api.util.ClickPos;
import com.gtnewhorizons.navigator.api.xaero.waypoints.XaeroWaypointManager;

public class DirtyChunkLayerManager extends InteractableLayerManager {

    public static final DirtyChunkLayerManager INSTANCE = new DirtyChunkLayerManager();

    public DirtyChunkLayerManager() {
        super(new DirtyChunkButtonManager());
    }

    @Override
    protected @Nullable LayerRenderer addLayerRenderer(InteractableLayerManager manager, SupportedMods mod) {
        return new UniversalInteractableRenderer(manager).withClickAction(this::onClick)
            .withKeyPressAction(this::onKeyPress)
            .withRenderStep(location -> new DirtyChunkRenderStep((DirtyChunkLocation) location));
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

        IChunkProvider chunkProvider = world.getChunkProvider();

        int chunkX = loc.getChunkX();
        int chunkZ = loc.getChunkZ();
        if (!chunkProvider.chunkExists(chunkX, chunkZ)) {
            return;
        }
        boolean dirty = world.getChunkFromChunkCoords(chunkX, chunkZ).isModified;
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

    public boolean onClick(ClickPos pos) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (pos.getRenderStep() == null) {
            player.addChatMessage(new ChatComponentText("Clicked outside of render step"));
        } else {
            DirtyChunkLocation loc = (DirtyChunkLocation) pos.getRenderStep()
                .getLocation();
            String status = loc.isDirty() ? "dirty" : "clean";
            player.addChatMessage(
                new ChatComponentText("Chunk " + loc.getChunkX() + ", " + loc.getChunkZ() + " is " + status));
        }
        return true;
    }

    public boolean onKeyPress(int keyCode) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        player.addChatMessage(new ChatComponentText("Key pressed: " + keyCode));
        return false;
    }
}
