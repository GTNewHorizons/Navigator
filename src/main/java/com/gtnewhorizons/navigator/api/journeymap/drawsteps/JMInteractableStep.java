package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;

/**
 * JourneyMap 5-specific interactable step for waypoint-capable locations.
 *
 * @deprecated Use {@link com.gtnewhorizons.navigator.api.model.steps.UniversalLocationInteractableStep}, or
 *             {@link com.gtnewhorizons.navigator.api.model.steps.UniversalInteractableStep} when double-click should
 *             toggle a waypoint. This interface is retained for JourneyMap 5-only integrations.
 */
@Deprecated
public interface JMInteractableStep extends JMRenderStep, InteractableStep {

    /** Draws custom tooltip content in JourneyMap's fullscreen GUI. */
    void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    /** @return whether the given GUI-space pointer is over this step */
    boolean isMouseOver(int mouseX, int mouseY);
}
