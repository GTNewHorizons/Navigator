package com.gtnewhorizons.navigator.mixins.late.journeymap;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.waypoints.JMWaypointManager;
import com.gtnewhorizons.navigator.api.model.waypoints.WaypointManager;

import journeymap.client.model.Waypoint;
import journeymap.client.render.ingame.RenderWaypointBeacon;

@Mixin(RenderWaypointBeacon.class)
public abstract class RenderWaypointBeaconMixin {

    @Shadow(remap = false)
    static Minecraft mc;

    @Shadow(remap = false)
    static void doRender(Waypoint waypoint) {
        throw new IllegalStateException("Mixin failed to shadow doRender()");
    }

    @Inject(
        method = "renderAll",
        at = @At(
            value = "INVOKE",
            target = "Ljourneymap/client/waypoint/WaypointStore;instance()Ljourneymap/client/waypoint/WaypointStore;"),
        remap = false,
        require = 1)
    private static void visualprospecting$onRenderAll(CallbackInfo ci) {
        for (WaypointManager waypointManager : NavigatorApi.waypointManagers) {
            if (waypointManager.hasWaypoint() && waypointManager instanceof JMWaypointManager jmWaypointManager) {
                final Waypoint waypoint = jmWaypointManager.getJmWaypoint();
                if (waypoint.getDimensions()
                    .contains(mc.thePlayer.dimension)) {
                    doRender(waypoint);
                }
            }
        }
    }
}
