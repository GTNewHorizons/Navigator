package com.gtnewhorizons.navigator.api.model.steps;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

/**
 * Universal interactable step for a plain location with no waypoint requirement.
 * <p>
 * Subclasses normally provide tooltip lines, optional action-key behavior, and may override the rectangular
 * {@link #isMouseOver(int, int)} hit test.
 *
 * @param <T> source location type
 */
public abstract class UniversalLocationInteractableStep<T extends ILocationProvider> extends UniversalRenderStep<T>
    implements LocationInteractableStep {

    public UniversalLocationInteractableStep(T location) {
        super(location);
    }

    public abstract void draw(double x, double y, float drawScale, double zoom);

    /**
     * Draws a custom tooltip after normal tooltip processing.
     *
     * @param fontRenderer  Minecraft font renderer
     * @param mouseX        mouse X in GUI coordinates
     * @param mouseY        mouse Y in GUI coordinates
     * @param displayWidth  GUI width
     * @param displayHeight GUI height
     */
    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {}

    /**
     * Tests the default rectangular hit area.
     *
     * @param mouseX mouse X in the current map's interaction coordinates
     * @param mouseY mouse Y in the current map's interaction coordinates
     * @return whether the pointer is inside this step
     */
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getAdjustedWidth()
            && mouseY >= getY()
            && mouseY <= getY() + getAdjustedHeight();
    }

    /**
     * Applies integration-specific scaling before calling {@link #isMouseOver(int, int)}.
     *
     * @return whether the pointer is inside this step
     */
    public final boolean mouseOver(int mouseX, int mouseY) {
        if (isXaero && shouldScale) {
            mouseX = (int) (mouseX * getScaling(zoom));
            mouseY = (int) (mouseY * getScaling(zoom));
        }

        return isMouseOver(mouseX, mouseY);
    }

    public final boolean isMouseOver(double mouseX, double mouseY, double scale) {
        return false;
    }

    public final void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj) {
        drawCustomTooltip(gui.mc.fontRenderer, (int) mouseX, (int) mouseY, gui.width, gui.height);
    }

    /** Default action-key hook; subclasses may override. */
    @Override
    public void onActionKeyPressed() {}
}
