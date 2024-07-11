package com.gtnewhorizons.navigator.impl.journeymap;

import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;

public class JMDirtyChunkWaypointManager extends JMWaypointManager {

    public static final JMDirtyChunkWaypointManager instance = new JMDirtyChunkWaypointManager();

    public JMDirtyChunkWaypointManager() {
        super(DirtyChunkLayerManager.instance);
    }
}
