package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

public interface XaeroRenderStep extends RenderStep {

    void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale);

    default void draw(double cameraX, double cameraZ, double scale, float guiBasedScale) {

    }
}
