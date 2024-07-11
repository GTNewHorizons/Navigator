package com.gtnewhorizons.navigator.api.xaero.renderers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.InteractableRenderStep;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

public abstract class InteractableLayerRenderer extends XaeroLayerRenderer {

    private double mouseX;
    private double mouseY;
    protected WaypointProviderManager manager;
    protected InteractableRenderStep hovered;

    public InteractableLayerRenderer(WaypointProviderManager manager) {
        super(manager);
        this.manager = manager;
        hovered = null;
    }

    public void updateHovered(double mouseX, double mouseY, double scale) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        for (XaeroRenderStep step : getReversedRenderSteps()) {
            if (step instanceof InteractableRenderStep interactableRenderStep
                && interactableRenderStep.isMouseOver(mouseX, mouseY, scale)) {
                hovered = interactableRenderStep;
                return;
            }
        }
        hovered = null;
    }

    public void drawTooltip(GuiScreen gui, double scale, int scaleAdj) {
        if (hovered != null) {
            hovered.drawTooltip(gui, mouseX, mouseY, scale, scaleAdj);
        }
    }

    public void doActionKeyPress() {
        if (manager.isLayerActive() && hovered != null) {
            hovered.onActionButton();
            manager.forceRefresh();
        }
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
        return Navigator.actionKey;
    }
}
