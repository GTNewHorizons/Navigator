package com.gtnewhorizons.navigator.mixins.late.journeymap;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.minimap.DisplayVars;
import journeymap.client.ui.minimap.MiniMap;
import journeymap.client.ui.minimap.Shape;

@Mixin(MiniMap.class)
public abstract class MiniMapMixin {

    @Final
    @Shadow(remap = false)
    private static GridRenderer gridRenderer;

    @Final
    @Shadow(remap = false)
    private Minecraft mc;

    @Shadow(remap = false)
    private DisplayVars dv;

    @Inject(method = "drawOnMapWaypoints", at = @At(value = "HEAD"), remap = false, require = 1)
    private void navigator$onBeforeDrawWaypoints(double rotation, CallbackInfo ci) {
        for (LayerManager layerManager : NavigatorApi.layerManagers) {
            if (layerManager.isLayerActive()) {
                if (((DisplayVarsAccessor) dv).getShape() == Shape.Circle) {
                    layerManager.recacheMiniMap(
                        (int) mc.thePlayer.posX,
                        (int) mc.thePlayer.posZ,
                        ((DisplayVarsAccessor) dv).getMinimapWidth());
                } else {
                    layerManager.recacheMiniMap(
                        (int) mc.thePlayer.posX,
                        (int) mc.thePlayer.posZ,
                        gridRenderer.getWidth(),
                        gridRenderer.getHeight());
                }
            }
        }

        for (LayerRenderer layerRenderer : NavigatorApi.getJourneyMapLayerRenderers()) {
            if (layerRenderer.isMinimapActive()) {
                for (RenderStep renderStep : layerRenderer.getRenderSteps()) {
                    if (renderStep instanceof DrawStep drawStep) {
                        drawStep.draw(
                            0.0D,
                            0.0D,
                            gridRenderer,
                            ((DisplayVarsAccessor) dv).getDrawScale(),
                            ((DisplayVarsAccessor) dv).getFontScale(),
                            rotation);
                    }
                }
            }
        }
    }
}
