package com.gtnewhorizons.navigator.mixins.late.xaerosminimap;

import static com.gtnewhorizons.navigator.api.model.SupportedMods.XaeroWorldMap;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;
import com.llamalad7.mixinextras.sugar.Local;

import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapRenderer;

@Mixin(value = MinimapRenderer.class, remap = false)
public abstract class MinimapRendererMixin {

    @Unique
    private boolean navigator$stencilEnabled = true;

    @Shadow
    protected Minecraft mc;

    @Shadow
    protected double zoom;

    @Inject(
        method = "renderMinimap",
        at = @At(
            value = "INVOKE",
            target = "Lxaero/common/minimap/waypoints/render/WaypointsGuiRenderer;render(Lxaero/common/XaeroMinimapSession;Lxaero/common/minimap/render/MinimapRendererHelper;DDIIDDFDZFZ)V"))
    private void navigator$injectDraw(XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x, int y,
        int width, int height, int scale, int size, float partial, CallbackInfo ci,
        @Local(name = "circleShape") boolean circleShape, @Local(name = "minimapFrameSize") int minimapFrameSize,
        @Local(name = "angle") double angle, @Local(name = "minimapScale") float minimapScale) {
        if (mc.currentScreen != null) return;
        for (LayerManager layerManager : NavigatorApi.getEnabledLayers(XaeroWorldMap)) {
            if (layerManager.isLayerActive()) {
                if (circleShape) {
                    layerManager.recacheMiniMap((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, minimapFrameSize * 2);
                } else {
                    layerManager.recacheMiniMap(
                        (int) mc.thePlayer.posX,
                        (int) mc.thePlayer.posZ,
                        minimapFrameSize * 2,
                        minimapFrameSize * 2);
                }
            }
        }

        if (navigator$stencilEnabled) {
            double mapZoom = zoom * (double) minimapScale / 2.0;
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glRotated(Math.toDegrees(angle) - 90, 0.0, 0.0, 1.0);
            GL11.glScaled(mapZoom, mapZoom, 0);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);

            for (LayerRenderer layerRenderer : NavigatorApi.getActiveRenderersByPriority(XaeroWorldMap)) {
                for (XaeroRenderStep renderStep : ((XaeroLayerRenderer) layerRenderer).getRenderSteps()) {
                    renderStep.draw(null, minimap.mainPlayerX, minimap.mainPlayerZ, mapZoom);
                }
            }

            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glPopMatrix();
        }
    }

    @Inject(
        method = "renderMinimap",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V", shift = At.Shift.AFTER),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawTexturedElipseInsideRectangle(IFFIIFF)V")))
    private void navigator$injectBeginStencil(XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x,
        int y, int width, int height, int scale, int size, float partial, CallbackInfo ci) {
        if (mc.currentScreen != null) return;
        if (navigator$stencilEnabled && MinecraftForgeClient.getStencilBits() == 0) {
            navigator$stencilEnabled = false;
            Navigator.LOG.warn("Could not enable stencils! Xaero's minimap overlays will not render");
        }
        // if stencil is not enabled, this code will do nothing
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    @Inject(
        method = "renderMinimap",
        at = @At(value = "INVOKE", target = "Lxaero/common/minimap/MinimapInterface;usingFBO()Z"),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawTexturedElipseInsideRectangle(IFFIIFF)V")))
    private void navigator$injectEndStencil(XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x, int y,
        int width, int height, int scale, int size, float partial, CallbackInfo ci) {
        if (mc.currentScreen != null) return;
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
