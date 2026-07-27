package com.gtnewhorizons.navigator.api.util;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.LocationInteractableStep;

/**
 * Reused click-event view passed to {@code UniversalInteractableRenderer.withClickAction} callbacks.
 * <p>
 * Do not retain this mutable object after the callback returns. Use {@link #getLocationRenderStep()} for new code;
 * {@link #getRenderStep()} is the waypoint-only compatibility accessor.
 */
public class ClickPos {

    private boolean doubleClick;
    private int mouseX, mouseY, blockX, blockZ;
    private LocationInteractableStep renderStep;

    /**
     * Populates this event using a legacy waypoint-capable step.
     *
     * @return this reused event instance
     */
    public ClickPos set(@Nullable InteractableStep renderStep, boolean doubleClick, int mouseX, int mouseY, int blockX,
        int blockZ) {
        return set((LocationInteractableStep) renderStep, doubleClick, mouseX, mouseY, blockX, blockZ);
    }

    /**
     * Populates this event using any location-interactable step.
     *
     * @return this reused event instance
     */
    public ClickPos set(@Nullable LocationInteractableStep renderStep, boolean doubleClick, int mouseX, int mouseY,
        int blockX, int blockZ) {
        this.renderStep = renderStep;
        this.doubleClick = doubleClick;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.blockX = blockX;
        this.blockZ = blockZ;
        return this;
    }

    /** @return whether the map classified this event as a double-click */
    public boolean isDoubleClick() {
        return doubleClick;
    }

    /** @return mouse X in GUI coordinates */
    public int getMouseX() {
        return mouseX;
    }

    /** @return mouse Y in GUI coordinates */
    public int getMouseY() {
        return mouseY;
    }

    /** @return world block X under the pointer */
    public int getBlockX() {
        return blockX;
    }

    /** @return world block Z under the pointer */
    public int getBlockZ() {
        return blockZ;
    }

    /** @return chunk X under the pointer */
    public int getChunkX() {
        return Util.coordBlockToChunk(blockX);
    }

    /** @return chunk Z under the pointer */
    public int getChunkZ() {
        return Util.coordBlockToChunk(blockZ);
    }

    /**
     * Returns the hovered legacy waypoint-capable step.
     *
     * @return hovered step, or {@code null} for empty space and plain-location interactable steps
     */
    public @Nullable InteractableStep getRenderStep() {
        return renderStep instanceof InteractableStep interactableStep ? interactableStep : null;
    }

    /**
     * @return hovered location-interactable step, or {@code null} for empty space
     */
    public @Nullable LocationInteractableStep getLocationRenderStep() {
        return renderStep;
    }

}
