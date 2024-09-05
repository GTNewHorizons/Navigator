package com.gtnewhorizons.navigator.api.model.steps;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.joml.Vector2d;
import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

import cpw.mods.fml.common.Optional;
import journeymap.client.render.map.GridRenderer;

@Optional.Interface(iface = "com.gtnewhorizons.navigator.api.journeymap.drawsteps.JMRenderStep", modid = "journeymap")
public abstract class UniversalRenderStep<T extends ILocationProvider> implements JMRenderStep, XaeroRenderStep {

    private final Vector2d pos = new Vector2d();
    protected double fontScale = 1;
    protected double rotation = 0;
    protected double width = NavigatorApi.CHUNK_WIDTH;
    protected double height = NavigatorApi.CHUNK_WIDTH;
    protected double x;
    protected double y;
    protected double blockSize = 1;
    protected T location;
    protected double offsetX, offsetY;
    protected double zoom;
    protected boolean isJourneyMap, isXaero;
    protected double minScale;
    protected boolean shouldScale;

    public UniversalRenderStep(T location) {
        this.location = location;
    }

    public abstract void draw(double x, double y, float drawScale, double zoom);

    /**
     * Do any setup that needs to happen prior to rendering.
     */
    public void preRender(double x, double y, float drawScale, double zoom) {}

    @Override
    public final void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale) {}

    @Override
    public final void draw(double cameraX, double cameraZ, double scale, float guiBasedScale) {
        isXaero = true;
        x = location.getBlockX() - 0.5 - cameraX;
        y = location.getBlockZ() - 0.5 - cameraZ;
        zoom = scale;
        preRender(getX(), getY(), guiBasedScale, scale);
        GL11.glPushMatrix();
        if (shouldScale) {
            double scaling = getScaling(scale);
            x *= scaling;
            y *= scaling;
            GL11.glScaled(1 / scaling, 1 / scaling, 1);
        }
        DrawUtils.setupDrawing();
        draw(getX(), getY(), guiBasedScale, scale);
        GL11.glPopMatrix();
    }

    @Override
    public final void draw(double draggedPixelX, double draggedPixelY, GridRenderer gridRenderer, float drawScale,
        double fontScale, double rotation) {
        this.fontScale = fontScale;
        this.rotation = rotation;
        isJourneyMap = true;
        zoom = gridRenderer.getZoom();
        Vector2d blockPos = getBlockFromGrid(
            gridRenderer,
            draggedPixelX,
            draggedPixelY,
            location.getBlockX(),
            location.getBlockZ());
        x = blockPos.x;
        y = blockPos.y;
        preRender(getX(), getY(), drawScale, zoom);
        GL11.glPushMatrix();
        DrawUtils.setupDrawing();
        draw(getX(), getY(), drawScale, zoom);
        GL11.glPopMatrix();
    }

    /**
     * Only applies to Xaero's if used in the constructor.
     * If needed for both mods it should be set in {@link #preRender(double, double, float, double)}
     */
    public void setFontScale(double fontScale) {
        this.fontScale = fontScale;
    }

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void setSize(double size) {
        setSize(size, size);
    }

    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setOffset(double offset) {
        setOffset(offset, offset);
    }

    public double getFontScale() {
        return fontScale;
    }

    /**
     * Gives a consistent width regardless of mod
     */
    public double getAdjustedWidth() {
        return width * blockSize;
    }

    /**
     * Gives a consistent height regardless of mod
     */
    public double getAdjustedHeight() {
        return height * blockSize;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getX() {
        return x + offsetX;
    }

    public double getY() {
        return y + offsetY;
    }

    /**
     * Only relevant for Xaero's maps
     * 
     * @param minScale the zoom level at which the RenderStep should stop scaling
     */
    public void setMinScale(int minScale) {
        this.minScale = minScale;
        shouldScale = true;
    }

    public double getScaling(double currentScale) {
        return Math.max(currentScale, minScale);
    }

    public boolean isMinimap() {
        return Minecraft.getMinecraft().currentScreen == null;
    }

    public double getZoomStep() {
        return isXaero ? getXaeroZoomAsSteps(zoom) : zoom;
    }

    @Override
    public T getLocation() {
        return location;
    }

    private Vector2d getBlockFromGrid(GridRenderer gridRenderer, double pixelX, double pixelY, double x, double z) {
        blockSize = (int) Math.pow(2.0, gridRenderer.getZoom());
        double localBlockX = x - gridRenderer.getCenterBlockX();
        double localBlockZ = z - gridRenderer.getCenterBlockZ();
        double pixelOffsetX = (double) (gridRenderer.getWidth() / 2) + localBlockX * blockSize;
        double pixelOffsetZ = (double) (gridRenderer.getHeight() / 2) + localBlockZ * blockSize;
        return pos.set(pixelOffsetX + pixelX, pixelOffsetZ + pixelY);
    }

    private int getXaeroZoomAsSteps(double zoom) {
        if (zoom < 1) return 0;
        if (zoom <= 2) return 1;
        if (zoom <= 6) return 2;
        if (zoom <= 10) return 3;
        if (zoom <= 30) return 4;
        return 5;
    }
}
