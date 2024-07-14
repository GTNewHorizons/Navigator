package com.gtnewhorizons.navigator.api.xaero.renderers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

public abstract class XaeroInteractableLayerRenderer extends XaeroLayerRenderer {

    protected WaypointProviderManager manager;
    protected XaeroInteractableStep hovered;

    public XaeroInteractableLayerRenderer(WaypointProviderManager manager) {
        super(manager);
        this.manager = manager;
        hovered = null;
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

    public void doActionKeyPress() {
        if (manager.isLayerActive() && hovered != null) {
            hovered.onActionButton();
            manager.forceRefresh();
        }
    }

    public final void onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX, int mouseBlockZ) {
        if (manager.getOpenModGui()
            .equals(getLayerMod())) {
            onClick(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
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
        if (hovered != null) {
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
        } else {
            onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, mouseBlockX, mouseBlockZ);
        }
    }

    public void onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int mouseBlockX,
        int mouseBlockZ) {}

    public KeyBinding getActionKey() {
        return NavigatorApi.ACTION_KEY;
    }
}
