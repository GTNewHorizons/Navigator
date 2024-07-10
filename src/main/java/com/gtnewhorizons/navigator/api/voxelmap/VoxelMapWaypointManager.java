package com.gtnewhorizons.navigator.api.voxelmap;

import java.lang.reflect.Method;
import java.util.TreeSet;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.Utils;
import com.thevoxelbox.voxelmap.interfaces.AbstractVoxelMap;
import com.thevoxelbox.voxelmap.interfaces.IWaypointManager;
import com.thevoxelbox.voxelmap.util.Waypoint;

public class VoxelMapWaypointManager {

    private static Method getCurrentSubworldDescriptor;

    public static void addVoxelMapWaypoint(Waypoint waypoint) {
        if (!Utils.isVoxelMapInstalled()) return;
        IWaypointManager waypointManager = AbstractVoxelMap.getInstance()
            .getWaypointManager();
        waypointManager.addWaypoint(waypoint);
    }

    public static void addVoxelMapWaypoint(String name, int x, int y, int z, boolean enabled, float red, float green,
        float blue, String icon, String world, TreeSet<Integer> dimension) {
        if (!Utils.isVoxelMapInstalled()) return;
        addVoxelMapWaypoint(new Waypoint(name, x, y, z, enabled, red, green, blue, icon, world, dimension));
    }

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
