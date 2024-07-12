package com.gtnewhorizons.navigator.impl.journeymap;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.journeymap.render.JMInteractableLayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class JMDirtyChunkRenderer extends JMInteractableLayerRenderer {

    public static final JMDirtyChunkRenderer instance = new JMDirtyChunkRenderer();

    public JMDirtyChunkRenderer() {
        super(DirtyChunkLayerManager.instance);
    }

    @Override
    protected List<? extends RenderStep> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<JMRenderStep> drawSteps = new ArrayList<>();
        visibleElements.stream()
            .map(element -> (DirtyChunkLocation) element)
            .forEach(location -> drawSteps.add(new JMDirtyChunkRenderStep(location)));
        return drawSteps;
    }
}
