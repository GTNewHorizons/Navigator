package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import com.gtnewhorizon.gtnhlib.util.CoordinatePacker;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.Util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class LayerManager {

    private final ButtonManager buttonManager;

    public boolean forceRefresh = false;
    protected List<? extends ILocationProvider> visibleElements = new ArrayList<>();
    private final Long2ObjectMap<ILocationProvider> cachedLocations = new Long2ObjectOpenHashMap<>();
    private final Set<ILocationProvider> visibleLocations = new HashSet<>();
    private final Set<ILocationProvider> removeQueue = new HashSet<>();
    protected final Map<SupportedMods, LayerRenderer> layerRenderer = new EnumMap<>(SupportedMods.class);
    private int miniMapWidth = 0;
    private int miniMapHeight = 0;
    private int fullscreenMapWidth = 0;
    private int fullscreenMapHeight = 0;
    private SupportedMods openModGui;

    public LayerManager(ButtonManager buttonManager) {
        this.buttonManager = buttonManager;
        buttonManager.setLayerNotify(this::onLayerToggled);
        for (SupportedMods mod : SupportedMods.values()) {
            if (!mod.isEnabled()) continue;

            LayerRenderer renderer = addLayerRenderer(this, mod);
            if (renderer == null) continue;
            layerRenderer.put(mod, renderer);
        }
    }

    /**
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The {@link LayerRenderer} implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod);

    /**
     * @param chunkX The chunk x coordinate
     * @param chunkZ The chunk z coordinate
     * @param dim    The dimension id
     * @return The {@link ILocationProvider} for the chunk or null if none
     */
    protected @Nullable ILocationProvider generateLocation(int chunkX, int chunkZ, int dim) {
        return null;
    }

    /**
     * @param packedChunk A long packed with {@link CoordinatePacker#pack(int chunkX, int dim, int chunkZ)}
     * @return The {@link ILocationProvider} for the chunk or null if none
     */
    protected @Nullable ILocationProvider generateLocation(long packedChunk) {
        return null;
    }

    private ILocationProvider getOrCreateLocation(int chunkX, int chunkZ, int dim) {
        long chunkKey = CoordinatePacker.pack(chunkX, dim, chunkZ);
        ILocationProvider location = cachedLocations.get(chunkKey);
        if (location != null) return location;

        location = generateLocation(chunkX, chunkZ, dim);
        if (location == null) location = generateLocation(chunkKey);
        if (location == null) location = tryOldLocation(chunkX, chunkZ);
        if (location == null) return null;

        cachedLocations.put(chunkKey, location);
        return location;
    }

    private ILocationProvider tryOldLocation(int chunkX, int chunkZ) {
        int minBlockX = Util.coordChunkToBlock(chunkX);
        int minBlockZ = Util.coordChunkToBlock(chunkZ);
        int maxBlockX = minBlockX + 15;
        int maxBlockZ = minBlockZ + 15;
        if (needsRegenerateVisibleElements(minBlockX, minBlockZ, maxBlockX, maxBlockZ)) {
            List<? extends ILocationProvider> oldLoc = generateVisibleElements(
                minBlockX,
                minBlockZ,
                maxBlockX,
                maxBlockZ);
            if (oldLoc == null || oldLoc.isEmpty()) return null;
            ILocationProvider loc = null;
            for (ILocationProvider location : oldLoc) {
                // Capture first location to return
                if (loc == null) {
                    loc = location;
                    continue;
                }
                cachedLocations.put(location.toLong(), location);
            }

            return loc;
        }
        return null;
    }

    public boolean isLayerActive() {
        return buttonManager.isActive();
    }

    public void activateLayer() {
        buttonManager.activate();
    }

    public void deactivateLayer() {
        buttonManager.deactivate();
    }

    public void toggleLayer() {
        buttonManager.toggle();
    }

    public void forceRefresh() {
        forceRefresh = true;
    }

    public final void onGuiOpened(SupportedMods mod) {
        openModGui = mod;
        clearCache();
        onOpenMap();
    }

    public final void onGuiClosed(SupportedMods mod) {
        openModGui = SupportedMods.NONE;
        onCloseMap();
    }

    public void onOpenMap() {}

    public void onCloseMap() {}

    public final SupportedMods getOpenModGui() {
        return openModGui;
    }

    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockRadius) {
        recacheMiniMap(centerBlockX, centerBlockZ, blockRadius, blockRadius);
    }

    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        miniMapWidth = blockWidth;
        miniMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    public void recacheFullscreenMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        fullscreenMapWidth = blockWidth;
        fullscreenMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    private void recacheVisibleElements(int centerBlockX, int centerBlockZ) {
        int radiusBlockX = (Math.max(miniMapWidth, fullscreenMapWidth) + 1) >> 1;
        int radiusBlockZ = (Math.max(miniMapHeight, fullscreenMapHeight) + 1) >> 1;
        int minBlockX = centerBlockX - radiusBlockX;
        int minBlockZ = centerBlockZ - radiusBlockZ;
        int maxBlockX = centerBlockX + radiusBlockX;
        int maxBlockZ = centerBlockZ + radiusBlockZ;

        if (!removeQueue.isEmpty()) {
            for (ILocationProvider location : removeQueue) {
                layerRenderer.values()
                    .forEach(layer -> layer.removeRenderStep(location.toLong()));
                cachedLocations.remove(location.toLong());
            }
            removeQueue.clear();
        }

        int chunkMinX = Util.coordBlockToChunk(minBlockX);
        int chunkMinZ = Util.coordBlockToChunk(minBlockZ);
        int chunkMaxX = Util.coordBlockToChunk(maxBlockX);
        int chunkMaxZ = Util.coordBlockToChunk(maxBlockZ);
        int dim = Minecraft.getMinecraft().thePlayer.dimension;

        onUpdatePre(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);

        visibleLocations.clear();
        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                ILocationProvider location = getOrCreateLocation(chunkX, chunkZ, dim);
                if (location == null) continue;

                updateElement(location);
                visibleLocations.add(location);
            }
        }

        layerRenderer.values()
            .forEach(layer -> layer.refreshVisibleElements(visibleLocations));
        onUpdatePost(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);
    }

    public void onLayerToggled(boolean state) {
        clearCache();
    }

    public void onUpdatePre(int minX, int maxX, int minZ, int maxZ) {}

    public void onUpdatePost(int minX, int maxX, int minZ, int maxZ) {}

    public final void removeLocation(ILocationProvider location) {
        removeQueue.add(location);
        forceRefresh();
    }

    public final void removeLocation(long location) {
        ILocationProvider loc = cachedLocations.get(location);
        if (loc == null) return;
        removeLocation(loc);
    }

    public final void addExtraLocation(ILocationProvider location) {
        cachedLocations.put(location.toLong(), location);
    }

    /**
     * Update the information contained in the {@link ILocationProvider}
     * <p>
     * If this information is updated outside of this method {@link #forceRefresh()} should be called
     *
     * @param location The location to update
     */
    public void updateElement(ILocationProvider location) {}

    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    public LayerRenderer getLayerRenderer(SupportedMods map) {
        return layerRenderer.get(map);
    }

    public Collection<? extends ILocationProvider> getVisibleLocations() {
        return visibleLocations;
    }

    public Collection<? extends ILocationProvider> getCachedLocations() {
        return cachedLocations.values();
    }

    /**
     * Whether the layer is enabled for the corresponding mod.
     *
     * @param mod the mod checking if it is enabled
     * @return true if there is a layer implementation for the mod, false otherwise
     */
    public boolean isEnabled(SupportedMods mod) {
        return layerRenderer.containsKey(mod);
    }

    public void clearCache() {
        cachedLocations.clear();
        layerRenderer.values()
            .forEach(LayerRenderer::clearRenderSteps);
    }

    /**
     * @deprecated Use {@link #updateElement(ILocationProvider)} to update the info contained in a single
     *             {@link ILocationProvider}
     */
    @Deprecated
    protected void checkAndUpdateElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {}

    @Deprecated
    protected boolean needsRegenerateVisibleElements(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        return false;
    }

    /**
     * @deprecated Use {@link #generateLocation(int chunkX, int chunkZ, int dim)} to generate a single location
     */
    @Deprecated
    protected List<? extends ILocationProvider> generateVisibleElements(int minBlockX, int minBlockZ, int maxBlockX,
        int maxBlockZ) {
        return null;
    }
}
