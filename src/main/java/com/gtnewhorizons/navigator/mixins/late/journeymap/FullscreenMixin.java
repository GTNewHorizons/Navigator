package com.gtnewhorizons.navigator.mixins.late.journeymap;

import static com.gtnewhorizons.navigator.api.model.SupportedMods.JourneyMap;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.render.JMLayerRenderer;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayer;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalLayerRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.fullscreen.MapChat;
import journeymap.client.ui.fullscreen.layer.LayerDelegate;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeButton;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;

@Mixin(Fullscreen.class)
public abstract class FullscreenMixin extends JmUI {

    @Unique
    private int navigator$oldMouseX = 0;
    @Unique
    private int navigator$oldMouseY = 0;
    @Unique
    private long navigator$timeLastClick = 0;

    @Unique
    private int navigator$oldCenterX = 0;
    @Unique
    private int navigator$oldCenterZ = 0;
    @Unique
    private int navigator$oldWidth = 0;
    @Unique
    private int navigator$oldHeight = 0;

    @Unique
    private long navigator$lastRecache = 0;

    @Final
    @Shadow(remap = false)
    static GridRenderer gridRenderer;

    @Shadow(remap = false)
    ThemeToolbar mapTypeToolbar;

    @Shadow(remap = false)
    ThemeButton buttonCaves;

    @Shadow(remap = false)
    ThemeButton buttonNight;

    @Shadow(remap = false)
    ThemeButton buttonDay;

    @Shadow(remap = false)
    MapChat chat;

    @Shadow(remap = false)
    int mx;

    @Shadow(remap = false)
    int my;

    public FullscreenMixin() {
        super("");
    }

    @Shadow(remap = false)
    protected abstract int getMapFontScale();

    @Inject(method = "initGui", at = @At("RETURN"), require = 1)
    private void navigator$onInit(CallbackInfo ci) {
        NavigatorApi.getEnabledLayers(JourneyMap)
            .forEach(layerManager -> {
                layerManager.onGuiOpened(JourneyMap);
                layerManager.forceRefresh();
            });
    }

    @Inject(
        method = "drawMap",
        at = @At(value = "INVOKE", target = "Ljourneymap/client/model/MapState;getDrawWaypointSteps()Ljava/util/List;"),
        remap = false,
        require = 1,
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void navigator$onBeforeDrawJourneyMapWaypoints(CallbackInfo ci, boolean refreshReady, StatTimer timer,
        int xOffset, int yOffset, float drawScale) {
        final int fontScale = getMapFontScale();
        final Minecraft minecraft = Minecraft.getMinecraft();
        final int centerBlockX = (int) Math.round(gridRenderer.getCenterBlockX());
        final int centerBlockZ = (int) Math.round(gridRenderer.getCenterBlockZ());
        final int widthBlocks = minecraft.displayWidth >> gridRenderer.getZoom();
        final int heightBlocks = minecraft.displayHeight >> gridRenderer.getZoom();
        for (LayerManager layerManager : NavigatorApi.getEnabledLayers(JourneyMap)) {
            if (navigator$shouldRecache(centerBlockX, centerBlockZ, widthBlocks, heightBlocks, layerManager)) {
                layerManager.recacheFullscreenMap(centerBlockX, centerBlockZ, widthBlocks, heightBlocks);
            }
        }

        for (LayerRenderer layer : NavigatorApi.getActiveRenderersByPriority(JourneyMap)) {
            if (layer instanceof JMLayerRenderer jmLayer) {
                gridRenderer.draw(jmLayer.getRenderSteps(), xOffset, yOffset, drawScale, fontScale, 0.0);
            } else if (layer instanceof UniversalLayerRenderer universalLayer) {
                gridRenderer.draw(universalLayer.getRenderSteps(), xOffset, yOffset, drawScale, fontScale, 0.0);
            }
        }
    }

    @Redirect(
        method = "initButtons",
        at = @At(
            value = "FIELD",
            target = "Ljourneymap/client/ui/fullscreen/Fullscreen;mapTypeToolbar:Ljourneymap/client/ui/theme/ThemeToolbar;",
            opcode = Opcodes.PUTFIELD),
        remap = false,
        require = 1)
    private void navigator$OnCreateMapTypeToolbar(Fullscreen owner, ThemeToolbar value) {
        final Theme theme = ThemeFileHandler.getCurrentTheme();
        final ButtonList buttonList = new ButtonList();

        for (ButtonManager btnManager : NavigatorApi.getEnabledButtons(JourneyMap)) {
            String icon = btnManager.getIcon(JourneyMap, theme.name)
                .toString();
            String trimmedIcon = icon.substring(0, icon.lastIndexOf("."));
            final ThemeToggle button = new ThemeToggle(theme, "", "", trimmedIcon);
            btnManager.setOnToggle((toggled) -> button.setToggled(toggled, false));
            button.setLabels(btnManager.getButtonText(), btnManager.getButtonText());
            button.setToggled(btnManager.isActive(), false);
            button.addToggleListener((unused, toggled) -> {
                btnManager.toggle();
                return true;
            });
            buttonList.add(button);
        }

        buttonList.add(buttonCaves);
        buttonList.add(buttonNight);
        buttonList.add(buttonDay);
        mapTypeToolbar = new ThemeToolbar(theme, buttonList);
    }

    @Inject(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderHelper;disableStandardItemLighting()V",
            shift = At.Shift.BY,
            by = 2))
    private void navigator$drawCustomTooltip(CallbackInfo ci, @Local List<String> tooltip) {
        if (tooltip == null || tooltip.isEmpty()) {
            for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
                if (layer instanceof InteractableLayer interactableLayer) {
                    interactableLayer.drawCustomTooltip(getFontRenderer(), mx, my, this.width, this.height);
                }
            }
        }
    }

    @Inject(
        method = "drawScreen",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Ljourneymap/client/ui/fullscreen/Fullscreen;chat:Ljourneymap/client/ui/fullscreen/MapChat;",
            ordinal = 0,
            shift = At.Shift.BEFORE,
            remap = false))
    private void navigator$getLayerTooltip(CallbackInfo ci, @Local LocalRef<List<String>> tooltip) {
        final int scaledMouseX = (mx * mc.displayWidth) / this.width;
        final int scaledMouseY = (my * mc.displayHeight) / this.height;
        for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
            if (layer instanceof InteractableLayer waypointProviderLayer) {
                waypointProviderLayer.onMouseMove(scaledMouseX, scaledMouseY);
                if (tooltip.get() == null) {
                    tooltip.set(waypointProviderLayer.getTooltip());
                }
            }
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "HEAD"), require = 1, cancellable = true)
    private void navigator$onKeyPress(CallbackInfo ci, @Local(argsOnly = true) int keyCode) {
        if ((chat == null || chat.isHidden())) {
            for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
                if (layer instanceof InteractableLayer waypointProvider) {
                    if (waypointProvider.onKeyPressed(keyCode)) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onGuiClosed", at = @At("RETURN"))
    private void navigator$onGuiClosed(CallbackInfo ci) {
        NavigatorApi.getEnabledLayers(JourneyMap)
            .forEach(layerManager -> layerManager.onGuiClosed(JourneyMap));
    }

    @WrapOperation(
        method = "mouseClicked",
        at = @At(
            value = "INVOKE",
            target = "Ljourneymap/client/ui/fullscreen/layer/LayerDelegate;onMouseClicked(Lnet/minecraft/client/Minecraft;DDIILjourneymap/client/model/BlockCoordIntPair;I)V",
            remap = false))
    private void navigator$mouseClicked(LayerDelegate instance, Minecraft mc, double mouseX, double mouseY,
        int gridWidth, int gridHeight, BlockCoordIntPair clickedBlock, int mouseButton, Operation<Void> original) {
        int scaledMouseX = mx * mc.displayWidth / width;
        int scaledMouseY = my * mc.displayHeight / height;
        if (!navigator$onMapClicked(mouseButton, scaledMouseX, scaledMouseY, clickedBlock)) {
            original.call(instance, mc, mouseX, mouseY, gridWidth, gridHeight, clickedBlock, mouseButton);
        }
    }

    @Unique
    private boolean navigator$onMapClicked(int mouseButton, int mouseX, int mouseY, BlockCoordIntPair blockCoord) {
        if (mouseButton != 0) return false;
        final long timestamp = System.currentTimeMillis();
        final boolean isDoubleClick = mouseX == navigator$oldMouseX && mouseY == navigator$oldMouseY
            && timestamp - navigator$timeLastClick < 250L;
        navigator$oldMouseX = mouseX;
        navigator$oldMouseY = mouseY;
        navigator$timeLastClick = timestamp;
        for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
            if (layer instanceof InteractableLayer waypointProviderLayer) {
                return waypointProviderLayer.onMapClick(isDoubleClick, mouseX, mouseY, blockCoord.x, blockCoord.z);
            }
        }
        return false;
    }

    @Unique
    private boolean navigator$shouldRecache(int centerX, int centerZ, int width, int height, LayerManager manager) {
        if (!manager.isLayerActive()) return false;
        long now = System.currentTimeMillis();
        if (navigator$oldCenterX != centerX || navigator$oldCenterZ != centerZ
            || navigator$oldWidth != width
            || navigator$oldHeight != height
            || manager.forceRefresh
            || now - navigator$lastRecache >= 1000L) {
            navigator$oldCenterX = centerX;
            navigator$oldCenterZ = centerZ;
            navigator$oldWidth = width;
            navigator$oldHeight = height;
            navigator$lastRecache = now;
            return true;
        }
        return false;
    }
}
