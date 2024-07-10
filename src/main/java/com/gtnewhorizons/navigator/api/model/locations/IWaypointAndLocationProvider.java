package com.gtnewhorizons.navigator.api.model.locations;

import com.gtnewhorizons.navigator.api.model.waypoints.Waypoint;

public interface IWaypointAndLocationProvider extends ILocationProvider {

    Waypoint toWaypoint();

    boolean isActiveAsWaypoint();

    void onWaypointCleared();

    void onWaypointUpdated(Waypoint waypoint);
}
