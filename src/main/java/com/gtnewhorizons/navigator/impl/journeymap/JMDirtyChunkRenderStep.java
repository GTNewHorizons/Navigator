package com.gtnewhorizons.navigator.impl.journeymap;

import static com.gtnewhorizons.navigator.api.NavigatorApi.CHUNK_WIDTH;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMInteractableStep;
import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;

public class JMDirtyChunkRenderStep implements JMInteractableStep {

    private final DirtyChunkLocation dirtyChunkLocation;

    private double topX, topY, chunkSize;

    public JMDirtyChunkRenderStep(DirtyChunkLocation dirtyChunkLocation) {
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

        chunkSize = blockSize * CHUNK_WIDTH;
        topX = pixel.getX();
        topY = pixel.getY();
        float alpha = 0.5f;
        alpha *= alpha * 204;
        int color = dirtyChunkLocation.isDirty() ? 0xFF0000 : 0x00FFAA;

        DrawUtil.drawRectangle(pixel.getX(), pixel.getY(), chunkSize, chunkSize, color, (int) alpha);

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
                DrawUtil.HAlign.Center,
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

    @Override
    public List<String> getTooltip() {
        String tooltip = dirtyChunkLocation.isDirty() ? "Dirty Chunk" : "Clean Chunk";
        return Collections.singletonList(tooltip);
    }

    @Override
    public void drawTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight) {}

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= topX && mouseX <= topX + chunkSize && mouseY >= topY && mouseY <= topY + chunkSize;
    }

    @Override
    public void onActionKeyPressed() {
        dirtyChunkLocation.onWaypointCleared();
    }

    @Override
    public IWaypointAndLocationProvider getLocationProvider() {
        return dirtyChunkLocation;
    }
}
