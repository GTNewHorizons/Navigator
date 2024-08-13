package com.gtnewhorizons.navigator.api.model.steps;

import java.awt.geom.Point2D;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

import cpw.mods.fml.common.Optional;
import journeymap.client.render.map.GridRenderer;

@Optional.InterfaceList(
    value = {
        @Optional.Interface(
            iface = "com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep",
            modid = "journeymap"),
        @Optional.Interface(
            iface = "com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep",
            modid = "xaeroworldmap") })
public class UniversalRenderStep<T extends ILocationProvider> implements JMRenderStep, XaeroRenderStep {

    protected double fontScale = 1;
    protected double rotation = 0;
    protected double width = NavigatorApi.CHUNK_WIDTH;
    protected double height = NavigatorApi.CHUNK_WIDTH;
    protected double topX;
    protected double topY;
    protected double blockSize = 1;
    protected T location;

    public UniversalRenderStep(T location) {
        this.location = location;
    }

    public void draw(double topX, double topY, float drawScale, double zoom) {

    }

    @Override
    public final void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale) {
        topX = location.getBlockX() - 0.5 - cameraX;
        topY = location.getBlockZ() - 0.5 - cameraZ;
        // fontScale = blockSize;
        GL11.glPushMatrix();
        DrawUtils.setupDrawing();
        draw(topX, topY, 1f, scale);
        GL11.glPopMatrix();
    }

    @Override
    public final void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale, float guiBasedScale) {
        topX = location.getBlockX() - 0.5 - cameraX;
        topY = location.getBlockZ() - 0.5 - cameraZ;
        // fontScale = blockSize;
        GL11.glPushMatrix();
        DrawUtils.setupDrawing();
        draw(topX, topY, guiBasedScale, scale);
        GL11.glPopMatrix();
    }

    @Override
    public final void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale,
        double fontScale, double rotation) {
        this.fontScale = fontScale;
        this.rotation = rotation;
        Point2D.Double blockAsPixel = gridRenderer.getBlockPixelInGrid(location.getBlockX(), location.getBlockZ());
        Point2D.Double pixel = new Point2D.Double(
            blockAsPixel.getX() + draggedPixelX,
            blockAsPixel.getY() + draggedPixelY);
        blockSize = Math.pow(2, gridRenderer.getZoom());
        topX = pixel.getX();
        topY = pixel.getY();
        GL11.glPushMatrix();
        DrawUtils.setupDrawing();
        draw(topX, topY, drawScale, gridRenderer.getZoom());
        GL11.glPopMatrix();
    }

    public void setFontScale(double fontScale) {
        this.fontScale = fontScale;
    }

    public double getFontScale() {
        return fontScale;
    }

    public double getAdjustedWidth() {
        return width * blockSize;
    }

    public double getAdjustedHeight() {
        return height * blockSize;
    }

    public boolean isMinimap() {
        return Minecraft.getMinecraft().currentScreen == null;
    }

    @Override
    public T getLocation() {
        return location;
    }
}
