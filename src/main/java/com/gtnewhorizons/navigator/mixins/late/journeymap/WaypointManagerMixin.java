package com.gtnewhorizons.navigator.mixins.late.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;

import journeymap.client.ui.waypoint.WaypointManager;

@Mixin(WaypointManager.class)
public abstract class WaypointManagerMixin {

    @Inject(method = "toggleItems", at = @At("HEAD"), remap = false, require = 1)
    private void navigator$onToggleAllWaypoints(boolean enable, CallbackInfoReturnable<Boolean> cir) {
        if (!enable) {
            for (LayerManager layer : NavigatorApi.layerManagers) {
                if (layer instanceof InteractableLayerManager waypointProvider) {
                    waypointProvider.clearActiveWaypoint();
                }
            }
        }
    }
}
