package com.gtnewhorizons.navigator.api.model.layers;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.Util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

@SuppressWarnings({ "DeprecatedIsStillUsed", "unused" })
public abstract class LayerManager {

    private final ButtonManager buttonManager;
    public boolean forceRefresh = false;
    private final Int2ObjectMap<Long2ObjectMap<ILocationProvider>> dimCachedLocations = new Int2ObjectOpenHashMap<>();
    private Long2ObjectMap<ILocationProvider> currentDimCache;
    private final Set<ILocationProvider> visibleLocations = new HashSet<>();
    private final Set<ILocationProvider> removeQueue = new HashSet<>();
    protected final Map<SupportedMods, LayerRenderer> layerRenderer = new EnumMap<>(SupportedMods.class);
    private int miniMapWidth = 0;
    private int miniMapHeight = 0;
    private int fullscreenMapWidth = 0;
    private int fullscreenMapHeight = 0;
    private int currentDim;
    private SupportedMods openModGui;
    private boolean refreshDim = true;
    private boolean clearFull, clearCurrent;

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
     * @param packedChunk A long packed with {@link Util#packChunkToLocation(int, int)}
     * @return The {@link ILocationProvider} for the chunk or null if none
     */
    protected @Nullable ILocationProvider generateLocation(long packedChunk, int dim) {
        return null;
    }

    /**
     * Update the information contained in the {@link ILocationProvider}
     * <p>
     * If this information is updated outside of this method {@link #forceRefresh()} should be called
     *
     * @param location The location to update
     */
    public void updateElement(ILocationProvider location) {}

    /**
     * Needed for layers where a location represents an area larger than a single chunk
     *
     * @return The width/height of the element in chunks
     */
    public int getElementSize() {
        return 0;
    }

    private ILocationProvider getOrCreateLocation(int chunkX, int chunkZ) {
        long chunkKey = Util.packChunkToLocation(chunkX, chunkZ);
        ILocationProvider location = currentDimCache.get(chunkKey);
        if (location != null) return location;

        location = generateLocation(chunkX, chunkZ, currentDim);
        if (location == null) location = generateLocation(chunkKey, currentDim);
        if (location == null) location = tryOldLocation(chunkX, chunkZ);
        if (location == null) return null;

        currentDimCache.put(chunkKey, location);
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
                currentDimCache.put(location.toLong(), location);
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

        if (clearCurrent) clearCurrent();
        if (clearFull) clearFull();

        int dim = Minecraft.getMinecraft().thePlayer.dimension;
        if (refreshDim || currentDim != dim) {
            currentDim = dim;
            refreshDimCache();
        }

        if (!removeQueue.isEmpty()) {
            for (ILocationProvider location : removeQueue) {
                layerRenderer.values()
                    .forEach(layer -> layer.removeRenderStep(location.toLong()));
                currentDimCache.remove(location.toLong());
            }
            removeQueue.clear();
        }

        int chunkMinX = Util.coordBlockToChunk(minBlockX) - getElementSize();
        int chunkMinZ = Util.coordBlockToChunk(minBlockZ) - getElementSize();
        int chunkMaxX = Util.coordBlockToChunk(maxBlockX) + getElementSize();
        int chunkMaxZ = Util.coordBlockToChunk(maxBlockZ) + getElementSize();

        onUpdatePre(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);

        visibleLocations.clear();
        for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                ILocationProvider location = getOrCreateLocation(chunkX, chunkZ);
                if (location == null) continue;

                updateElement(location);
                visibleLocations.add(location);
            }
        }

        layerRenderer.values()
            .forEach(layer -> layer.refreshVisibleElements(visibleLocations));
        onUpdatePost(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);
    }

    public void onLayerToggled(boolean toEnable) {
        if (!toEnable) {
            clearFull();
        }
    }

    public void onUpdatePre(int minX, int maxX, int minZ, int maxZ) {}

    public void onUpdatePost(int minX, int maxX, int minZ, int maxZ) {}

    public final void removeLocation(ILocationProvider location) {
        removeQueue.add(location);
        forceRefresh();
    }

    /**
     * @param location must be a long packed with {@link Util#packChunkToLocation(int, int)}
     */
    public final void removeLocation(long location) {
        ILocationProvider loc = currentDimCache.get(location);
        if (loc == null) return;
        removeLocation(loc);
    }

    public final void removeLocation(int chunkX, int chunkZ) {
        removeLocation(Util.packChunkToLocation(chunkX, chunkZ));
    }

    public final void addExtraLocation(ILocationProvider location) {
        currentDimCache.put(location.toLong(), location);
    }

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
        return getCurrentDimCache().values();
    }

    public Long2ObjectMap<ILocationProvider> getCurrentDimCache() {
        if (currentDimCache == null) {
            currentDimCache = dimCachedLocations.computeIfAbsent(currentDim, k -> new Long2ObjectOpenHashMap<>());
        }
        return currentDimCache;
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

    protected void refreshDimCache() {
        refreshDim = false;
        currentDimCache = dimCachedLocations.computeIfAbsent(currentDim, k -> new Long2ObjectOpenHashMap<>());
        layerRenderer.values()
            .forEach(renderer -> renderer.setDimCache(currentDim));
    }

    public void clearCurrentCache() {
        clearCurrent = true;
        forceRefresh();
    }

    public void clearFullCache() {
        clearFull = true;
        forceRefresh();
    }

    private void clearCurrent() {
        clearCurrent = false;
        if (currentDimCache == null) return;
        currentDimCache.clear();
        layerRenderer.values()
            .forEach(LayerRenderer::clearCurrentCache);
    }

    private void clearFull() {
        clearFull = false;
        refreshDim = true;
        dimCachedLocations.clear();
        currentDimCache = null;
        layerRenderer.values()
            .forEach(LayerRenderer::clearFullCache);
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
