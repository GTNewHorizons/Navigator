package com.gtnewhorizons.navigator.api.model.markers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.navigator.api.model.steps.UniversalRenderStep;

/**
 * Map-neutral description of a JourneyMap 6 native point marker.
 * <p>
 * Navigator creates and owns the native overlay, enables fullscreen and minimap contexts, centers image anchors, and
 * forwards interaction. JourneyMap 5 and Xaero continue using the renderer's universal render step.
 */
public final class MapMarker {

    private final @Nullable BufferedImage image;
    private final @Nullable ResourceLocation imageLocation;
    private final int textureX;
    private final int textureY;
    private final int textureWidth;
    private final int textureHeight;
    private double displayWidth;
    private double displayHeight;
    private @Nullable String label;
    private @Nullable List<String> tooltip;
    private int labelColor = 0xFFFFFF;
    private float labelScale = 1.0F;
    private float labelBackgroundOpacity;
    private int labelOffsetY;
    private @Nullable Integer labelMinZoom;
    private boolean labelOnMinimap = true;
    private @Nullable double[] displayZoomScale;
    private @Nullable double[] labelZoomScale;

    /**
     * Creates a marker from an in-memory image.
     *
     * @param image non-null image whose pixel dimensions become the initial display size
     */
    public MapMarker(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
        imageLocation = null;
        textureX = 0;
        textureY = 0;
        textureWidth = image.getWidth();
        textureHeight = image.getHeight();
        displayWidth = textureWidth;
        displayHeight = textureHeight;
    }

    /**
     * Creates a marker from a Minecraft texture.
     *
     * @param imageLocation texture resource
     * @param textureWidth  source texture width in pixels
     * @param textureHeight source texture height in pixels
     */
    public MapMarker(ResourceLocation imageLocation, int textureWidth, int textureHeight) {
        this(imageLocation, 0, 0, textureWidth, textureHeight);
    }

    /**
     * Creates a marker from a sprite already stitched into a Minecraft texture atlas.
     *
     * @param atlasLocation texture atlas resource
     * @param sprite        stitched sprite; animated atlas updates remain visible
     */
    public MapMarker(ResourceLocation atlasLocation, TextureAtlasSprite sprite) {
        this(atlasLocation, sprite.getOriginX(), sprite.getOriginY(), sprite.getIconWidth(), sprite.getIconHeight());
    }

    /**
     * Creates a marker from a region of a Minecraft texture, such as an animated block-atlas sprite.
     *
     * @param imageLocation texture resource
     * @param textureX      source region X in pixels
     * @param textureY      source region Y in pixels
     * @param textureWidth  source region width in pixels
     * @param textureHeight source region height in pixels
     */
    public MapMarker(ResourceLocation imageLocation, int textureX, int textureY, int textureWidth, int textureHeight) {
        image = null;
        this.imageLocation = Objects.requireNonNull(imageLocation);
        this.textureX = Math.max(0, textureX);
        this.textureY = Math.max(0, textureY);
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        displayWidth = this.textureWidth;
        displayHeight = this.textureHeight;
    }

    /**
     * @param width  displayed width in map pixels
     * @param height displayed height in map pixels
     * @return this marker
     */
    public MapMarker setDisplaySize(double width, double height) {
        displayWidth = width;
        displayHeight = height;
        return this;
    }

    /** Scales the icon over normalized zoom steps on JourneyMap 6's fullscreen map. */
    public MapMarker setDisplayZoomScale(double minScale, double maxScale, double minZoom, double maxZoom) {
        displayZoomScale = zoomScale(minScale, maxScale, minZoom, maxZoom);
        return this;
    }

    /** @return this marker */
    public MapMarker setLabel(@Nullable String label) {
        this.label = label;
        return this;
    }

    /**
     * Copies tooltip lines into an immutable list.
     * <p>
     * When no tooltip is supplied, Navigator obtains lines from an associated interactable render step.
     *
     * @return this marker
     */
    public MapMarker setTooltip(@Nullable List<String> tooltip) {
        this.tooltip = tooltip == null ? null : Collections.unmodifiableList(new ArrayList<>(tooltip));
        return this;
    }

    /** @return this marker */
    public MapMarker setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    /** @return this marker */
    public MapMarker setLabelScale(float labelScale) {
        this.labelScale = labelScale;
        return this;
    }

    /** Scales the label over normalized zoom steps on JourneyMap 6's fullscreen map. */
    public MapMarker setLabelZoomScale(double minScale, double maxScale, double minZoom, double maxZoom) {
        labelZoomScale = zoomScale(minScale, maxScale, minZoom, maxZoom);
        return this;
    }

    /** @return this marker */
    public MapMarker setLabelBackgroundOpacity(float labelBackgroundOpacity) {
        this.labelBackgroundOpacity = labelBackgroundOpacity;
        return this;
    }

    /** @return this marker */
    public MapMarker setLabelOffsetY(int labelOffsetY) {
        this.labelOffsetY = labelOffsetY;
        return this;
    }

    /**
     * Sets the minimum Navigator zoom step where the label is visible.
     *
     * @param labelMinZoom normalized Navigator zoom step
     * @return this marker
     */
    public MapMarker setLabelMinZoom(int labelMinZoom) {
        this.labelMinZoom = labelMinZoom;
        return this;
    }

    /**
     * Controls only the label context; the icon remains enabled on the minimap.
     *
     * @param labelOnMinimap whether marker text is visible on the minimap
     * @return this marker
     */
    public MapMarker setLabelOnMinimap(boolean labelOnMinimap) {
        this.labelOnMinimap = labelOnMinimap;
        return this;
    }

    /** @return in-memory image, or {@code null} when backed by a resource */
    public @Nullable BufferedImage getImage() {
        return image;
    }

    /** @return texture resource, or {@code null} when backed by an in-memory image */
    public @Nullable ResourceLocation getImageLocation() {
        return imageLocation;
    }

    /** @return source texture X */
    public int getTextureX() {
        return textureX;
    }

    /** @return source texture Y */
    public int getTextureY() {
        return textureY;
    }

    /** @return source texture width */
    public int getTextureWidth() {
        return textureWidth;
    }

    /** @return source texture height */
    public int getTextureHeight() {
        return textureHeight;
    }

    /** @return displayed marker width */
    public double getDisplayWidth() {
        return displayWidth;
    }

    /** @return displayed marker height */
    public double getDisplayHeight() {
        return displayHeight;
    }

    /** @return fullscreen icon multiplier for the supplied normalized zoom step */
    public double getDisplayZoomScale(double zoomStep) {
        return interpolate(displayZoomScale, zoomStep);
    }

    /** @return marker label, or {@code null} */
    public @Nullable String getLabel() {
        return label;
    }

    /** @return immutable tooltip lines, or {@code null} to use the render step tooltip */
    public @Nullable List<String> getTooltip() {
        return tooltip;
    }

    /** @return RGB label color */
    public int getLabelColor() {
        return labelColor;
    }

    /** @return label scale multiplier */
    public float getLabelScale() {
        return labelScale;
    }

    /** @return fullscreen label multiplier for the supplied normalized zoom step */
    public double getLabelZoomScale(double zoomStep) {
        return interpolate(labelZoomScale, zoomStep);
    }

    /** @return label background opacity */
    public float getLabelBackgroundOpacity() {
        return labelBackgroundOpacity;
    }

    /** @return label Y offset */
    public int getLabelOffsetY() {
        return labelOffsetY;
    }

    /** @return minimum normalized zoom step, or {@code null} for JourneyMap's default */
    public @Nullable Integer getLabelMinZoom() {
        return labelMinZoom;
    }

    /** @return whether label text is enabled on the minimap */
    public boolean isLabelOnMinimap() {
        return labelOnMinimap;
    }

    private static double[] zoomScale(double minScale, double maxScale, double minZoom, double maxZoom) {
        if (minScale <= 0 || maxScale <= 0) throw new IllegalArgumentException("zoom scales must be positive");
        if (maxZoom <= minZoom) throw new IllegalArgumentException("maxZoom must be greater than minZoom");
        return new double[] { minScale, maxScale, minZoom, maxZoom };
    }

    private static double interpolate(@Nullable double[] scale, double zoomStep) {
        return scale == null ? 1
            : UniversalRenderStep.interpolateZoomScale(zoomStep, scale[0], scale[1], scale[2], scale[3]);
    }
}
