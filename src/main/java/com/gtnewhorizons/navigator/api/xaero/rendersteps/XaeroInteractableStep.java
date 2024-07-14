package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;

public interface XaeroInteractableStep extends XaeroRenderStep {

    boolean isMouseOver(double mouseX, double mouseY, double scale);

    void getTooltip(List<String> list);

    void drawCustomTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj);

    void onActionButton();

    IWaypointAndLocationProvider getLocationProvider();
}
