package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;

public interface XaeroInteractableStep extends XaeroRenderStep, InteractableStep {

    boolean isMouseOver(double mouseX, double mouseY, double scale);

    void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj);

    @Override
    default void onActionKeyPressed() {
        onActionButton();
    }

    /**
     * @deprecated Use {@link #onActionKeyPressed()} instead
     */
    @Deprecated
    default void onActionButton() {}
}
