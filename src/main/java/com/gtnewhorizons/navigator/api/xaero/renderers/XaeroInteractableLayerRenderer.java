package com.gtnewhorizons.navigator.api.xaero.renderers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.InteractableLayerManager;
import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

public abstract class XaeroInteractableLayerRenderer extends XaeroLayerRenderer {

    protected InteractableLayerManager manager;
    protected XaeroInteractableStep hovered;

    public XaeroInteractableLayerRenderer(InteractableLayerManager manager) {
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

    public final void onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX, int mouseBlockZ) {
        if (!manager.getOpenModGui()
            .equals(getLayerMod())) return;
        if (hovered != null) {
            onClick(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
        } else {
            onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
        }
    }

    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        if (hovered != null) {
            hovered.getTooltip(tooltip);
        }
        return tooltip;
    }

    public void onClick(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX, int mouseBlockZ) {
        if (isDoubleClick) {
            if (hovered.getLocationProvider()
                .isActiveAsWaypoint()) {
                manager.clearActiveWaypoint();
            } else {
                manager.setActiveWaypoint(
                    hovered.getLocationProvider()
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
        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY) && hovered != null) {
            hovered.onActionKeyPressed();
            manager.forceRefresh();
            return true;
        }
        return false;
    }
}
