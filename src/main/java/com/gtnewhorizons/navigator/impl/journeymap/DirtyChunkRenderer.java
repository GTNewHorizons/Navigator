package com.gtnewhorizons.navigator.impl.journeymap;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMDrawStep;
import com.gtnewhorizons.navigator.api.journeymap.render.JMLayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class DirtyChunkRenderer extends JMLayerRenderer {

    public static final DirtyChunkRenderer instance = new DirtyChunkRenderer();

    public DirtyChunkRenderer() {
        super(DirtyChunkLayerManager.instance);
    }

    @Override
    protected List<? extends RenderStep> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<JMDrawStep> drawSteps = new ArrayList<>();
        visibleElements.stream()
            .map(element -> (DirtyChunkLocation) element)
            .forEach(location -> drawSteps.add(new DirtyChunkDrawStep(location)));
        return drawSteps;
    }
}
