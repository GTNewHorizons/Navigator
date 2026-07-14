package com.gtnewhorizons.navigator.api.model.locations;

import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;

/**
 * Optional capability for locations that can become Navigator's active waypoint.
 * <p>
 * Plain clickable locations should implement only {@link ILocationProvider} and use a location-neutral interactable
 * render step. Do not return {@code null} from {@link #toWaypoint()} to emulate another double-click action.
 */
public interface IWaypointAndLocationProvider extends ILocationProvider {

    /** @return a complete waypoint representing this location */
    Waypoint toWaypoint();

    /** @return whether this location currently represents the manager's active waypoint */
    boolean isActiveAsWaypoint();

    /** Called when the manager clears its active waypoint. */
    void onWaypointCleared();

    /**
     * Called when the manager sets or synchronizes an active waypoint.
     *
     * @param waypoint current active waypoint
     */
    void onWaypointUpdated(Waypoint waypoint);
}
