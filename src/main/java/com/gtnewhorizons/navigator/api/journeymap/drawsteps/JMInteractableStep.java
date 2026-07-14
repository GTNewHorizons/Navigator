package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;

/** JourneyMap 5-specific interactable step for waypoint-capable locations. */
public interface JMInteractableStep extends JMRenderStep, InteractableStep {

    /** Draws custom tooltip content in JourneyMap's fullscreen GUI. */
    void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    /** @return whether the given GUI-space pointer is over this step */
    boolean isMouseOver(int mouseX, int mouseY);
}
