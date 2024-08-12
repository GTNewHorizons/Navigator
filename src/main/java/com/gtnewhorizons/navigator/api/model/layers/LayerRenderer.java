package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

@SuppressWarnings("DeprecatedIsStillUsed")
public abstract class LayerRenderer {

    protected final LayerManager manager;
    private final SupportedMods mod;
    protected List<? extends RenderStep> renderSteps = new ArrayList<>();
    protected final Long2ObjectMap<RenderStep> cachedRenderSteps = new Long2ObjectOpenHashMap<>();
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

        RenderStep renderStep = cachedRenderSteps.get(key);
        if (renderStep != null) {
            return renderStep;
        }

        renderStep = generateRenderStep(location);
        if (renderStep != null) {
            return cachedRenderSteps.put(key, renderStep);
        }

        List<? extends RenderStep> renderSteps = generateRenderSteps(Collections.singletonList(location));
        if (renderSteps != null) {
            for (RenderStep step : renderSteps) {
                return cachedRenderSteps.put(key, step);
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
        RenderStep renderStep = cachedRenderSteps.remove(key);
        renderSteps.remove(renderStep);
    }

    void clearRenderSteps() {
        cachedRenderSteps.clear();
        renderSteps.clear();
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
