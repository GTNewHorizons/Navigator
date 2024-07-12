package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

public abstract class JMLayerRenderer extends LayerRenderer {

    public JMLayerRenderer(LayerManager manager) {
        super(manager, SupportedMods.JourneyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JMRenderStep> getRenderSteps() {
        return (List<JMRenderStep>) renderSteps;
    }

    @Override
    public void updateVisibleElements(List<? extends ILocationProvider> visibleElements) {
        renderSteps = generateRenderSteps(visibleElements);
    }
}
