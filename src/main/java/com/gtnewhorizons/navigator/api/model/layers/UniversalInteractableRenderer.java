package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.util.Util;

public class UniversalInteractableRenderer extends UniversalLayerRenderer implements InteractableLayer {

    protected InteractableLayerManager manager;
    protected JMInteractableStep hoveredDrawStep = null;

    public UniversalInteractableRenderer(InteractableLayerManager manager) {
        super(manager);
        this.manager = manager;
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
        if (hoveredDrawStep != null) {
            return onClick(isDoubleClick, mouseX, mouseY, blockX, blockZ);
        }

        return onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, blockX, blockZ);
    }

    public boolean onClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (isDoubleClick) {
            if (hoveredDrawStep.getLocationProvider()
                .isActiveAsWaypoint()) {
                manager.clearActiveWaypoint();
            } else {
                manager.setActiveWaypoint(
                    hoveredDrawStep.getLocationProvider()
                        .toWaypoint());
            }
            return true;
        }
        return false;
    }

    public boolean onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        return false;
    }

    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        if (hoveredDrawStep != null) {
            hoveredDrawStep.getTooltip(tooltip);
        }
        return tooltip;
    }

    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {
        if (hoveredDrawStep != null) {
            hoveredDrawStep.drawCustomTooltip(fontRenderer, mouseX, mouseY, displayWidth, displayHeight);
        }
    }

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    public boolean onKeyPressed(int keyCode) {
        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY) && hoveredDrawStep != null) {
            hoveredDrawStep.onActionKeyPressed();
            manager.forceRefresh();
            return true;
        }
        return false;
    }
}
