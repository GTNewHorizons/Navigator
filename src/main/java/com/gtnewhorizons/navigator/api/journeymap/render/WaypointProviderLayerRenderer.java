package com.gtnewhorizons.navigator.api.journeymap.render;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.settings.KeyBinding;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMClickableDrawStep;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMDrawStep;
import com.gtnewhorizons.navigator.api.model.layers.WaypointProviderManager;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

public abstract class WaypointProviderLayerRenderer extends JMLayerRenderer {

    private final WaypointProviderManager manager;
    private JMClickableDrawStep hoveredDrawStep = null;

    public WaypointProviderLayerRenderer(WaypointProviderManager manager) {
        super(manager);
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JMDrawStep> getRenderSteps() {
        return (List<JMDrawStep>) getReversedRenderSteps();
    }

    public void onMouseMove(int mouseX, int mouseY) {
        hoveredDrawStep = null;
        for (RenderStep drawStep : getRenderStepsForInteraction()) {
            if (drawStep instanceof JMClickableDrawStep clickableDrawStep) {
                if (clickableDrawStep.isMouseOver(mouseX, mouseY)) {
                    hoveredDrawStep = clickableDrawStep;
                    return;
                }
            }
        }
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
        return Navigator.actionKey;
    }
}
