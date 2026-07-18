package com.gtnewhorizons.navigator.api.model.steps;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.locations.ILocationProvider;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;

/**
 * Map-neutral render step used by JourneyMap and Xaero integrations.
 * <p>
 * Navigator calculates map-space position, applies integration scaling, configures basic OpenGL drawing state, and
 * calls {@link #draw(double, double, float, double)}. Subclasses own any additional GL state they change.
 *
 * @param <T> source location type
 */
public abstract class UniversalRenderStep<T extends ILocationProvider> implements XaeroRenderStep {

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

    /**
     * @param location source location retained for the lifetime of this cached step
     */
    public UniversalRenderStep(T location) {
        this.location = location;
    }

    /**
     * Draws the element in the active map integration.
     *
     * @param x         calculated map-space X including the configured offset
     * @param y         calculated map-space Y/Z including the configured offset
     * @param drawScale GUI/map scale supplied by the integration
     * @param zoom      integration zoom value; prefer {@link #getZoomStep()} for thresholds
     */
    public abstract void draw(double x, double y, float drawScale, double zoom);

    /**
     * Updates derived state immediately before rendering.
     *
     * @param x         current map-space X
     * @param y         current map-space Y/Z
     * @param drawScale GUI/map scale supplied by the integration
     * @param zoom      current integration zoom value
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

    /**
     * JourneyMap render entry point used by Navigator's integration.
     *
     * @param x         map-space X
     * @param y         map-space Y/Z
     * @param drawScale JourneyMap draw scale
     * @param zoom      Navigator/JourneyMap zoom step
     * @param blockSize pixels per world block
     * @param fontScale JourneyMap font scale
     * @param rotation  map rotation in degrees
     */
    public final void drawJourneyMap(double x, double y, float drawScale, double zoom, double blockSize,
        double fontScale, double rotation) {
        this.fontScale = fontScale;
        this.rotation = rotation;
        isJourneyMap = true;
        this.zoom = zoom;
        this.blockSize = blockSize;
        this.x = x;
        this.y = y;
        preRender(getX(), getY(), drawScale, zoom);
        GL11.glPushMatrix();
        DrawUtils.setupDrawing();
        draw(getX(), getY(), drawScale, zoom);
        GL11.glPopMatrix();
    }

    /**
     * Sets a custom font scale.
     * <p>
     * Constructor-time values affect Xaero. Set it from {@link #preRender(double, double, float, double)} when both
     * integrations need the value.
     *
     * @param fontScale scale multiplier
     */
    public void setFontScale(double fontScale) {
        this.fontScale = fontScale;
    }

    /**
     * Sets the logical element size in world blocks.
     *
     * @param width  width in blocks
     * @param height height in blocks
     */
    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
    }

    /** @param size square logical size in world blocks */
    public void setSize(double size) {
        setSize(size, size);
    }

    /**
     * Sets a map-space offset applied after the integration calculates the location position.
     *
     * @param offsetX horizontal offset
     * @param offsetY vertical/Z offset
     */
    public void setOffset(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    /** @param offset offset applied to both axes */
    public void setOffset(double offset) {
        setOffset(offset, offset);
    }

    /** @return current integration font scale */
    public double getFontScale() {
        return fontScale;
    }

    /**
     * @return logical width converted to the active integration's map space
     */
    public double getAdjustedWidth() {
        return width * blockSize;
    }

    /**
     * @return logical height converted to the active integration's map space
     */
    public double getAdjustedHeight() {
        return height * blockSize;
    }

    /** @return configured logical width in blocks */
    public double getWidth() {
        return width;
    }

    /** @return configured logical height in blocks */
    public double getHeight() {
        return height;
    }

    /** @return current calculated X plus configured offset */
    public double getX() {
        return x + offsetX;
    }

    /** @return current calculated Y/Z plus configured offset */
    public double getY() {
        return y + offsetY;
    }

    /**
     * Sets the lower Xaero scale bound used to keep an element legible while zooming out.
     *
     * @param minScale scale at which the step stops shrinking
     */
    public void setMinScale(int minScale) {
        setMinScale((double) minScale);
    }

    /**
     * Sets the lower Xaero scale bound used to keep an element legible while zooming out.
     *
     * @param minScale scale at which the step stops shrinking
     */
    public void setMinScale(double minScale) {
        this.minScale = minScale;
        shouldScale = true;
    }

    /**
     * @param currentScale active Xaero scale
     * @return scale clamped to the configured minimum
     */
    public double getScaling(double currentScale) {
        return Math.max(currentScale, minScale);
    }

    /**
     * @return {@code true} when no fullscreen GUI is open; primarily useful for minimizing minimap detail
     */
    public boolean isMinimap() {
        return Minecraft.getMinecraft().currentScreen == null;
    }

    /**
     * Returns a cross-map zoom step suitable for visibility thresholds.
     *
     * @return JourneyMap's supplied zoom step or Xaero's scale converted to steps {@code 0..5}
     */
    public double getZoomStep() {
        return isXaero ? getXaeroZoomAsSteps(zoom) : zoom;
    }

    /** @return source location */
    @Override
    public T getLocation() {
        return location;
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
