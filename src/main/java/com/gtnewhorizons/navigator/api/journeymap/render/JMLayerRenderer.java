package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import javax.annotation.Nonnull;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;

/**
 * JourneyMap 5-specific renderer.
 *
 * @deprecated Use {@link com.gtnewhorizons.navigator.api.model.layers.UniversalLayerRenderer}. This class is retained
 *             for JourneyMap 5-only integrations.
 */
@Deprecated
public abstract class JMLayerRenderer extends LayerRenderer {

    public JMLayerRenderer(@Nonnull LayerManager manager) {
        super(manager, SupportedMods.JourneyMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JMRenderStep> getRenderSteps() {
        return (List<JMRenderStep>) renderSteps;
    }
}
