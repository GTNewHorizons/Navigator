package com.gtnewhorizons.navigator.internal.journeymap.v6;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayer;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalLayerRenderer;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;
import com.gtnewhorizons.navigator.api.util.DrawUtils;

import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.event.DisplayUpdateEvent;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.event.FullscreenMapEvent;
import journeymap.api.v2.client.event.FullscreenRenderEvent;
import journeymap.api.v2.client.fullscreen.IFullscreen;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import journeymap.api.v2.client.util.UIState;
import journeymap.api.v2.common.Context;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.event.ClientEventRegistry;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import journeymap.api.v2.common.util.BlockPos;

@JourneyMapPlugin(apiVersion = IClientAPI.API_VERSION, dependencies = Navigator.MODID)
public final class JourneyMapV6Plugin implements IClientPlugin {

    private static final SupportedMods MOD = SupportedMods.JourneyMap;

    private static IClientAPI api;
    private static IFullscreen fullscreen;

    private boolean fullscreenActive;
    private long lastRecache;
    private int oldCenterX = Integer.MIN_VALUE;
    private int oldCenterZ = Integer.MIN_VALUE;
    private int oldWidth = -1;
    private int oldHeight = -1;
    private long timeLastClick;
    private int oldMouseX;
    private int oldMouseY;
    private ButtonManager lastPressedButton;
    private long lastButtonPress;

    @Override
    public String getModId() {
        return Navigator.MODID;
    }

    @Override
    public void initialize(IClientAPI clientApi) {
        api = clientApi;
        ClientEventRegistry.DISPLAY_UPDATE_EVENT.subscribe(Navigator.MODID, this::onDisplayUpdate);
        FullscreenEventRegistry.FULLSCREEN_RENDER_EVENT.subscribe(Navigator.MODID, this::onRender);
        FullscreenEventRegistry.FULLSCREEN_MAP_CLICK_EVENT.subscribe(Navigator.MODID, this::onClick);
        FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(Navigator.MODID, this::onButtons);
    }

    public static @Nullable IClientAPI getApi() {
        return api;
    }

    public static void centerOn(int blockX, int blockZ) {
        if (fullscreen != null && fullscreen.getUiState().active) {
            fullscreen.centerOn(blockX, blockZ);
        }
    }

    private void onDisplayUpdate(DisplayUpdateEvent event) {
        if (event.uiState.ui != Context.UI.Fullscreen || event.uiState.active == fullscreenActive) return;

        fullscreenActive = event.uiState.active;
        for (LayerManager manager : NavigatorApi.getEnabledLayers(MOD)) {
            if (fullscreenActive) {
                manager.onGuiOpened(MOD);
                manager.forceRefresh();
            } else {
                manager.onGuiClosed(MOD);
            }
        }
        if (!fullscreenActive) fullscreen = null;
    }

    private void onButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
        fullscreen = event.getFullscreen();
        for (ButtonManager manager : NavigatorApi.getEnabledButtons(MOD)) {
            IThemeButton button = event.getThemeButtonDisplay()
                .addThemeToggleButton(
                    manager.getButtonText(),
                    manager.getIcon(MOD, ""),
                    manager.isActive(),
                    ignored -> toggleButton(manager));
            manager.setOnToggle(button::setToggled);
        }
    }

    private void toggleButton(ButtonManager manager) {
        long now = System.nanoTime();
        // JM 6.0.0-beta.1 adds addon buttons to the fullscreen twice and invokes both entries on one click.
        if (manager == lastPressedButton && now - lastButtonPress < 50_000_000L) return;

        lastPressedButton = manager;
        lastButtonPress = now;
        manager.toggle();
    }

    private void onRender(FullscreenRenderEvent event) {
        fullscreen = event.getFullscreen();
        UIState state = fullscreen.getUiState();
        if (!state.active || state.blockBounds == null || state.displayBounds == null || state.blockSize <= 0) return;

        int centerX = (int) Math.round(fullscreen.getCenterBlockX(true));
        int centerZ = (int) Math.round(fullscreen.getCenterBlockZ(true));
        int width = (int) Math.ceil(state.blockBounds.maxX - state.blockBounds.minX);
        int height = (int) Math.ceil(state.blockBounds.maxZ - state.blockBounds.minZ);
        recache(centerX, centerZ, width, height);

        Minecraft minecraft = fullscreen.getMinecraft();
        int guiScale = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight)
            .getScaleFactor();
        double blockSize = state.blockSize / guiScale;
        double mapCenterX = fullscreen.getCenterBlockX(true);
        double mapCenterZ = fullscreen.getCenterBlockZ(true);
        double zoomStep = Math.log(state.blockSize) / Math.log(2.0);
        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersByPriority(MOD)) {
            if (!(renderer instanceof UniversalLayerRenderer)) continue;
            for (UniversalRenderStep<?> step : ((UniversalLayerRenderer) renderer).getRenderSteps()) {
                double x = state.displayBounds.getCenterX() / guiScale + (step.getLocation()
                    .getBlockX() - mapCenterX) * blockSize;
                double y = state.displayBounds.getCenterY() / guiScale + (step.getLocation()
                    .getBlockZ() - mapCenterZ) * blockSize;
                step.drawJourneyMap(x, y, 1.0F / guiScale, zoomStep, blockSize, 1.0, 0.0);
            }
        }

        drawTooltip(event);
    }

    private void recache(int centerX, int centerZ, int width, int height) {
        long now = System.currentTimeMillis();
        boolean viewportChanged = oldCenterX != centerX || oldCenterZ != centerZ
            || oldWidth != width
            || oldHeight != height;
        for (LayerManager manager : NavigatorApi.getEnabledLayers(MOD)) {
            if (manager.isLayerActive() && (viewportChanged || manager.forceRefresh || now - lastRecache >= 1000L)) {
                manager.recacheFullscreenMap(centerX, centerZ, width, height);
            }
        }
        if (viewportChanged || now - lastRecache >= 1000L) {
            oldCenterX = centerX;
            oldCenterZ = centerZ;
            oldWidth = width;
            oldHeight = height;
            lastRecache = now;
        }
    }

    private void drawTooltip(FullscreenRenderEvent event) {
        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersFor(MOD)) {
            if (!(renderer instanceof InteractableLayer interactable)) continue;

            interactable.onMouseMove(event.getMouseX(), event.getMouseY());
            List<String> tooltip = interactable.getTooltip();
            if (!tooltip.isEmpty()) {
                DrawUtils.drawSimpleTooltip(
                    event.getFullscreen()
                        .getScreen(),
                    tooltip,
                    event.getMouseX() + 16,
                    event.getMouseY() - 12,
                    0xFFFFFFFF,
                    0x86000000);
            } else {
                interactable.drawCustomTooltip(
                    event.getFullscreen()
                        .getMinecraft().fontRenderer,
                    event.getMouseX(),
                    event.getMouseY(),
                    event.getFullscreen()
                        .getScreen().width,
                    event.getFullscreen()
                        .getScreen().height);
            }
            return;
        }
    }

    private void onClick(FullscreenMapEvent.ClickEvent event) {
        if (event.getStage() != FullscreenMapEvent.Stage.PRE || event.getButton() != 0) return;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        long now = System.currentTimeMillis();
        boolean doubleClick = mouseX == oldMouseX && mouseY == oldMouseY && now - timeLastClick < 250L;
        oldMouseX = mouseX;
        oldMouseY = mouseY;
        timeLastClick = now;

        BlockPos location = event.getLocation();
        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersFor(MOD)) {
            if (renderer instanceof InteractableLayer interactable
                && interactable.onMapClick(doubleClick, mouseX, mouseY, location.getX(), location.getZ())) {
                event.cancel();
                return;
            }
        }
    }
}
