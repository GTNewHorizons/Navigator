package com.gtnewhorizons.navigator.api.util;

import javax.annotation.Nullable;

import com.gtnewhorizons.navigator.api.model.steps.InteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.LocationInteractableStep;

public class ClickPos {

    private boolean doubleClick;
    private int mouseX, mouseY, blockX, blockZ;
    private LocationInteractableStep renderStep;

    public ClickPos set(@Nullable InteractableStep renderStep, boolean doubleClick, int mouseX, int mouseY, int blockX,
        int blockZ) {
        return set((LocationInteractableStep) renderStep, doubleClick, mouseX, mouseY, blockX, blockZ);
    }

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

    public boolean isDoubleClick() {
        return doubleClick;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public int getChunkX() {
        return Util.coordBlockToChunk(blockX);
    }

    public int getChunkZ() {
        return Util.coordBlockToChunk(blockZ);
    }

    public @Nullable InteractableStep getRenderStep() {
        return renderStep instanceof InteractableStep interactableStep ? interactableStep : null;
    }

    public @Nullable LocationInteractableStep getLocationRenderStep() {
        return renderStep;
    }

}
