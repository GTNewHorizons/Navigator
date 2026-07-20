package com.gtnewhorizons.navigator.api.model.layers;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.Util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Owns one logical layer's location cache, viewport discovery, button state, search hook, and map renderers.
 * <p>
 * Locations are cached per dimension and discovered by chunk or as a viewport collection. A recache uses the larger
 * of the active minimap and fullscreen viewports, calls the update hooks, and then gives every renderer the same
 * visible location set.
 */
@SuppressWarnings({ "DeprecatedIsStillUsed", "unused" })
public abstract class LayerManager {

    private final ButtonManager buttonManager;
    public boolean forceRefresh = false;
    private final Int2ObjectMap<Long2ObjectMap<ILocationProvider>> dimCachedLocations = new Int2ObjectOpenHashMap<>();
    private Long2ObjectMap<ILocationProvider> currentDimCache;
    private final Set<ILocationProvider> visibleLocations = new LinkedHashSet<>();
    private final Set<ILocationProvider> removeQueue = new HashSet<>();
    protected final Map<SupportedMods, LayerRenderer> layerRenderer = new EnumMap<>(SupportedMods.class);
    private int miniMapWidth = 0;
    private int miniMapHeight = 0;
    private int fullscreenMapWidth = 0;
    private int fullscreenMapHeight = 0;
    private int currentDim;
    private SupportedMods openModGui = SupportedMods.NONE;
    private boolean refreshDim = true;
    private boolean clearFull, clearCurrent;
    private boolean hasSearchField;
    private long refreshVersion;

    /**
     * Creates a layer and its renderers for currently enabled map integrations.
     *
     * @param buttonManager shared logical button controlling this layer
     */
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
     * Creates the renderer used by one installed map integration.
     * <p>
     * Return a universal renderer for map-neutral behavior or {@code null} when this layer does not support the map.
     *
     * @param manager This layer manager
     * @param mod     The mod to add the layer renderer for
     * @return The {@link LayerRenderer} implementation for the mod or null if none
     */
    protected abstract @Nullable LayerRenderer addLayerRenderer(LayerManager manager, SupportedMods mod);

    /**
     * Discovers one location for a chunk that is not already cached.
     *
     * @param chunkX The chunk x coordinate
     * @param chunkZ The chunk z coordinate
     * @param dim    The dimension id
     * @return The {@link ILocationProvider} for the chunk or null if none
     */
    protected @Nullable ILocationProvider generateLocation(int chunkX, int chunkZ, int dim) {
        return null;
    }

    /**
     * Alternative discovery hook using a packed chunk key.
     *
     * @param packedChunk A long packed with {@link Util#packChunkToLocation(int, int)}
     * @param dim         dimension id
     * @return The {@link ILocationProvider} for the chunk or null if none
     */
    protected @Nullable ILocationProvider generateLocation(long packedChunk, int dim) {
        return null;
    }

    /**
     * Discovers every location in the current viewport when a layer can contain multiple elements per chunk.
     * <p>
     * Return {@code null} to use the normal chunk-by-chunk {@link #generateLocation(int, int, int)} path. Each
     * returned location must provide a stable, layer-unique {@link ILocationProvider#toLong()} identity. Navigator
     * retains the first object for an identity and calls {@link #updateElement(ILocationProvider)} on it during later
     * recaches.
     *
     * @param minBlockX inclusive minimum block X
     * @param minBlockZ inclusive minimum block Z
     * @param maxBlockX inclusive maximum block X
     * @param maxBlockZ inclusive maximum block Z
     * @param dimension current dimension
     * @return visible locations, an empty collection for none, or {@code null} to use chunk discovery
     */
    protected @Nullable Collection<? extends ILocationProvider> generateVisibleLocations(int minBlockX, int minBlockZ,
        int maxBlockX, int maxBlockZ, int dimension) {
        return null;
    }

    /**
     * Updates a cached visible location during each recache.
     * <p>
     * If the information changes outside this method, call {@link #forceRefresh()}.
     *
     * @param location The location to update
     */
    public void updateElement(ILocationProvider location) {}

    /**
     * Expands discovery around the viewport for elements larger than one chunk.
     *
     * @return extra chunks added on every side of the discovery bounds
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

    /** @return whether the shared layer button is active */
    public boolean isLayerActive() {
        return buttonManager.isActive();
    }

    /** Activates this layer and deactivates other distinct Navigator buttons. */
    public void activateLayer() {
        buttonManager.activate();
    }

    /** Deactivates this layer. */
    public void deactivateLayer() {
        buttonManager.deactivate();
    }

    /** Toggles this layer. */
    public void toggleLayer() {
        buttonManager.toggle();
    }

    /**
     * Requests renderer/native-overlay synchronization after external data changes.
     * <p>
     * The legacy flag is consumed by the next successful recache. Repeated calls increment the refresh version and
     * should be avoided when nothing changed.
     */
    public void forceRefresh() {
        forceRefresh = true;
        refreshVersion++;
    }

    /** @return monotonically increasing version incremented by {@link #forceRefresh()} */
    public long getRefreshVersion() {
        return refreshVersion;
    }

    /**
     * Records that a supported fullscreen map opened and calls {@link #onOpenMap()}.
     *
     * @param mod map integration that opened
     */
    public final void onGuiOpened(SupportedMods mod) {
        openModGui = mod;
        onOpenMap();
    }

    /**
     * Records that a supported fullscreen map closed and calls {@link #onCloseMap()}.
     *
     * @param mod map integration that closed
     */
    public final void onGuiClosed(SupportedMods mod) {
        openModGui = SupportedMods.NONE;
        onCloseMap();
    }

    /** Consumer hook called after a supported fullscreen map opens. */
    public void onOpenMap() {}

    /** Consumer hook called after a supported fullscreen map closes. */
    public void onCloseMap() {}

    /** @return currently open map integration, or {@link SupportedMods#NONE} */
    public final SupportedMods getOpenModGui() {
        return openModGui;
    }

    /**
     * Recaches a square minimap viewport.
     *
     * @param centerBlockX viewport center X
     * @param centerBlockZ viewport center Z
     * @param blockWidth   viewport width and height in blocks
     */
    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth) {
        recacheMiniMap(centerBlockX, centerBlockZ, blockWidth, blockWidth);
    }

    /**
     * Recaches a rectangular minimap viewport.
     *
     * @param centerBlockX viewport center X
     * @param centerBlockZ viewport center Z
     * @param blockWidth   viewport width in blocks
     * @param blockHeight  viewport height in blocks
     */
    public void recacheMiniMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        miniMapWidth = blockWidth;
        miniMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    /**
     * Recaches a fullscreen viewport.
     *
     * @param centerBlockX viewport center X
     * @param centerBlockZ viewport center Z
     * @param blockWidth   viewport width in blocks
     * @param blockHeight  viewport height in blocks
     */
    public void recacheFullscreenMap(int centerBlockX, int centerBlockZ, int blockWidth, int blockHeight) {
        fullscreenMapWidth = blockWidth;
        fullscreenMapHeight = blockHeight;
        recacheVisibleElements(centerBlockX, centerBlockZ);
    }

    private void recacheVisibleElements(int centerBlockX, int centerBlockZ) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.theWorld == null) return;
        forceRefresh = false;

        int radiusBlockX = (Math.max(miniMapWidth, fullscreenMapWidth) + 1) >> 1;
        int radiusBlockZ = (Math.max(miniMapHeight, fullscreenMapHeight) + 1) >> 1;
        int minBlockX = centerBlockX - radiusBlockX;
        int minBlockZ = centerBlockZ - radiusBlockZ;
        int maxBlockX = centerBlockX + radiusBlockX;
        int maxBlockZ = centerBlockZ + radiusBlockZ;

        if (clearCurrent) clearCurrent();
        if (clearFull) clearFull();

        int dim = minecraft.thePlayer.dimension;
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
        Collection<? extends ILocationProvider> generatedLocations = generateVisibleLocations(
            minBlockX,
            minBlockZ,
            maxBlockX,
            maxBlockZ,
            currentDim);
        if (generatedLocations != null) {
            for (ILocationProvider generated : generatedLocations) {
                if (generated == null || generated.getDimensionId() != currentDim) continue;
                ILocationProvider location = currentDimCache.get(generated.toLong());
                if (location == null) {
                    location = generated;
                    currentDimCache.put(location.toLong(), location);
                }
                updateElement(location);
                visibleLocations.add(location);
            }
        } else {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                    ILocationProvider location = getOrCreateLocation(chunkX, chunkZ);
                    if (location == null) continue;

                    updateElement(location);
                    visibleLocations.add(location);
                }
            }
        }

        layerRenderer.values()
            .forEach(layer -> layer.refreshVisibleElements(visibleLocations));
        onUpdatePost(chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ);
    }

    /**
     * Called when the shared layer button changes.
     * <p>
     * The default implementation clears all caches when disabled. Overrides should call {@code super} unless they
     * deliberately retain renderer state.
     *
     * @param toEnable new active state
     */
    public void onLayerToggled(boolean toEnable) {
        if (!toEnable) {
            clearFull();
        }
    }

    /**
     * Runs before chunk discovery/update for a viewport recache.
     *
     * @param minX inclusive minimum chunk X
     * @param maxX inclusive maximum chunk X
     * @param minZ inclusive minimum chunk Z
     * @param maxZ inclusive maximum chunk Z
     */
    public void onUpdatePre(int minX, int maxX, int minZ, int maxZ) {}

    /**
     * Runs after visible locations and renderer steps have been refreshed.
     *
     * @param minX inclusive minimum chunk X
     * @param maxX inclusive maximum chunk X
     * @param minZ inclusive minimum chunk Z
     * @param maxZ inclusive maximum chunk Z
     */
    public void onUpdatePost(int minX, int maxX, int minZ, int maxZ) {}

    /**
     * Queues a cached location and its render steps for removal on the next recache.
     *
     * @param location location to remove
     */
    public final void removeLocation(ILocationProvider location) {
        removeQueue.add(location);
        forceRefresh();
    }

    /**
     * Queues a cached location for removal by identity.
     *
     * @param location stable location identity, normally from {@link ILocationProvider#toLong()}
     */
    public final void removeLocation(long location) {
        if (currentDimCache == null) return;
        ILocationProvider loc = currentDimCache.get(location);
        if (loc == null) return;
        removeLocation(loc);
    }

    /** Queues the location cached under a chunk coordinate for removal. */
    public final void removeLocation(int chunkX, int chunkZ) {
        removeLocation(Util.packChunkToLocation(chunkX, chunkZ));
    }

    /**
     * Inserts an already-created location into the current dimension cache.
     *
     * @param location location whose {@link ILocationProvider#toLong()} is its cache key
     */
    public final void addExtraLocation(ILocationProvider location) {
        currentDimCache.put(location.toLong(), location);
    }

    /** @return shared logical button */
    public ButtonManager getButtonManager() {
        return buttonManager;
    }

    /**
     * @param map map integration
     * @return renderer registered for that integration, or {@code null}
     */
    public LayerRenderer getLayerRenderer(SupportedMods map) {
        return layerRenderer.get(map);
    }

    /**
     * @return manager-owned set of locations visible in the latest combined viewport
     */
    public Collection<ILocationProvider> getVisibleLocations() {
        return visibleLocations;
    }

    /** @return manager-owned cached locations for the current dimension */
    public Collection<? extends ILocationProvider> getCachedLocations() {
        return getCurrentDimCache().values();
    }

    /**
     * Returns or initializes the current dimension cache.
     *
     * @return mutable location map keyed by stable location identity
     */
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

    /** Schedules the current dimension's locations and render steps for clearing. */
    public void clearCurrentCache() {
        clearCurrent = true;
        forceRefresh();
    }

    /** Schedules every dimension's locations and render steps for clearing. */
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
     * Enables or disables Navigator's search field for this layer.
     *
     * @param hasSearchField whether the layer accepts search text
     */
    protected void setHasSearchField(boolean hasSearchField) {
        this.hasSearchField = hasSearchField;
    }

    /** @return whether this layer requests the map search field */
    public boolean hasSearchField() {
        return hasSearchField;
    }

    /**
     * Receives search text changes from a supported fullscreen map.
     *
     * @param searchString current non-null search text
     */
    public void onSearch(@NotNull String searchString) {}

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
