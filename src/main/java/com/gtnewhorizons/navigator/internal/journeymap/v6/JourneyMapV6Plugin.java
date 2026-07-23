package com.gtnewhorizons.navigator.internal.journeymap.v6;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.event.LayerRefreshEvent;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayer;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.model.layers.LayerRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalInteractableRenderer;
import com.gtnewhorizons.navigator.api.model.layers.UniversalLayerRenderer;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.model.markers.MapMarker;
import com.gtnewhorizons.navigator.api.model.steps.UniversalLocationInteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.internal.SearchBar;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.Displayable;
import journeymap.api.v2.client.display.IOverlayListener;
import journeymap.api.v2.client.display.MarkerOverlay;
import journeymap.api.v2.client.display.Overlay;
import journeymap.api.v2.client.event.DisplayUpdateEvent;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.event.FullscreenMapEvent;
import journeymap.api.v2.client.event.FullscreenRenderEvent;
import journeymap.api.v2.client.fullscreen.IFullscreen;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import journeymap.api.v2.client.model.MapImage;
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
    private static @Nullable SearchBar searchBar;

    private final Map<UniversalLayerRenderer, Map<ILocationProvider, List<Displayable>>> overlays = new IdentityHashMap<>();
    private final Map<MarkerOverlay, MapMarker> markerProperties = new IdentityHashMap<>();
    private final Set<LayerManager> dirtyLayers = Collections
        .synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
    private @Nullable OverlayListener hoveredOverlay;
    private boolean actionKeyDown;
    private boolean fullscreenActive;
    private @Nullable Context.UI overlayUi;
    private int overlayDimension = Integer.MIN_VALUE;
    private int overlayCenterX = Integer.MIN_VALUE;
    private int overlayCenterZ = Integer.MIN_VALUE;
    private int overlayWidth = -1;
    private int overlayHeight = -1;
    private long lastRecache;
    private int oldCenterX = Integer.MIN_VALUE;
    private int oldCenterZ = Integer.MIN_VALUE;
    private int oldWidth = -1;
    private int oldHeight = -1;
    private long timeLastClick;
    private int searchScreenWidth = -1;
    private int searchScreenHeight = -1;
    private int lastMarkerZoom = Integer.MIN_VALUE;

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
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        dirtyLayers.addAll(NavigatorApi.getEnabledLayers(MOD));
    }

    public static @Nullable IClientAPI getApi() {
        return api;
    }

    public static void centerOn(int blockX, int blockZ) {
        if (fullscreen != null && fullscreen.getUiState().active) {
            fullscreen.centerOn(blockX, blockZ);
        }
    }

    public static boolean onSearchKeyTyped(char typedChar, int keyCode) {
        return searchBar != null && searchBar.getVisible() && searchBar.textboxKeyTyped(typedChar, keyCode);
    }

    public static boolean isSearchFocused() {
        return searchBar != null && searchBar.getVisible() && searchBar.isFocused();
    }

    public static void onChatOpened() {
        if (searchBar != null) searchBar.setFocused(false);
    }

    public static boolean onSearchMouseClicked(int mouseX, int mouseY, int button) {
        if (searchBar == null || !searchBar.getVisible()) return false;

        searchBar.mouseClicked(mouseX, mouseY, button);
        return searchBar.isHovered(mouseX, mouseY);
    }

    private void onDisplayUpdate(DisplayUpdateEvent event) {
        if (event.uiState.ui == Context.UI.Fullscreen && event.uiState.active != fullscreenActive) {
            fullscreenActive = event.uiState.active;
            lastMarkerZoom = Integer.MIN_VALUE;
            for (LayerManager manager : NavigatorApi.getEnabledLayers(MOD)) {
                if (fullscreenActive) {
                    manager.onGuiOpened(MOD);
                    manager.forceRefresh();
                } else {
                    manager.onGuiClosed(MOD);
                }
            }
            if (!fullscreenActive) {
                resetMarkerScales();
                fullscreen = null;
                searchBar = null;
            }
        }

        UIState state = getActiveMapState();
        if (state != null) syncVisibleOverlays(state);
    }

    private void onButtons(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
        fullscreen = event.getFullscreen();
        for (ButtonManager manager : NavigatorApi.getEnabledButtons(MOD)) {
            IThemeButton button = event.getThemeButtonDisplay()
                .addThemeToggleButton(
                    manager.getButtonText(),
                    manager.getIcon(MOD, ""),
                    manager.isActive(),
                    ignored -> manager.toggle());
            manager.setOnToggle(button::setToggled);
        }
    }

    private void onRender(FullscreenRenderEvent event) {
        fullscreen = event.getFullscreen();
        UIState state = fullscreen.getUiState();
        if (!state.active || state.blockBounds == null || state.displayBounds == null || state.blockSize <= 0) return;
        if (state.zoom != lastMarkerZoom) {
            updateMarkerScales(state);
            lastMarkerZoom = state.zoom;
        }

        int centerX = (int) Math.round(fullscreen.getCenterBlockX(true));
        int centerZ = (int) Math.round(fullscreen.getCenterBlockZ(true));
        int width = (int) Math.ceil(state.blockBounds.maxX - state.blockBounds.minX);
        int height = (int) Math.ceil(state.blockBounds.maxZ - state.blockBounds.minZ);
        recache(centerX, centerZ, width, height);

        Minecraft minecraft = fullscreen.getMinecraft();
        int guiScale = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight)
            .getScaleFactor();
        double blockSize = state.blockSize / guiScale;
        double zoomStep = Math.log(state.blockSize) / Math.log(2.0);
        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersByPriority(MOD)) {
            if (!(renderer instanceof UniversalLayerRenderer universal) || universal.hasMapMarker()
                || universal.journeyMapV6OverlaysReplaceRenderSteps()) {
                continue;
            }
            for (UniversalRenderStep<?> step : universal.getRenderSteps()) {
                Point2D.Double pixel = getBlockPixel(
                    step.getLocation()
                        .getBlockX(),
                    step.getLocation()
                        .getBlockZ());
                step.drawJourneyMap(
                    pixel.x / guiScale,
                    pixel.y / guiScale,
                    1.0F / guiScale,
                    zoomStep,
                    blockSize,
                    1.0 / guiScale,
                    0.0);
            }
        }

        drawSearchBar(event);
        drawTooltip(event);
    }

    /** Mirrors JourneyMap's pixel-snapped grid position using public fullscreen state. */
    private Point2D.Double getBlockPixel(double blockX, double blockZ) {
        double blockSize = fullscreen.getUiState().blockSize;
        double centerX = fullscreen.getCenterBlockX(false);
        double centerZ = fullscreen.getCenterBlockZ(false);
        Minecraft minecraft = fullscreen.getMinecraft();
        double x = minecraft.displayWidth / 2 + (snapToScreenPixel(blockX, blockSize) - centerX) * blockSize;
        double y = minecraft.displayHeight / 2 + (snapToScreenPixel(blockZ, blockSize) - centerZ) * blockSize;

        double dragX = centerX - fullscreen.getCenterBlockX(true);
        double dragZ = centerZ - fullscreen.getCenterBlockZ(true);
        if (dragX != 0 || dragZ != 0) {
            x += (centerX - snapToScreenPixel(centerX - dragX, blockSize)) * blockSize;
            y += (centerZ - snapToScreenPixel(centerZ - dragZ, blockSize)) * blockSize;
        }
        return new Point2D.Double(x, y);
    }

    private static double snapToScreenPixel(double blockCoordinate, double blockSize) {
        return Math.floor(blockCoordinate * blockSize) / blockSize;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        handleMarkerActionKey();
        if (searchBar != null && searchBar.getVisible()) searchBar.updateCursorCounter();

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.theWorld == null) {
            removeAllOverlays();
            dirtyLayers.addAll(NavigatorApi.getEnabledLayers(MOD));
            return;
        }

        UIState state = getActiveMapState();
        if (state == null) return;
        if (overlayViewportChanged(state)) {
            syncVisibleOverlays(state);
        } else {
            for (LayerManager manager : drainDirtyLayers()) syncLayerOverlays(manager, state, true);
        }
    }

    @SubscribeEvent
    public void onLayerRefresh(LayerRefreshEvent event) {
        if (event.getLayerManager()
            .isEnabled(MOD)) dirtyLayers.add(event.getLayerManager());
    }

    private @Nullable UIState getActiveMapState() {
        UIState state = api.getUIState(Context.UI.Fullscreen);
        if (isUsable(state)) return state;

        state = api.getUIState(Context.UI.Minimap);
        return isUsable(state) ? state : null;
    }

    private boolean isUsable(@Nullable UIState state) {
        return state != null && state.active && state.blockBounds != null && state.mapCenter != null;
    }

    private void syncVisibleOverlays(UIState state) {
        for (LayerManager manager : NavigatorApi.getEnabledLayers(MOD)) {
            syncLayerOverlays(manager, state, consumeDirtyLayer(manager));
        }
        overlayUi = state.ui;
        overlayDimension = state.dimension;
        overlayCenterX = getOverlayCenterX(state);
        overlayCenterZ = getOverlayCenterZ(state);
        overlayWidth = (int) Math.ceil(state.blockBounds.maxX - state.blockBounds.minX);
        overlayHeight = (int) Math.ceil(state.blockBounds.maxZ - state.blockBounds.minZ);
    }

    private boolean overlayViewportChanged(UIState state) {
        return overlayUi != state.ui || overlayDimension != state.dimension
            || overlayCenterX != getOverlayCenterX(state)
            || overlayCenterZ != getOverlayCenterZ(state)
            || overlayWidth != (int) Math.ceil(state.blockBounds.maxX - state.blockBounds.minX)
            || overlayHeight != (int) Math.ceil(state.blockBounds.maxZ - state.blockBounds.minZ);
    }

    private void syncLayerOverlays(LayerManager manager, UIState state, boolean refresh) {
        LayerRenderer layer = manager.getLayerRenderer(MOD);
        if (!(layer instanceof UniversalLayerRenderer renderer) || !renderer.hasJourneyMapV6Overlays()) return;
        if (!manager.isLayerActive()) {
            removeOverlays(renderer);
            return;
        }

        int width = (int) Math.ceil(state.blockBounds.maxX - state.blockBounds.minX);
        int height = (int) Math.ceil(state.blockBounds.maxZ - state.blockBounds.minZ);
        if (state.ui == Context.UI.Fullscreen) {
            manager.recacheFullscreenMap(getOverlayCenterX(state), getOverlayCenterZ(state), width, height);
        } else {
            manager.recacheMiniMap(getOverlayCenterX(state), getOverlayCenterZ(state), width, height);
        }
        syncOverlays(renderer, refresh);
    }

    private int getOverlayCenterX(UIState state) {
        if (state.ui == Context.UI.Fullscreen && fullscreen != null && fullscreen.getUiState().active) {
            return (int) Math.round(fullscreen.getCenterBlockX(true));
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        return state.ui == Context.UI.Minimap && minecraft.thePlayer != null ? (int) minecraft.thePlayer.posX
            : state.mapCenter.getX();
    }

    private int getOverlayCenterZ(UIState state) {
        if (state.ui == Context.UI.Fullscreen && fullscreen != null && fullscreen.getUiState().active) {
            return (int) Math.round(fullscreen.getCenterBlockZ(true));
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        return state.ui == Context.UI.Minimap && minecraft.thePlayer != null ? (int) minecraft.thePlayer.posZ
            : state.mapCenter.getZ();
    }

    private boolean consumeDirtyLayer(LayerManager manager) {
        synchronized (dirtyLayers) {
            return dirtyLayers.remove(manager);
        }
    }

    private List<LayerManager> drainDirtyLayers() {
        synchronized (dirtyLayers) {
            List<LayerManager> layers = new ArrayList<>(dirtyLayers);
            dirtyLayers.clear();
            return layers;
        }
    }

    private void syncOverlays(UniversalLayerRenderer renderer, boolean refresh) {
        Map<ILocationProvider, List<Displayable>> shown = overlays
            .computeIfAbsent(renderer, ignored -> new HashMap<>());
        Set<ILocationProvider> visible = new HashSet<>();
        for (UniversalRenderStep<?> step : renderer.getRenderSteps()) {
            ILocationProvider location = step.getLocation();
            visible.add(location);
            if (renderer.hasMapMarker()) {
                List<Displayable> current = shown.get(location);
                if (current != null && !refresh) continue;

                MarkerOverlay marker = createMarker(renderer, step);
                if (marker == null) {
                    removeOverlays(current);
                    shown.put(location, new ArrayList<>());
                } else {
                    List<Displayable> updated = new ArrayList<>();
                    updated.add(marker);
                    showOverlays(updated);
                    removeOverlays(current);
                    shown.put(location, updated);
                }
                continue;
            }

            if (refresh) removeOverlays(shown.remove(location));
            if (shown.containsKey(location)) continue;

            List<Displayable> overlays = new ArrayList<>();
            Collection<?> candidates = renderer.createJourneyMapV6Overlays(location);
            if (candidates == null) {
                shown.put(location, overlays);
                continue;
            }
            for (Object candidate : candidates) {
                if (!(candidate instanceof Displayable displayable)) continue;
                if (displayable instanceof Overlay overlay) attachInteraction(overlay, renderer, step, null);
                overlays.add(displayable);
            }
            showOverlays(overlays);
            shown.put(location, overlays);
        }

        shown.entrySet()
            .removeIf(entry -> {
                if (visible.contains(entry.getKey())) return false;
                removeOverlays(entry.getValue());
                return true;
            });
    }

    private @Nullable MarkerOverlay createMarker(UniversalLayerRenderer renderer, UniversalRenderStep<?> step) {
        MapMarker marker = renderer.createMapMarker(step.getLocation());
        if (marker == null) return null;

        MapImage image;
        if (marker.getImage() != null) {
            image = new MapImage(marker.getImage());
        } else if (marker.getImageLocation() != null) {
            image = new MapImage(
                marker.getImageLocation(),
                marker.getTextureX(),
                marker.getTextureY(),
                marker.getTextureWidth(),
                marker.getTextureHeight(),
                0xFFFFFF,
                1.0F);
        } else {
            return null;
        }
        image.setDisplayWidth(marker.getDisplayWidth())
            .setDisplayHeight(marker.getDisplayHeight())
            .centerAnchors()
            .setBlur(false);

        ILocationProvider location = step.getLocation();
        BlockPos point = new BlockPos(
            (int) Math.floor(location.getBlockX()),
            64,
            (int) Math.floor(location.getBlockZ()));
        MarkerOverlay overlay = new MarkerOverlay(Navigator.MODID, point, image);
        overlay.setDimension(location.getDimensionId())
            .setOverlayGroupName(
                location.getClass()
                    .getName())
            .setActiveUIs(Context.UI.Fullscreen, Context.UI.Minimap)
            .setDisplayOrder(100 + renderer.getRenderPriority());

        overlay.setLabel(marker.getLabel())
            .setTitle(null);
        int labelMinZoom = marker.getLabelMinZoom() == null ? UIState.FULLSCREEN_ZOOM_MIN
            : toJourneyMapZoom(marker.getLabelMinZoom());
        overlay.getTextProperties()
            .setColor(marker.getLabelColor())
            .setScale(marker.getLabelScale())
            .setBackgroundOpacity(marker.getLabelBackgroundOpacity())
            .setOffsetY(marker.getLabelOffsetY())
            .setMinZoom(labelMinZoom);
        if (!marker.isLabelOnMinimap()) overlay.getTextProperties()
            .setActiveUIs(Context.UI.Fullscreen);

        markerProperties.put(overlay, marker);
        if (fullscreen != null && fullscreen.getUiState().active)
            updateMarkerScale(overlay, marker, fullscreen.getUiState());

        List<String> tooltip = marker.getTooltip();
        if (tooltip == null && step instanceof UniversalLocationInteractableStep<?>interactableStep) {
            tooltip = new ArrayList<>();
            interactableStep.getTooltip(tooltip);
        }

        attachInteraction(overlay, renderer, step, tooltip);
        return overlay;
    }

    private void attachInteraction(Overlay overlay, UniversalLayerRenderer renderer, UniversalRenderStep<?> step,
        @Nullable List<String> tooltip) {
        if (overlay.getOverlayListener() != null
            || !(renderer instanceof UniversalInteractableRenderer interactableRenderer)
            || !(step instanceof UniversalLocationInteractableStep<?>interactableStep)) return;

        if (tooltip == null) {
            tooltip = new ArrayList<>();
            interactableStep.getTooltip(tooltip);
        }
        OverlayListener listener = new OverlayListener(
            overlay instanceof MarkerOverlay marker ? marker : null,
            interactableRenderer,
            interactableStep);
        listener.setTooltip(tooltip);
        overlay.setOverlayListener(listener);
    }

    private int toJourneyMapZoom(int zoomStep) {
        return (int) Math.max(UIState.FULLSCREEN_ZOOM_MIN, Math.min(UIState.ZOOM_IN_MAX, 512 * Math.pow(2, zoomStep)));
    }

    private void updateMarkerScales(UIState state) {
        markerProperties.forEach((overlay, marker) -> updateMarkerScale(overlay, marker, state));
    }

    private void updateMarkerScale(MarkerOverlay overlay, MapMarker marker, UIState state) {
        double zoomStep = Math.log(state.blockSize) / Math.log(2.0);
        applyMarkerScale(overlay, marker, marker.getDisplayZoomScale(zoomStep), marker.getLabelZoomScale(zoomStep));
    }

    private void resetMarkerScales() {
        markerProperties.forEach((overlay, marker) -> applyMarkerScale(overlay, marker, 1, 1));
    }

    private void applyMarkerScale(MarkerOverlay overlay, MapMarker marker, double displayScale, double labelScale) {
        MapImage image = overlay.getIcon();
        double width = marker.getDisplayWidth() * displayScale;
        double height = marker.getDisplayHeight() * displayScale;
        float textScale = (float) (marker.getLabelScale() * labelScale);
        int labelOffset = (int) Math.round(marker.getLabelOffsetY() * displayScale);
        if (image.getDisplayWidth() == width && image.getDisplayHeight() == height
            && overlay.getTextProperties()
                .getScale() == textScale
            && overlay.getTextProperties()
                .getOffsetY() == labelOffset)
            return;

        image.setDisplayWidth(width)
            .setDisplayHeight(height)
            .centerAnchors();
        overlay.getTextProperties()
            .setScale(textScale)
            .setOffsetY(labelOffset);
        overlay.flagForRerender();
    }

    private void showOverlays(Collection<Displayable> displayables) {
        for (Displayable displayable : displayables) {
            try {
                api.show(displayable);
            } catch (Exception e) {
                Navigator.LOG.error("Could not show JourneyMap 6 overlay", e);
            }
        }
    }

    private void handleMarkerActionKey() {
        int keyCode = NavigatorApi.ACTION_KEY.getKeyCode();
        boolean down = keyCode >= 0 && keyCode < 256 && Keyboard.isKeyDown(keyCode);
        if (down && !actionKeyDown && hoveredOverlay != null) {
            hoveredOverlay.renderer.onRenderStepKeyPressed(hoveredOverlay.step, keyCode);
        }
        actionKeyDown = down;
    }

    private void removeOverlays(UniversalLayerRenderer renderer) {
        if (hoveredOverlay != null && hoveredOverlay.renderer == renderer) clearHoveredOverlay();
        Map<ILocationProvider, List<Displayable>> shown = overlays.remove(renderer);
        if (shown != null) shown.values()
            .forEach(this::removeOverlays);
    }

    private void removeOverlays(@Nullable Collection<Displayable> displayables) {
        if (displayables == null) return;
        for (Displayable displayable : displayables) {
            if (displayable instanceof Overlay overlay && overlay.getOverlayListener() == hoveredOverlay) {
                clearHoveredOverlay();
            }
            if (displayable instanceof MarkerOverlay marker) markerProperties.remove(marker);
            api.remove(displayable);
        }
    }

    private void removeAllOverlays() {
        overlays.values()
            .forEach(
                shown -> shown.values()
                    .forEach(this::removeOverlays));
        overlays.clear();
        markerProperties.clear();
        overlayUi = null;
        overlayDimension = Integer.MIN_VALUE;
        overlayCenterX = Integer.MIN_VALUE;
        overlayCenterZ = Integer.MIN_VALUE;
        overlayWidth = -1;
        overlayHeight = -1;
        clearHoveredOverlay();
    }

    private void clearHoveredOverlay() {
        if (hoveredOverlay != null) hoveredOverlay.clearHover();
    }

    private final class OverlayListener implements IOverlayListener {

        private final @Nullable MarkerOverlay marker;
        private final UniversalInteractableRenderer renderer;
        private final UniversalLocationInteractableStep<?> step;
        private List<String> tooltip = new ArrayList<>();

        private OverlayListener(@Nullable MarkerOverlay marker, UniversalInteractableRenderer renderer,
            UniversalLocationInteractableStep<?> step) {
            this.marker = marker;
            this.renderer = renderer;
            this.step = step;
        }

        @Override
        public void onDeactivate(UIState mapState) {
            clearHover();
        }

        @Override
        public void onMouseMove(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
            if (!contains((int) mousePosition.x, (int) mousePosition.y)) {
                clearHover();
                return;
            }
            renderer.setRenderStepHover(step);
            hoveredOverlay = this;
        }

        @Override
        public void onMouseOut(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition) {
            clearHover();
        }

        @Override
        public boolean onMouseClick(UIState mapState, Point2D.Double mousePosition, BlockPos blockPosition, int button,
            boolean doubleClick) {
            // Fullscreen clicks are dispatched by the PRE map-click event before JourneyMap's own layers run.
            return true;
        }

        private void clearHover() {
            renderer.clearRenderStepHover(step);
            if (hoveredOverlay == this) hoveredOverlay = null;
        }

        private void setTooltip(@Nullable List<String> tooltip) {
            this.tooltip = tooltip == null ? new ArrayList<>() : new ArrayList<>(tooltip);
        }

        private boolean contains(int mouseX, int mouseY) {
            return marker == null || JourneyMapV6Plugin.this.contains(marker, mouseX, mouseY);
        }
    }

    private boolean contains(MarkerOverlay marker, int mouseX, int mouseY) {
        if (fullscreen == null) return false;

        UIState state = fullscreen.getUiState();
        Point2D.Double position = getBlockPixel(
            marker.getPoint()
                .getX(),
            marker.getPoint()
                .getZ());
        MapImage icon = marker.getIcon();
        double centerX = position.x + state.blockSize / 2.0;
        double centerY = position.y + state.blockSize / 2.0;
        return mouseX >= centerX - icon.getAnchorX() && mouseX < centerX + icon.getDisplayWidth() - icon.getAnchorX()
            && mouseY >= centerY - icon.getAnchorY()
            && mouseY < centerY + icon.getDisplayHeight() - icon.getAnchorY();
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
        if (hoveredOverlay != null) {
            if (!hoveredOverlay.tooltip.isEmpty()) {
                DrawUtils.drawSimpleTooltip(
                    event.getFullscreen()
                        .getScreen(),
                    hoveredOverlay.tooltip,
                    event.getMouseX() + 16,
                    event.getMouseY() - 12,
                    0xFFFFFFFF,
                    0x86000000);
            } else {
                hoveredOverlay.renderer.drawCustomTooltip(
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

        List<InteractableLayer> interactables = new ArrayList<>();
        List<String> tooltip = null;
        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersFor(MOD)) {
            if (renderer instanceof UniversalLayerRenderer universal && universal.hasJourneyMapV6Overlays()) continue;
            if (!(renderer instanceof InteractableLayer interactable)) continue;

            interactables.add(interactable);
            interactable.onMouseMove(event.getMouseX(), event.getMouseY());
            if (tooltip == null || tooltip.isEmpty()) tooltip = interactable.getTooltip();
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            DrawUtils.drawSimpleTooltip(
                event.getFullscreen()
                    .getScreen(),
                tooltip,
                event.getMouseX() + 16,
                event.getMouseY() - 12,
                0xFFFFFFFF,
                0x86000000);
            return;
        }
        for (InteractableLayer interactable : interactables) {
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
    }

    private void drawSearchBar(FullscreenRenderEvent event) {
        boolean visible = NavigatorApi.getEnabledLayers(MOD)
            .stream()
            .anyMatch(manager -> manager.isLayerActive() && manager.hasSearchField());
        if (!visible) {
            if (searchBar != null) searchBar.setVisible(false);
            return;
        }

        int width = event.getFullscreen()
            .getScreen().width;
        int height = event.getFullscreen()
            .getScreen().height;
        if (searchBar == null || width != searchScreenWidth || height != searchScreenHeight) {
            searchScreenWidth = width;
            searchScreenHeight = height;
            searchBar = new SearchBar(6, height - 51, Math.min(width / 2 - 50, 200) * 2 / 3, 16);
            searchBar.setTextConsumer(
                text -> NavigatorApi.getEnabledLayers(MOD)
                    .forEach(manager -> {
                        if (manager.hasSearchField()) {
                            manager.onSearch(text);
                            manager.forceRefresh();
                        }
                    }));
        }
        searchBar.setVisible(true);
        searchBar.drawTextBox();
    }

    private void onClick(FullscreenMapEvent.ClickEvent event) {
        if (event.getStage() != FullscreenMapEvent.Stage.PRE || event.getButton() != 0) return;

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        long now = System.currentTimeMillis();
        boolean doubleClick = now - timeLastClick < 200L;
        timeLastClick = now;

        BlockPos location = event.getLocation();
        if (hoveredOverlay != null && !hoveredOverlay.contains(mouseX, mouseY)) clearHoveredOverlay();
        if (hoveredOverlay != null && hoveredOverlay.renderer
            .onRenderStepClick(hoveredOverlay.step, doubleClick, mouseX, mouseY, location.getX(), location.getZ())) {
            event.cancel();
            return;
        }

        for (LayerRenderer renderer : NavigatorApi.getActiveRenderersFor(MOD)) {
            if (hoveredOverlay != null && renderer instanceof UniversalLayerRenderer universal
                && universal.hasJourneyMapV6Overlays()) continue;
            if (renderer instanceof UniversalInteractableRenderer nativeInteractable
                && nativeInteractable.hasJourneyMapV6Overlays()) {
                nativeInteractable.clearRenderStepHover();
            }
            if (renderer instanceof InteractableLayer interactable
                && interactable.onMapClick(doubleClick, mouseX, mouseY, location.getX(), location.getZ())) {
                event.cancel();
                return;
            }
        }
    }
}
