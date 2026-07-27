package com.gtnewhorizons.navigator.api.model.steps;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;

/**
 * Universal interactable step for locations that support Navigator waypoints.
 * <p>
 * {@link com.gtnewhorizons.navigator.api.model.layers.UniversalInteractableRenderer} supplies default double-click
 * set/clear waypoint behavior for this step. Use {@link UniversalLocationInteractableStep} for domain interaction
 * without waypoints.
 *
 * @param <T> waypoint-capable location type
 */
public abstract class UniversalInteractableStep<T extends IWaypointAndLocationProvider>
    extends UniversalLocationInteractableStep<T> implements XaeroInteractableStep {

    public UniversalInteractableStep(T location) {
        super(location);
    }
}
