package com.gtnewhorizons.navigator.api.model.steps;

import java.util.List;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.util.Util;

/**
 * Interaction contract for any {@link com.gtnewhorizons.navigator.api.model.locations.ILocationProvider}.
 * <p>
 * This is the location-neutral alternative to the legacy waypoint-bound {@link InteractableStep}.
 */
public interface LocationInteractableStep extends RenderStep {

    /**
     * Appends tooltip lines for this step.
     *
     * @param list mutable tooltip supplied by Navigator
     */
    void getTooltip(List<String> list);

    /** Called when Navigator's shared action key is pressed while this step is hovered. */
    void onActionKeyPressed();

    /**
     * Handles a raw key press. The default implementation dispatches {@link NavigatorApi#ACTION_KEY}.
     *
     * @param keyCode LWJGL key code
     * @return {@code true} when the key was consumed
     */
    default boolean onKeyPressed(int keyCode) {
        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY)) {
            onActionKeyPressed();
            return true;
        }
        return false;
    }
}
