package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;

/** Xaero-specific interactable step for waypoint-capable locations. */
public interface XaeroInteractableStep extends XaeroRenderStep, InteractableStep {

    /** @return whether Xaero's map-space pointer is over this step */
    boolean isMouseOver(double mouseX, double mouseY, double scale);

    /** Draws custom tooltip content using Xaero's GUI coordinates. */
    void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj);

    @Override
    default void onActionKeyPressed() {
        onActionButton();
    }

    /**
     * Legacy action hook.
     *
     * @deprecated Use {@link #onActionKeyPressed()} instead
     */
    @Deprecated
    default void onActionButton() {}
}
