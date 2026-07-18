package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Converts a manager's cached locations into cached render steps for one map integration.
 * <p>
 * A render step is reused while its location identity remains cached. Override
 * {@link #generateRenderStep(ILocationProvider)} for new implementations.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class LayerRenderer {

    protected final LayerManager manager;
    private final SupportedMods mod;
    protected List<? extends RenderStep> renderSteps = new ArrayList<>();
    protected final Int2ObjectMap<Long2ObjectMap<RenderStep>> dimCachedRenderSteps = new Int2ObjectOpenHashMap<>();
    protected Long2ObjectMap<RenderStep> currentDimSteps;
    private final List<RenderStep> visibleSteps = new ArrayList<>();

    /**
     * @param manager owning layer manager
     * @param mod     map integration this renderer targets, or {@link SupportedMods#NONE} for a universal renderer
     */
    public LayerRenderer(@Nonnull LayerManager manager, SupportedMods mod) {
        this.mod = mod;
        this.manager = manager;
    }

    /**
     * Rebuilds the visible step list while reusing cached steps.
     *
     * @param locations currently visible locations
     */
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
            currentDimSteps.put(key, renderStep);
            return renderStep;
        }

        List<? extends RenderStep> renderSteps = generateRenderSteps(Collections.singletonList(location));
        if (renderSteps != null) {
            for (RenderStep step : renderSteps) {
                currentDimSteps.put(key, step);
                return step;
            }
        }
        return null;
    }

    /**
     * Creates a cached render step for one location.
     *
     * @param location The location to generate a {@link RenderStep} for
     * @return A {@link RenderStep} for the given location, or null if none should be generated
     */
    protected @Nullable RenderStep generateRenderStep(ILocationProvider location) {
        return null;
    }

    /** @return map integration declared by this renderer */
    public final SupportedMods getLayerMod() {
        return mod;
    }

    /** @return steps in the order used for hit testing */
    public List<? extends RenderStep> getRenderStepsForInteraction() {
        return renderSteps;
    }

    /** @return currently visible render steps */
    public List<? extends RenderStep> getRenderSteps() {
        return renderSteps;
    }

    /** @return a new list containing visible render steps in reverse order */
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

    /**
     * Controls ordering relative to other active layer renderers.
     *
     * @return ascending render priority; defaults to {@code 0}
     */
    public int getRenderPriority() {
        return 0;
    }

    /**
     * @deprecated Visible elements are refreshed internally from {@link LayerManager}.
     */
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
