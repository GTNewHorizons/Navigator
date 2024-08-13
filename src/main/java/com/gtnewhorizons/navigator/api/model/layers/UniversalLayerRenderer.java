package com.gtnewhorizons.navigator.api.model.layers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalInteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;

public class UniversalLayerRenderer extends LayerRenderer {

    private Class<? extends UniversalRenderStep<?>> renderStepClass;
    private Class<?> locationClass;

    public UniversalLayerRenderer(LayerManager manager) {
        super(manager, SupportedMods.NONE);
    }

    public UniversalLayerRenderer(LayerManager manager, Class<? extends UniversalInteractableStep<?>> renderStepClass,
        Class<?> location) {
        super(manager, SupportedMods.NONE);
        this.renderStepClass = renderStepClass;
        this.locationClass = location;
    }

    public UniversalLayerRenderer withLocation(@Nonnull Class<?> location) {
        this.locationClass = location;
        return this;
    }

    public UniversalLayerRenderer withRenderStep(@Nonnull Class<? extends UniversalRenderStep<?>> step) {
        this.renderStepClass = step;
        return this;
    }

    @Nullable
    @Override
    protected RenderStep generateRenderStep(ILocationProvider location) {
        try {
            return renderStepClass.getDeclaredConstructor(locationClass)
                .newInstance(location);
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public List<UniversalRenderStep<?>> getRenderSteps() {
        return (List<UniversalRenderStep<?>>) super.getRenderSteps();
    }
}
