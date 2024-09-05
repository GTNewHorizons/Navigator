package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class LayerRenderer {

    protected final LayerManager manager;
    private final SupportedMods mod;
    protected List<? extends RenderStep> renderSteps = new ArrayList<>();
    protected final Int2ObjectMap<Long2ObjectMap<RenderStep>> dimCachedRenderSteps = new Int2ObjectOpenHashMap<>();
    protected Long2ObjectMap<RenderStep> currentDimSteps;
    private final List<RenderStep> visibleSteps = new ArrayList<>();

    public LayerRenderer(LayerManager manager, SupportedMods mod) {
        this.mod = mod;
        this.manager = manager;
    }

    public void refreshVisibleElements(Set<ILocationProvider> locations) {
        visibleSteps.clear();
        for (ILocationProvider location : locations) {
            RenderStep step = getOrCreateRenderStep(location);

            if (step == null) continue;
            visibleSteps.add(step);
        }
        renderSteps = visibleSteps;
    }

    private RenderStep getOrCreateRenderStep(ILocationProvider location) {
        long key = location.toLong();

        RenderStep renderStep = currentDimSteps.get(key);
        if (renderStep != null) {
            return renderStep;
        }

        renderStep = generateRenderStep(location);
        if (renderStep != null) {
            return currentDimSteps.put(key, renderStep);
        }

        List<? extends RenderStep> renderSteps = generateRenderSteps(Collections.singletonList(location));
        if (renderSteps != null) {
            for (RenderStep step : renderSteps) {
                return currentDimSteps.put(key, step);
            }
        }
        return null;
    }

    /**
     * @param location The location to generate a {@link RenderStep} for
     * @return A {@link RenderStep} for the given location, or null if none should be generated
     */
    protected @Nullable RenderStep generateRenderStep(ILocationProvider location) {
        return null;
    }

    public final SupportedMods getLayerMod() {
        return mod;
    }

    public List<? extends RenderStep> getRenderStepsForInteraction() {
        return renderSteps;
    }

    public List<? extends RenderStep> getRenderSteps() {
        return renderSteps;
    }

    public List<? extends RenderStep> getReversedRenderSteps() {
        List<RenderStep> reversed = new ArrayList<>(renderSteps);
        Collections.reverse(reversed);
        return reversed;
    }

    void removeRenderStep(long key) {
        RenderStep renderStep = currentDimSteps.remove(key);
        renderSteps.remove(renderStep);
    }

    void setDimCache(int dim) {
        currentDimSteps = dimCachedRenderSteps.computeIfAbsent(dim, k -> new Long2ObjectOpenHashMap<>());
    }

    void clearCurrentCache() {
        if (currentDimSteps != null) {
            currentDimSteps.clear();
        }
        visibleSteps.clear();
        renderSteps.clear();
    }

    void clearFullCache() {
        currentDimSteps = null;
        clearCurrentCache();
        dimCachedRenderSteps.clear();
    }

    public int getRenderPriority() {
        return 0;
    }

    @Deprecated
    public void updateVisibleElements(List<? extends ILocationProvider> visibleElements) {}

    /**
     * @deprecated Use {@link #generateRenderStep(ILocationProvider)} to generate a single RenderStep
     */
    @Deprecated
    protected List<? extends RenderStep> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        return null;
    }
}
