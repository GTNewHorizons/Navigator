package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

public abstract class JMInteractableLayerRenderer extends JMLayerRenderer {

    protected final WaypointProviderManager manager;
    protected JMInteractableStep hoveredDrawStep = null;

    public JMInteractableLayerRenderer(WaypointProviderManager manager) {
        super(manager);
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JMRenderStep> getRenderSteps() {
        return (List<JMRenderStep>) getReversedRenderSteps();
    }

    public void onMouseMove(int mouseX, int mouseY) {
        hoveredDrawStep = null;
        for (RenderStep drawStep : getRenderStepsForInteraction()) {
            if (drawStep instanceof JMInteractableStep clickableDrawStep) {
                if (clickableDrawStep.isMouseOver(mouseX, mouseY)) {
                    hoveredDrawStep = clickableDrawStep;
                    return;
                }
            }
        }
    }

    public final boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (manager.getOpenModGui()
            .equals(getLayerMod())) {
            return onClick(isDoubleClick, mouseX, mouseY, blockX, blockZ);
        }
        return false;
    }

    public boolean onClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (hoveredDrawStep != null) {
            if (isDoubleClick) {
                if (hoveredDrawStep.getLocationProvider()
                    .isActiveAsWaypoint()) {
                    manager.clearActiveWaypoint();
                } else {
                    manager.setActiveWaypoint(
                        hoveredDrawStep.getLocationProvider()
                            .toWaypoint());
                }
            }
            return true;
        }
        return onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, blockX, blockZ);
    }

    public boolean onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        return false;
    }

    public List<String> getTextTooltip() {
        if (hoveredDrawStep != null) {
            return hoveredDrawStep.getTooltip();
        }
        return null;
    }

    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {
        if (hoveredDrawStep != null) {
            hoveredDrawStep.drawTooltip(fontRenderer, mouseX, mouseY, displayWidth, displayHeight);
        }
    }

    public void onActionKeyPressed() {
        if (hoveredDrawStep != null) {
            hoveredDrawStep.onActionKeyPressed();
            manager.forceRefresh();
        }
    }

    public KeyBinding getActionKey() {
        return NavigatorApi.ACTION_KEY;
    }
}
