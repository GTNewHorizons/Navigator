package com.gtnewhorizons.navigator.api.xaero.renderers;

import java.util.List;

import javax.annotation.Nonnull;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

public abstract class XaeroLayerRenderer extends LayerRenderer {

    public XaeroLayerRenderer(@Nonnull LayerManager manager) {
        super(manager, SupportedMods.XaeroWorldMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XaeroRenderStep> getRenderSteps() {
        return (List<XaeroRenderStep>) renderSteps;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<XaeroRenderStep> getReversedRenderSteps() {
        return (List<XaeroRenderStep>) super.getReversedRenderSteps();
    }
}
