package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;

public abstract class JMLayerRenderer extends LayerRenderer {

    public JMLayerRenderer(LayerManager manager) {
        super(manager, SupportedMods.JourneyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JMRenderStep> getRenderSteps() {
        return (List<JMRenderStep>) renderSteps;
    }
}
