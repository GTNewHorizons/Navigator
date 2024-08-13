package com.gtnewhorizons.navigator.api.model.steps;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep;

import cpw.mods.fml.common.Optional;

@Optional.InterfaceList(
    value = {
        @Optional.Interface(
            iface = "com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep",
            modid = "journeymap"),
        @Optional.Interface(
            iface = "com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroInteractableStep",
            modid = "XaeroWorldMap") })
public abstract class UniversalInteractableStep<T extends IWaypointAndLocationProvider> extends UniversalRenderStep<T>
    implements JMInteractableStep, XaeroInteractableStep {

    public UniversalInteractableStep(T location) {
        super(location);
    }

    public abstract void draw(double topX, double topY, float drawScale, double zoom);

    @Override
    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {

    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= topX && mouseX <= topX + getAdjustedWidth()
            && mouseY >= topY
            && mouseY <= topY + getAdjustedHeight();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY, double scale) {
        return isMouseOver((int) mouseX, (int) mouseY);
    }

    @Override
    public void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj) {

    }

    @Override
    public void onActionKeyPressed() {}

    @Override
    public IWaypointAndLocationProvider getLocationProvider() {
        return location;
    }
}
