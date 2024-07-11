package com.gtnewhorizons.navigator.impl.journeymap;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMDrawStep;
import com.gtnewhorizons.navigator.api.journeymap.render.WaypointProviderLayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class JMDirtyChunkRenderer extends WaypointProviderLayerRenderer {

    public static final JMDirtyChunkRenderer instance = new JMDirtyChunkRenderer();

    public JMDirtyChunkRenderer() {
        super(DirtyChunkLayerManager.instance);
    }

    @Override
    protected List<? extends RenderStep> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<JMDrawStep> drawSteps = new ArrayList<>();
        visibleElements.stream()
            .map(element -> (DirtyChunkLocation) element)
            .forEach(location -> drawSteps.add(new JMDirtyChunkDrawStep(location)));
        return drawSteps;
    }
}
