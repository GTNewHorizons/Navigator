package com.gtnewhorizons.navigator.impl.xaero;

import com.gtnewhorizons.navigator.api.xaero.waypoints.XaeroWaypointManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;

public class XaeroDirtyChunkWaypointManager extends XaeroWaypointManager {

    public static final XaeroDirtyChunkWaypointManager instance = new XaeroDirtyChunkWaypointManager();

    public XaeroDirtyChunkWaypointManager() {
        super(DirtyChunkLayerManager.instance);
    }
}
