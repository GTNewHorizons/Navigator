package com.gtnewhorizons.navigator.api.model.markers;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

/**
 * Map-neutral description of a point marker. Map integrations decide how to display it.
 */
public final class MapMarker {

    private final @Nullable BufferedImage image;
    private final @Nullable ResourceLocation imageLocation;
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

    public MapMarker(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
        imageLocation = null;
        textureWidth = image.getWidth();
        textureHeight = image.getHeight();
        displayWidth = textureWidth;
        displayHeight = textureHeight;
    }

    public MapMarker(ResourceLocation imageLocation, int textureWidth, int textureHeight) {
        image = null;
        this.imageLocation = Objects.requireNonNull(imageLocation);
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        displayWidth = this.textureWidth;
        displayHeight = this.textureHeight;
    }

    public MapMarker setDisplaySize(double width, double height) {
        displayWidth = width;
        displayHeight = height;
        return this;
    }

    public MapMarker setLabel(@Nullable String label) {
        this.label = label;
        return this;
    }

    public MapMarker setTooltip(@Nullable List<String> tooltip) {
        this.tooltip = tooltip == null ? null : Collections.unmodifiableList(new ArrayList<>(tooltip));
        return this;
    }

    public MapMarker setLabelColor(int labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    public MapMarker setLabelScale(float labelScale) {
        this.labelScale = labelScale;
        return this;
    }

    public MapMarker setLabelBackgroundOpacity(float labelBackgroundOpacity) {
        this.labelBackgroundOpacity = labelBackgroundOpacity;
        return this;
    }

    public MapMarker setLabelOffsetY(int labelOffsetY) {
        this.labelOffsetY = labelOffsetY;
        return this;
    }

    /**
     * Sets the minimum Navigator zoom step where the label is visible.
     */
    public MapMarker setLabelMinZoom(int labelMinZoom) {
        this.labelMinZoom = labelMinZoom;
        return this;
    }

    public MapMarker setLabelOnMinimap(boolean labelOnMinimap) {
        this.labelOnMinimap = labelOnMinimap;
        return this;
    }

    public @Nullable BufferedImage getImage() {
        return image;
    }

    public @Nullable ResourceLocation getImageLocation() {
        return imageLocation;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public double getDisplayWidth() {
        return displayWidth;
    }

    public double getDisplayHeight() {
        return displayHeight;
    }

    public @Nullable String getLabel() {
        return label;
    }

    public @Nullable List<String> getTooltip() {
        return tooltip;
    }

    public int getLabelColor() {
        return labelColor;
    }

    public float getLabelScale() {
        return labelScale;
    }

    public float getLabelBackgroundOpacity() {
        return labelBackgroundOpacity;
    }

    public int getLabelOffsetY() {
        return labelOffsetY;
    }

    public @Nullable Integer getLabelMinZoom() {
        return labelMinZoom;
    }

    public boolean isLabelOnMinimap() {
        return labelOnMinimap;
    }
}
