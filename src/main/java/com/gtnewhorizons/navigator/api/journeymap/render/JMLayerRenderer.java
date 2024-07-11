package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMDrawStep;
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
    public List<JMDrawStep> getRenderSteps() {
        return (List<JMDrawStep>) renderSteps;
    }

    @Override
    public void updateVisibleElements(List<? extends ILocationProvider> visibleElements) {
        renderSteps = generateRenderSteps(visibleElements);
    }
}
