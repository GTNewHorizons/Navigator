package com.gtnewhorizons.navigator.api.model.steps;

import java.util.List;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;

public interface InteractableStep extends RenderStep {

    void getTooltip(List<String> list);

    void onActionKeyPressed();

    @Override
    default IWaypointAndLocationProvider getLocation() {
        return getLocationProvider();
    }

    /**
     * @deprecated Use {@link #getLocation()} instead
     */
    @Deprecated
    default IWaypointAndLocationProvider getLocationProvider() {
        return null;
    }
}
