package com.gtnewhorizons.navigator.api.xaero.renderers;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayer;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

public abstract class XaeroInteractableLayerRenderer extends XaeroLayerRenderer implements InteractableLayer {

    protected InteractableLayerManager manager;
    protected XaeroInteractableStep hovered;

    public XaeroInteractableLayerRenderer(@Nonnull InteractableLayerManager manager) {
        super(manager);
        this.manager = manager;
    }

    public void updateHovered(double mouseX, double mouseY, double scale) {
        for (XaeroRenderStep step : getReversedRenderSteps()) {
            if (step instanceof XaeroInteractableStep interactableRenderStep
                && interactableRenderStep.isMouseOver(mouseX, mouseY, scale)) {
                hovered = interactableRenderStep;
                return;
            }
        }
        hovered = null;
    }

    public void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj) {
        if (hovered != null) {
            hovered.drawCustomTooltip(gui, mouseX, mouseY, scale, scaleAdj);
        }
    }

    @Override
    public final void onMouseMove(int mouseX, int mouseY) {
        updateHovered(mouseX, mouseY, 1f);
    }

    public final boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX, int mouseBlockZ) {
        if (!manager.getOpenModGui()
            .equals(getLayerMod())) return false;
        if (hovered != null) {
            onClick(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
            return true;
        } else {
            onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
            return false;
        }
    }

    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        if (hovered != null) {
            hovered.getTooltip(tooltip);
        }
        return tooltip;
    }

    @Override
    public final void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {

    }

    public void onClick(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX, int mouseBlockZ) {
        if (isDoubleClick) {
            if (hovered.getLocation()
                .isActiveAsWaypoint()) {
                manager.clearActiveWaypoint();
            } else {
                manager.setActiveWaypoint(
                    hovered.getLocation()
                        .toWaypoint());
            }
        }
    }

    public void onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX,
        int mouseBlockZ) {}

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    public boolean onKeyPressed(int keyCode) {
        if (hovered != null) {
            if (Util.isKeyPressed(NavigatorApi.ACTION_KEY)) {
                hovered.onActionKeyPressed();
                manager.forceRefresh();
                return true;
            } else if (hovered.onActionWithKeyPressed(keyCode)) {
                manager.forceRefresh();
                return true;
            }
        }
        return false;
    }
}
