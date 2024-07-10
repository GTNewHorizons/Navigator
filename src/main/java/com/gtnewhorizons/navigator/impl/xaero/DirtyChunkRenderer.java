package com.gtnewhorizons.navigator.impl.xaero;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class DirtyChunkRenderer extends XaeroLayerRenderer {

    public static final DirtyChunkRenderer instance = new DirtyChunkRenderer();

    public DirtyChunkRenderer() {
        super(DirtyChunkLayerManager.instance);
    }

    @Override
    protected List<DirtyChunkRenderSteps> generateRenderSteps(List<? extends ILocationProvider> visibleElements) {
        final List<DirtyChunkRenderSteps> renderSteps = new ArrayList<>();
        visibleElements.stream()
            .map(element -> (DirtyChunkLocation) element)
            .forEach(location -> renderSteps.add(new DirtyChunkRenderSteps(location)));
        return renderSteps;
    }

}
