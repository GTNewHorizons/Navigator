package com.gtnewhorizons.navigator.api.model.steps;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;

public abstract class UniversalLocationInteractableStep<T extends ILocationProvider> extends UniversalRenderStep<T>
    implements LocationInteractableStep {

    public UniversalLocationInteractableStep(T location) {
        super(location);
    }

    public abstract void draw(double x, double y, float drawScale, double zoom);

    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {}

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getAdjustedWidth()
            && mouseY >= getY()
            && mouseY <= getY() + getAdjustedHeight();
    }

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

    @Override
    public void onActionKeyPressed() {}
}
