package com.gtnewhorizons.navigator.impl.xaero;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroInteractableLayerRenderer;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class XaeroDirtyChunkRenderer extends XaeroInteractableLayerRenderer {

    public static final XaeroDirtyChunkRenderer instance = new XaeroDirtyChunkRenderer();

    public XaeroDirtyChunkRenderer() {
        super(DirtyChunkLayerManager.instance);
    }

    @Override
    protected List<XaeroDirtyChunkRenderSteps> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<XaeroDirtyChunkRenderSteps> renderSteps = new ArrayList<>();
        visibleElements.stream()
            .map(element -> (DirtyChunkLocation) element)
            .forEach(location -> renderSteps.add(new XaeroDirtyChunkRenderSteps(location)));
        return renderSteps;
    }
}
