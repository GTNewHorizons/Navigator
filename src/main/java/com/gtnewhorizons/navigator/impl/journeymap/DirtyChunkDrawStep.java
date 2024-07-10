package com.gtnewhorizons.navigator.impl.journeymap;

import java.awt.geom.Point2D;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMDrawStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;

public class DirtyChunkDrawStep implements JMDrawStep {

    private final DirtyChunkLocation dirtyChunkLocation;

    public DirtyChunkDrawStep(DirtyChunkLocation dirtyChunkLocation) {
        this.dirtyChunkLocation = dirtyChunkLocation;
    }

    @Override
    public void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale,
        double fontScale, double rotation) {
        final int zoom = gridRenderer.getZoom();
        double blockSize = Math.pow(2, zoom);
        final Point2D.Double blockAsPixel = gridRenderer
            .getBlockPixelInGrid(dirtyChunkLocation.getBlockX(), dirtyChunkLocation.getBlockZ());
        final Point2D.Double pixel = new Point2D.Double(
            blockAsPixel.getX() + draggedPixelX,
            blockAsPixel.getY() + draggedPixelY);
        float alpha = 0.5f;
        alpha *= alpha * 204;
        int color = dirtyChunkLocation.isDirty() ? 0xFF0000 : 0x00FFAA;
        DrawUtil.drawRectangle(pixel.getX(), pixel.getY(), 16 * blockSize, 16 * blockSize, color, (int) alpha);

        if (dirtyChunkLocation.isDirty()) {
            final int borderColor = 0xFFD700;
            final int borderAlpha = 204;
            DrawUtil.drawRectangle(pixel.getX(), pixel.getY(), 15 * blockSize, blockSize, borderColor, borderAlpha);
            DrawUtil.drawRectangle(
                pixel.getX() + 15 * blockSize,
                pixel.getY(),
                blockSize,
                15 * blockSize,
                borderColor,
                borderAlpha);
            DrawUtil.drawRectangle(
                pixel.getX() + 1 * blockSize,
                pixel.getY() + 15 * blockSize,
                15 * blockSize,
                blockSize,
                borderColor,
                borderAlpha);
            DrawUtil.drawRectangle(
                pixel.getX(),
                pixel.getY() + 1 * blockSize,
                blockSize,
                15 * blockSize,
                borderColor,
                borderAlpha);

            DrawUtil.drawLabel(
                "D",
                pixel.getX() + 13 * blockSize,
                pixel.getY() + 13 * blockSize,
                DrawUtil.HAlign.Left,
                DrawUtil.VAlign.Above,
                0,
                180,
                0x00FFFFFF,
                255,
                fontScale,
                false,
                rotation);
        }
    }
}
