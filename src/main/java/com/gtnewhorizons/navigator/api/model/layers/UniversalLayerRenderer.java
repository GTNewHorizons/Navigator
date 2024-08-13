package com.gtnewhorizons.navigator.api.model.layers;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;

public class UniversalLayerRenderer extends LayerRenderer {

    private Function<ILocationProvider, UniversalRenderStep<?>> stepCreator;
    private int renderPriority = 0;

    public UniversalLayerRenderer(LayerManager manager) {
        super(manager, SupportedMods.NONE);
    }

    public UniversalLayerRenderer withRenderStep(
        @Nonnull Function<ILocationProvider, UniversalRenderStep<?>> supplier) {
        this.stepCreator = supplier;
        return this;
    }

    public UniversalLayerRenderer withRenderPriority(int renderPriority) {
        this.renderPriority = renderPriority;
        return this;
    }

    @Nullable
    @Override
    protected RenderStep generateRenderStep(ILocationProvider location) {
        if (stepCreator != null) {
            return stepCreator.apply(location);
        }

        return null;
    }

    @Override
    public int getRenderPriority() {
        return renderPriority;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UniversalRenderStep<?>> getRenderSteps() {
        return (List<UniversalRenderStep<?>>) super.getRenderSteps();
    }
}
