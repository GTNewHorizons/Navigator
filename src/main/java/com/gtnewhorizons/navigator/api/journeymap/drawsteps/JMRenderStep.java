package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.map.GridRenderer;

/**
 * JourneyMap 5-specific render step.
 * <p>
 * Prefer {@link com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep} unless direct JourneyMap 5 drawing
 * APIs are required. This interface directly links JourneyMap client classes.
 *
 * @deprecated Use {@link com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep}. This interface is retained
 *             for JourneyMap 5-only integrations.
 */
@Deprecated
public interface JMRenderStep extends DrawStep, RenderStep {

    /** Draws using JourneyMap 5's grid renderer and coordinate system. */
    @Override
    void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale, double fontScale,
        double rotation);
}
