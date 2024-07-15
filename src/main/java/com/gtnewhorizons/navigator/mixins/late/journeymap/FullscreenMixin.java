package com.gtnewhorizons.navigator.mixins.late.journeymap;

import static com.gtnewhorizons.navigator.api.model.SupportedMods.JourneyMap;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Mouse;
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
import com.gtnewhorizons.navigator.api.journeymap.render.JMInteractableLayerRenderer;
import com.gtnewhorizons.navigator.api.journeymap.render.JMLayerRenderer;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.llamalad7.mixinextras.sugar.Local;

import journeymap.client.io.ThemeFileHandler;
import journeymap.client.log.LogFormatter;
import journeymap.client.log.StatTimer;
import journeymap.client.model.BlockCoordIntPair;
import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.UIManager;
import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.fullscreen.MapChat;
import journeymap.client.ui.fullscreen.layer.LayerDelegate;
import journeymap.client.ui.theme.Theme;
import journeymap.client.ui.theme.ThemeButton;
import journeymap.client.ui.theme.ThemeToggle;
import journeymap.client.ui.theme.ThemeToolbar;

@Mixin(value = Fullscreen.class, remap = false)
public abstract class FullscreenMixin extends JmUI {

    @Unique
    private int navigator$oldMouseX = 0;
    @Unique
    private int navigator$oldMouseY = 0;
    @Unique
    private long navigator$timeLastClick = 0;

    @Final
    @Shadow
    static GridRenderer gridRenderer;

    @Shadow
    ThemeToolbar mapTypeToolbar;

    @Shadow
    ThemeButton buttonCaves;

    @Shadow
    ThemeButton buttonNight;

    @Shadow
    ThemeButton buttonDay;

    @Shadow
    StatTimer drawScreenTimer;

    @Shadow()
    MapChat chat;

    @Final
    @Shadow
    LayerDelegate layerDelegate;

    @Shadow
    boolean firstLayoutPass;

    @Shadow
    int mx;

    @Shadow
    int my;

    public FullscreenMixin() {
        super("");
    }

    @Inject(method = "<init>*", at = @At("RETURN"), require = 1)
    private void navigator$init(CallbackInfo ci) {
        NavigatorApi.getEnabledLayers(JourneyMap)
            .forEach(LayerManager::forceRefresh);
    }

    @Shadow
    protected abstract int getMapFontScale();

    @Shadow
    void drawMap() {
        throw new IllegalStateException("Mixin failed to shadow drawMap()");
    }

    @Shadow
    public abstract void drawBackground(int layer);

    @Inject(method = "<init>*", at = @At("RETURN"), require = 1)
    private void navigator$onConstructed(CallbackInfo ci) {
        NavigatorApi.getEnabledLayers(JourneyMap)
            .forEach(layerManager -> layerManager.onGuiOpened(JourneyMap));
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
            if (layerManager.isLayerActive()) {
                layerManager.recacheFullscreenMap(centerBlockX, centerBlockZ, widthBlocks, heightBlocks);
            }
        }

        for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
            if (layer instanceof JMLayerRenderer jmLayer) {
                gridRenderer.draw(jmLayer.getRenderSteps(), xOffset, yOffset, drawScale, fontScale, 0.0);
            }
        }
    }

    @Redirect(
        method = "initButtons",
        at = @At(
            value = "FIELD",
            target = "Ljourneymap/client/ui/fullscreen/Fullscreen;mapTypeToolbar:Ljourneymap/client/ui/theme/ThemeToolbar;",
            opcode = Opcodes.PUTFIELD),
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

    @Override
    public void drawScreen(int width, int height, float f) {
        try {
            drawBackground(0);
            drawMap();
            drawScreenTimer.start();
            layoutButtons();
            List<String> tooltip = null;
            if (firstLayoutPass) {
                layoutButtons();
                firstLayoutPass = false;
            } else {
                for (GuiButton guibutton : buttonList) {
                    guibutton.drawButton(mc, width, height);
                    if (tooltip == null && guibutton instanceof Button button) {
                        if (button.mouseOver(mx, my)) {
                            tooltip = button.getTooltip();
                        }
                    }
                }
            }

            final int scaledMouseX = (mx * mc.displayWidth) / this.width;
            final int scaledMouseY = (my * mc.displayHeight) / this.height;
            for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
                if (layer instanceof JMInteractableLayerRenderer waypointProviderLayer) {
                    waypointProviderLayer.onMouseMove(scaledMouseX, scaledMouseY);
                    if (tooltip == null) {
                        tooltip = waypointProviderLayer.getTooltip();
                    }
                }
            }

            if (chat != null) {
                chat.drawScreen(width, height, f);
            }

            if (tooltip != null && !tooltip.isEmpty()) {
                drawHoveringText(tooltip, mx, my, getFontRenderer());
                RenderHelper.disableStandardItemLighting();
            } else {
                for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
                    if (layer instanceof JMInteractableLayerRenderer waypointProviderLayer) {
                        waypointProviderLayer.drawCustomTooltip(getFontRenderer(), mx, my, this.width, this.height);
                    }
                }
            }

        } catch (Throwable var11) {
            logger.log(
                Level.ERROR,
                "Unexpected exception in jm.fullscreen.drawScreen(): " + LogFormatter.toString(var11));
            UIManager.getInstance()
                .closeAll();
        } finally {
            drawScreenTimer.stop();
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "HEAD"), remap = true, require = 1, cancellable = true)
    private void navigator$onKeyPress(CallbackInfo ci, @Local(argsOnly = true) int keyCode) {
        if ((chat == null || chat.isHidden())) {
            for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
                if (layer instanceof JMInteractableLayerRenderer waypointProvider) {
                    if (waypointProvider.onKeyPressed(keyCode)) {
                        ci.cancel();
                    }
                }
            }
        }
    }

    @Inject(method = "onGuiClosed", at = @At("RETURN"), remap = true)
    private void navigator$onGuiClosed(CallbackInfo ci) {
        NavigatorApi.getEnabledLayers(JourneyMap)
            .forEach(layerManager -> layerManager.onGuiClosed(JourneyMap));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (chat != null && !chat.isHidden()) {
            chat.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.isMouseOverButton(mouseX, mouseY)) {
            final int scaledMouseX = mx * mc.displayWidth / width;
            final int scaledMouseY = my * mc.displayHeight / height;
            BlockCoordIntPair blockCoord = gridRenderer
                .getBlockUnderMouse(Mouse.getEventX(), Mouse.getEventY(), mc.displayWidth, mc.displayHeight);
            if (!navigator$onMapClicked(mouseButton, scaledMouseX, scaledMouseY, blockCoord)) {
                layerDelegate.onMouseClicked(
                    mc,
                    Mouse.getEventX(),
                    Mouse.getEventY(),
                    gridRenderer.getWidth(),
                    gridRenderer.getHeight(),
                    blockCoord,
                    mouseButton);
            }
        }
    }

    @Unique
    private boolean navigator$onMapClicked(int mouseButton, int mouseX, int mouseY, BlockCoordIntPair blockCoord) {
        final long timestamp = System.currentTimeMillis();
        final boolean isDoubleClick = mouseX == navigator$oldMouseX && mouseY == navigator$oldMouseY
            && timestamp - navigator$timeLastClick < 500;
        navigator$oldMouseX = mouseX;
        navigator$oldMouseY = mouseY;
        navigator$timeLastClick = isDoubleClick ? 0 : timestamp;
        if (mouseButton != 0) {
            return false;
        }
        for (LayerRenderer layer : NavigatorApi.getActiveRenderersFor(JourneyMap)) {
            if (layer instanceof JMInteractableLayerRenderer waypointProviderLayer) {
                return waypointProviderLayer.onMapClick(isDoubleClick, mouseX, mouseY, blockCoord.x, blockCoord.z);
            }
        }
        return false;
    }
}
