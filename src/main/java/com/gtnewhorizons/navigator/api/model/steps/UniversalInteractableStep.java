package com.gtnewhorizons.navigator.api.model.steps;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;

public abstract class UniversalInteractableStep<T extends IWaypointAndLocationProvider>
    extends UniversalLocationInteractableStep<T> implements XaeroInteractableStep {

    public UniversalInteractableStep(T location) {
        super(location);
    }
}
