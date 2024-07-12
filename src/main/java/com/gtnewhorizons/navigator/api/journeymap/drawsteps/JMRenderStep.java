package com.gtnewhorizons.navigator.api.journeymap.drawsteps;

import com.gtnewhorizons.navigator.api.model.steps.RenderStep;

import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.map.GridRenderer;

public interface JMRenderStep extends DrawStep, RenderStep {

    @Override
    void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale, double fontScale,
        double rotation);
}
