package com.gtnewhorizons.navigator.api.model.steps;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

public interface RenderStep {

    default ILocationProvider getLocation() {
        return null;
    }
}
