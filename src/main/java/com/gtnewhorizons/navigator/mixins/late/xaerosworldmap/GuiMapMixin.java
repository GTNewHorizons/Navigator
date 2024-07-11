package com.gtnewhorizons.navigator.mixins.late.xaerosworldmap;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.buttons.SizedGuiTexturedButton;
import com.gtnewhorizons.navigator.api.xaero.buttons.XaeroLayerButton;
import com.gtnewhorizons.navigator.api.xaero.renderers.InteractableLayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

import xaero.map.MapProcessor;
import xaero.map.gui.CursorBox;
import xaero.map.gui.GuiMap;
import xaero.map.gui.ScreenBase;
import xaero.map.misc.Misc;

@Mixin(value = GuiMap.class, remap = false)
public abstract class GuiMapMixin extends ScreenBase {

    @Unique
    private int navigator$oldMouseX = 0;

    @Unique
    private int navigator$oldMouseY = 0;

    @Unique
    private long navigator$timeLastClick = 0;

    protected GuiMapMixin(GuiScreen parent, GuiScreen escape) {
        super(parent, escape);
    }

    @Shadow
    private double cameraX;

    @Shadow
    private double cameraZ;

    @Shadow
    private double scale;

    @Shadow
    public abstract void addGuiButton(GuiButton b);

    @Shadow
    private int screenScale;

    @Shadow
    private int mouseBlockPosX;

    @Shadow
    private int mouseBlockPosZ;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void navigator$injectConstruct(GuiScreen parent, GuiScreen escape, MapProcessor mapProcessor, Entity player,
        CallbackInfo ci) {
        NavigatorApi.layerManagers.forEach(LayerManager::onOpenMap);
    }

    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V", ordinal = 1),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void navigator$injectPreRender(int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci,
        Minecraft mc) {
        // snap the camera to whole pixel values. works around a rendering issue but causes another when framerate is
        // uncapped
        if (mc.gameSettings.limitFramerate < 255 || mc.gameSettings.enableVsync) {
            cameraX = Math.round(cameraX * scale) / scale;
            cameraZ = Math.round(cameraZ * scale) / scale;
        }

        // there's some nice local variables for exactly this but the local table for this function is hell
        double mousePosX = (Misc.getMouseX(mc) - (double) mc.displayWidth / 2) / this.scale;
        double mousePosZ = (Misc.getMouseY(mc) - (double) mc.displayHeight / 2) / this.scale;

        for (LayerRenderer layer : NavigatorApi.layerRenderers) {
            if (layer instanceof InteractableLayerRenderer interactableLayer) {
                interactableLayer.updateHovered(mousePosX, mousePosZ, scale);
            }
        }
    }

    // deobf method = "drawScreen"
    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V", ordinal = 1, shift = At.Shift.AFTER),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL14;glBlendFuncSeparate(IIII)V"),
            to = @At(
                value = "INVOKE",
                target = "Lxaero/map/mods/SupportXaeroMinimap;renderWaypoints(Lnet/minecraft/client/gui/GuiScreen;DDIIDDDDLjava/util/regex/Pattern;Ljava/util/regex/Pattern;FLxaero/map/mods/gui/Waypoint;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/ScaledResolution;)Lxaero/map/mods/gui/Waypoint;")))
    private void navigator$injectDraw(int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        for (LayerManager layerManager : NavigatorApi.layerManagers) {
            if (layerManager.isLayerActive()) {
                // +20s are to work around precision loss from casting to int and right-shifting
                layerManager.recacheFullscreenMap(
                    (int) cameraX,
                    (int) cameraZ,
                    (int) (mc.displayWidth / scale) + 20,
                    (int) (mc.displayHeight / scale) + 20);
            }
        }

        for (XaeroLayerRenderer renderer : NavigatorApi.getXaeroLayerRenderers()) {
            if (renderer.isLayerActive()) {
                for (XaeroRenderStep step : renderer.getRenderSteps()) {
                    step.draw(this, cameraX, cameraZ, scale);
                }
            }
        }
    }

    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslated(DDD)V"),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
                opcode = Opcodes.GETFIELD),
            to = @At(value = "INVOKE", target = "Lxaero/map/gui/CursorBox;drawBox(IIII)V")))
    private void navigator$injectDrawTooltip(int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        for (XaeroLayerRenderer layer : NavigatorApi.getXaeroLayerRenderers()) {
            if (layer instanceof InteractableLayerRenderer interactableLayer && layer.isLayerActive()) {
                interactableLayer.drawTooltip(this, scale, screenScale);
            }
        }
    }

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;enableRepeatEvents(Z)V"))
    private void navigator$injectInitButtons(CallbackInfo ci) {
        List<XaeroLayerButton> buttons = NavigatorApi.getXaeroButtons();
        int numBtns = buttons.size();
        int totalHeight = numBtns * 20;
        for (int i = 0; i < numBtns; i++) {
            XaeroLayerButton layerButton = buttons.get(i);
            if (!layerButton.isEnabled()) continue;

            SizedGuiTexturedButton button = new SizedGuiTexturedButton(
                0,
                (height / 2 + totalHeight / 2) - 20 - 20 * i,
                layerButton.textureLocation,
                (btn) -> layerButton.toggle(),
                new CursorBox(layerButton.getButtonTextKey()));
            layerButton.setButton(button);
            addGuiButton(button);
        }
    }

    @Inject(method = "onInputPress", at = @At("TAIL"))
    private void navigator$injectListenKeypress(boolean mouse, int code, CallbackInfoReturnable<Boolean> cir) {
        LayerRenderer activeLayer = NavigatorApi.getActiveLayer();
        if (activeLayer instanceof InteractableLayerRenderer interactableLayer
            && Misc.inputMatchesKeyBinding(mouse, code, interactableLayer.getActionKey())) {
            interactableLayer.doActionKeyPress();
        }
    }

    @Inject(method = "mapClicked", at = @At("TAIL"))
    private void navigator$injectListenClick(int button, int x, int y, CallbackInfo ci) {
        if (button == 0) {
            final long timestamp = System.currentTimeMillis();
            final boolean isDoubleClick = x == navigator$oldMouseX && y == navigator$oldMouseY
                && timestamp - navigator$timeLastClick < 500;
            navigator$oldMouseX = x;
            navigator$oldMouseY = y;
            navigator$timeLastClick = isDoubleClick ? 0 : timestamp;

            LayerRenderer layer = NavigatorApi.getActiveLayer();
            if (layer instanceof InteractableLayerRenderer interactableLayer) {
                interactableLayer.onClick(isDoubleClick, x, y, mouseBlockPosX, mouseBlockPosZ);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
