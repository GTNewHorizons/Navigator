package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

public abstract class LayerRenderer {

    protected final LayerManager manager;
    private final SupportedMods mod;
    protected List<? extends RenderStep> renderSteps = new ArrayList<>();

    public LayerRenderer(LayerManager manager, SupportedMods mod) {
        manager.registerLayerRenderer(mod, this);
        this.mod = mod;
        this.manager = manager;
    }

    public abstract void updateVisibleElements(List<? extends ILocationProvider> visibleElements);

    protected abstract List<? extends RenderStep> generateRenderSteps(
        List<? extends ILocationProvider> visibleElements);

    public final boolean isLayerActive() {
        return manager.isLayerActive() && mod.isEnabled();
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
}
