package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalInteractableStep;
import com.gtnewhorizons.navigator.api.util.ClickPos;
import com.gtnewhorizons.navigator.api.util.Util;

public class UniversalInteractableRenderer extends UniversalLayerRenderer implements InteractableLayer {

    private final ClickPos clickPos = new ClickPos();
    protected InteractableLayerManager manager;
    protected UniversalInteractableStep<?> hoveredRenderStep = null;
    private Predicate<ClickPos> clickAction;
    private IntPredicate keyPressAction;

    public UniversalInteractableRenderer(InteractableLayerManager manager) {
        super(manager);
        this.manager = manager;
    }

    public void onMouseMove(int mouseX, int mouseY) {
        hoveredRenderStep = null;
        for (RenderStep drawStep : getRenderStepsForInteraction()) {
            if (drawStep instanceof UniversalInteractableStep<?>step) {
                if (step.mouseOver(mouseX, mouseY)) {
                    hoveredRenderStep = step;
                    return;
                }
            }
        }
    }

    public final boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (clickAction != null) {
            if (clickAction.test(clickPos.set(hoveredRenderStep, isDoubleClick, mouseX, mouseY, blockX, blockZ))) {
                return true;
            }
        }

        if (hoveredRenderStep != null) {
            return onClick(isDoubleClick, mouseX, mouseY, blockX, blockZ);
        }

        return onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, blockX, blockZ);
    }

    public boolean onClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (isDoubleClick) {
            if (hoveredRenderStep.getLocation()
                .isActiveAsWaypoint()) {
                manager.clearActiveWaypoint();
            } else {
                manager.setActiveWaypoint(
                    hoveredRenderStep.getLocation()
                        .toWaypoint());
            }
            return true;
        }
        return false;
    }

    public boolean onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        return false;
    }

    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        if (hoveredRenderStep != null) {
            hoveredRenderStep.getTooltip(tooltip);
        }
        return tooltip;
    }

    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {
        if (hoveredRenderStep != null) {
            hoveredRenderStep.drawCustomTooltip(fontRenderer, mouseX, mouseY, displayWidth, displayHeight);
        }
    }

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    public boolean onKeyPressed(int keyCode) {
        if (keyPressAction != null) {
            if (keyPressAction.test(keyCode)) {
                return true;
            }
        }

        if (Util.isKeyPressed(NavigatorApi.ACTION_KEY) && hoveredRenderStep != null) {
            hoveredRenderStep.onActionKeyPressed();
            manager.forceRefresh();
            return true;
        }
        return false;
    }

    public UniversalInteractableRenderer withClickAction(Predicate<ClickPos> action) {
        this.clickAction = action;
        return this;
    }

    public UniversalInteractableRenderer withKeyPressAction(IntPredicate keyPressAction) {
        this.keyPressAction = keyPressAction;
        return this;
    }

}