package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;

public interface InteractableRenderStep extends XaeroRenderStep {

    boolean isMouseOver(double mouseX, double mouseY, double scale);

    void drawTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj);

    void onActionButton();

    IWaypointAndLocationProvider getLocationProvider();
}
