package com.gtnewhorizons.navigator.api.model.steps;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;

import cpw.mods.fml.common.Optional;

@Optional.Interface(
    iface = "com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep",
    modid = "journeymap")
public abstract class UniversalInteractableStep<T extends IWaypointAndLocationProvider> extends UniversalRenderStep<T>
    implements JMInteractableStep, XaeroInteractableStep {

    public UniversalInteractableStep(T location) {
        super(location);
    }

    public abstract void draw(double topX, double topY, float drawScale, double zoom);

    @Override
    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {}

    @Override
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

    @Override
    public final boolean isMouseOver(double mouseX, double mouseY, double scale) {
        return false;
    }

    @Override
    public final void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj) {
        drawCustomTooltip(gui.mc.fontRenderer, (int) mouseX, (int) mouseY, gui.width, gui.height);
    }

    @Override
    public void onActionKeyPressed() {}
}
