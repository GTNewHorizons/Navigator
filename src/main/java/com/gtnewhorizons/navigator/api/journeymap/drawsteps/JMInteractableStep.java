package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;

public interface JMInteractableStep extends JMRenderStep, InteractableStep {

    void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    boolean isMouseOver(int mouseX, int mouseY);
}
