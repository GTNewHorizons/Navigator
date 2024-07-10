package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;

public interface JMClickableDrawStep extends JMDrawStep {

    List<String> getTooltip();

    void drawTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    boolean isMouseOver(int mouseX, int mouseY);

    void onActionKeyPressed();

    IWaypointAndLocationProvider getLocationProvider();
}
