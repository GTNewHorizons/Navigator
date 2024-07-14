package com.gtnewhorizons.navigator.mixins.late.xaerosworldmap;

import static com.gtnewhorizons.navigator.api.model.SupportedMods.XaeroWorldMap;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.buttons.SizedGuiTexturedButton;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroInteractableLayerRenderer;
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
        NavigatorApi.layerManagers.forEach(layerManager -> layerManager.onGuiOpened(XaeroWorldMap));
    }

    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPushMatrix()V", ordinal = 1, remap = false),
        remap = true)
    private void navigator$injectPreRender(int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
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
            if (layer instanceof XaeroInteractableLayerRenderer interactableLayer) {
                interactableLayer.updateHovered(mousePosX, mousePosZ, scale);
            }
        }
    }

    @Inject(
        method = "drawScreen",
        at = @At(value = "INVOKE", target = "Lxaero/map/mods/SupportMods;minimap()Z", ordinal = 1, remap = false),
        remap = true)
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

        XaeroLayerRenderer renderer = (XaeroLayerRenderer) NavigatorApi.getActiveLayerFor(XaeroWorldMap);
        if (renderer != null) {
            for (XaeroRenderStep step : renderer.getRenderSteps()) {
                step.draw(this, cameraX, cameraZ, scale);
            }
        }
    }

    @Inject(
        method = "drawScreen",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;",
            shift = At.Shift.AFTER),
        remap = true)
    private void navigator$injectDrawTooltip(int scaledMouseX, int scaledMouseY, float partialTicks, CallbackInfo ci) {
        LayerRenderer layer = NavigatorApi.getActiveLayerFor(XaeroWorldMap);
        if (layer instanceof XaeroInteractableLayerRenderer interactableLayer) {
            List<String> tooltip = interactableLayer.getTooltip();
            if (!tooltip.isEmpty()) {
                DrawUtils
                    .drawSimpleTooltip(this, tooltip, scaledMouseX + 16, scaledMouseY - 12, 0xFFFFFFFF, 0x86000000);
            } else {
                interactableLayer.drawCustomTooltip(this, scaledMouseX, scaledMouseY, scale, screenScale);
            }
        }
    }

    @Inject(method = "initGui", at = @At(value = "TAIL"), remap = true)
    private void navigator$injectInitButtons(CallbackInfo ci) {
        List<ButtonManager> buttons = NavigatorApi.buttonManagers;
        int numBtns = buttons.size();
        int totalHeight = numBtns * 20;
        for (int i = 0; i < numBtns; i++) {
            ButtonManager btnManager = buttons.get(i);
            if (!btnManager.isEnabled(XaeroWorldMap)) continue;
            SizedGuiTexturedButton button = new SizedGuiTexturedButton(
                0,
                (height / 2 + totalHeight / 2) - 20 - 20 * i,
                btnManager.getIcon(XaeroWorldMap, ""),
                (btn) -> btnManager.toggle(),
                new CursorBox(btnManager.getButtonText()));
            btnManager.setOnToggle(button::setActive);
            button.setActive(btnManager.isActive());
            addGuiButton(button);
        }
    }

    @Inject(
        method = "onInputPress",
        at = @At(
            value = "INVOKE",
            target = "Lxaero/map/misc/Misc;inputMatchesKeyBinding(ZILnet/minecraft/client/settings/KeyBinding;)Z",
            ordinal = 1),
        cancellable = true)
    private void navigator$injectListenKeypress(boolean mouse, int code, CallbackInfoReturnable<Boolean> cir) {
        LayerRenderer activeLayer = NavigatorApi.getActiveLayerFor(XaeroWorldMap);
        if (activeLayer instanceof XaeroInteractableLayerRenderer interactableLayer
            && interactableLayer.onKeyPressed(code)) {
            cir.setReturnValue(true);
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

            LayerRenderer layer = NavigatorApi.getActiveLayerFor(XaeroWorldMap);
            if (layer instanceof XaeroInteractableLayerRenderer interactableLayer) {
                interactableLayer.onMapClick(isDoubleClick, x, y, mouseBlockPosX, mouseBlockPosZ);
            }
        }
    }

    @Inject(method = "onGuiClosed", at = @At("RETURN"), remap = true)
    private void navigator$onGuiClosed(CallbackInfo ci) {
        NavigatorApi.layerManagers.forEach(layerManager -> layerManager.onGuiClosed(XaeroWorldMap));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
