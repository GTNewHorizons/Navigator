package com.gtnewhorizons.navigator.api.xaero.renderers;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

import com.gtnewhorizons.navigator.ClientProxy;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
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

    @Override
    protected abstract List<? extends InteractableRenderStep> generateRenderSteps(
        List<? extends ILocationProvider> visibleElements);

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

    public void doDoubleClick() {
        if (hovered != null) {
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

    public KeyBinding getActionKey() {
        return ClientProxy.ACTION_KEY;
    }
}
