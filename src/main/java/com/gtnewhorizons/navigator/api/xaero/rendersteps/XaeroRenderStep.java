package com.gtnewhorizons.navigator.api.xaero.rendersteps;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

/** Xaero-specific render step. Prefer {@code UniversalRenderStep} for new layers. */
public interface XaeroRenderStep extends RenderStep {

    /** Draws a step using Xaero's camera and scale. */
    void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale);

    /** Optional Xaero draw path that also exposes GUI-based scale. */
    default void draw(double cameraX, double cameraZ, double scale, float guiBasedScale) {

    }
}
