package com.gtnewhorizons.navigator.impl.journeymap;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.journeymap.render.JMInteractableLayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class JMDirtyChunkRenderer extends JMInteractableLayerRenderer {

    public JMDirtyChunkRenderer(InteractableLayerManager manager) {
        super(manager);
    }

    @Nullable
    @Override
    protected RenderStep generateRenderStep(ILocationProvider location) {
        return new JMDirtyChunkRenderStep((DirtyChunkLocation) location);
    }
}
