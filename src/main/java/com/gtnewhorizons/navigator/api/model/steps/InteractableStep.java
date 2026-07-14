package com.gtnewhorizons.navigator.api.model.steps;

import java.util.List;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.util.Util;

/**
 * Legacy interaction contract whose location is always waypoint-capable.
 * <p>
 * New non-waypoint layers should use {@link LocationInteractableStep}. This interface remains for source and binary
 * compatibility and for layers that intentionally use the default double-click waypoint behavior.
 */
public interface InteractableStep extends LocationInteractableStep {

    void getTooltip(List<String> list);

    void onActionKeyPressed();

    default boolean onKeyPressed(int keyCode) {
        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY)) {
            onActionKeyPressed();
            return true;
        }
        return false;
    }

    @Override
    default IWaypointAndLocationProvider getLocation() {
        return getLocationProvider();
    }

    /**
     * Legacy location accessor retained for old implementations.
     *
     * @deprecated Use {@link #getLocation()} instead
     */
    @Deprecated
    default IWaypointAndLocationProvider getLocationProvider() {
        return null;
    }
}
