package com.gtnewhorizons.navigator.api.model.steps;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

/** Visual representation derived from one cached {@link ILocationProvider}. */
public interface RenderStep {

    /**
     * @return source location, or {@code null} for legacy implementations that do not expose one
     */
    default ILocationProvider getLocation() {
        return null;
    }
}
