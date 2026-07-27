package com.gtnewhorizons.navigator.api.voxelmap;

import java.lang.reflect.Method;
import java.util.TreeSet;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.util.Util;
import com.thevoxelbox.voxelmap.interfaces.AbstractVoxelMap;
import com.thevoxelbox.voxelmap.interfaces.IWaypointManager;
import com.thevoxelbox.voxelmap.util.Waypoint;

/**
 * Optional VoxelMap waypoint helpers.
 * <p>
 * Navigator does not provide VoxelMap layer rendering. Calls are safe no-ops when VoxelMap is absent.
 */
public class VoxelMapWaypointManager {

    private static Method getCurrentSubworldDescriptor;

    /** Adds an already-created VoxelMap waypoint when VoxelMap is installed. */
    public static void addVoxelMapWaypoint(Waypoint waypoint) {
        if (!Util.isVoxelMapInstalled()) return;
        IWaypointManager waypointManager = AbstractVoxelMap.getInstance()
            .getWaypointManager();
        waypointManager.addWaypoint(waypoint);
    }

    /** Creates and adds a VoxelMap waypoint when VoxelMap is installed. */
    public static void addVoxelMapWaypoint(String name, int x, int y, int z, boolean enabled, float red, float green,
        float blue, String icon, TreeSet<Integer> dimension) {
        if (!Util.isVoxelMapInstalled()) return;
        IWaypointManager waypointManager = AbstractVoxelMap.getInstance()
            .getWaypointManager();
        addVoxelMapWaypoint(
            new Waypoint(
                name,
                x,
                y,
                z,
                enabled,
                red,
                green,
                blue,
                icon,
                getCurrentSubworldDescriptor(waypointManager, false),
                dimension));
    }

    /**
     * Invokes VoxelMap's obfuscated current-subworld method.
     *
     * @return current subworld descriptor, or an empty string if reflection failed
     */
    public static String getCurrentSubworldDescriptor(IWaypointManager obj, boolean arg) {
        try {
            return (String) getCurrentSubworldDescriptor.invoke(obj, arg);
        } catch (Exception e) {
            Navigator.LOG.error(
                "Could not invoke IWaypointManager#if. If it failed due to a NullPointerException, look for an error message starting with \"Getting the method IWaypointManager#if failed\" further up.");
            e.printStackTrace();
        }
        return "";
    }

    static {
        try {
            getCurrentSubworldDescriptor = IWaypointManager.class.getMethod("if", boolean.class);
        } catch (Exception e) {
            Navigator.LOG.error(
                "Getting the method IWaypointManager#if failed, any calls to IWaypointManagerReflection#getCurrentSubworldDescriptor will return an empty String.");
            e.printStackTrace();
        }
    }
}
