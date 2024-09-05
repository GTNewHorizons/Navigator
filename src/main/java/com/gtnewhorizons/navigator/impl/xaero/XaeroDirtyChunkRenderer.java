package com.gtnewhorizons.navigator.impl.xaero;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroInteractableLayerRenderer;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class XaeroDirtyChunkRenderer extends XaeroInteractableLayerRenderer {

    public XaeroDirtyChunkRenderer(InteractableLayerManager manager) {
        super(manager);
    }

    @Nullable
    @Override
    protected RenderStep generateRenderStep(ILocationProvider location) {
        return new XaeroDirtyChunkRenderSteps((DirtyChunkLocation) location);
    }
}
