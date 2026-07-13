package com.gtnewhorizons.navigator.internal.journeymap.v5;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;

import journeymap.client.render.map.GridRenderer;

public final class JourneyMapV5Renderer {

    private JourneyMapV5Renderer() {}

    public static void draw(RenderStep step, double pixelX, double pixelY, GridRenderer renderer, float drawScale,
        double fontScale, double rotation) {
        if (step instanceof JMRenderStep jmStep) {
            jmStep.draw(pixelX, pixelY, renderer, drawScale, fontScale, rotation);
        } else if (step instanceof UniversalRenderStep<?>universalStep) {
            int blockSize = (int) Math.pow(2.0, renderer.getZoom());
            double x = (double) (renderer.getWidth() / 2) + (universalStep.getLocation()
                .getBlockX() - renderer.getCenterBlockX()) * blockSize + pixelX;
            double y = (double) (renderer.getHeight() / 2) + (universalStep.getLocation()
                .getBlockZ() - renderer.getCenterBlockZ()) * blockSize + pixelY;
            universalStep.drawJourneyMap(x, y, drawScale, renderer.getZoom(), blockSize, fontScale, rotation);
        }
    }
}
