package com.gtnewhorizons.navigator.api.model.layers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.FontRenderer;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.model.steps.RenderStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalInteractableStep;
import com.gtnewhorizons.navigator.api.model.steps.UniversalLocationInteractableStep;
import com.gtnewhorizons.navigator.api.util.ClickPos;

/**
 * Universal renderer that adds hover, tooltip, click, action-key, and optional waypoint behavior.
 * <p>
 * A configured click callback runs before the default behavior. If it does not consume a double-click, waypoint-
 * capable locations toggle the manager's active waypoint; plain locations do nothing by default.
 */
public class UniversalInteractableRenderer extends UniversalLayerRenderer implements InteractableLayer {

    private final ClickPos clickPos = new ClickPos();
    protected InteractableLayerManager manager;
    protected UniversalInteractableStep<?> hoveredRenderStep = null;
    private UniversalLocationInteractableStep<?> hoveredLocationRenderStep = null;
    private Predicate<ClickPos> clickAction;
    private IntPredicate keyPressAction;

    /** @param manager owning interactable layer manager */
    public UniversalInteractableRenderer(@Nonnull InteractableLayerManager manager) {
        super(manager);
        this.manager = manager;
    }

    @Override
    public void onMouseMove(int mouseX, int mouseY) {
        setHoveredRenderStep(null);
        for (RenderStep drawStep : getRenderStepsForInteraction()) {
            if (drawStep instanceof UniversalLocationInteractableStep<?>step) {
                if (step.mouseOver(mouseX, mouseY)) {
                    setHoveredRenderStep(step);
                    return;
                }
            }
        }
    }

    @Override
    public final boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (clickAction != null) {
            if (clickAction
                .test(clickPos.set(hoveredLocationRenderStep, isDoubleClick, mouseX, mouseY, blockX, blockZ))) {
                return true;
            }
        }

        if (hoveredLocationRenderStep != null) {
            return onClick(isDoubleClick, mouseX, mouseY, blockX, blockZ);
        }

        return onClickOutsideRenderStep(isDoubleClick, mouseX, mouseY, blockX, blockZ);
    }

    /**
     * Handles a click on the currently hovered step using default waypoint behavior.
     *
     * @return {@code true} when a waypoint was set or cleared
     */
    public boolean onClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        if (isDoubleClick
            && hoveredLocationRenderStep.getLocation() instanceof IWaypointAndLocationProvider waypointLocation) {
            if (waypointLocation.isActiveAsWaypoint()) {
                manager.clearActiveWaypoint();
            } else {
                manager.setActiveWaypoint(waypointLocation.toWaypoint());
            }
            return true;
        }
        return false;
    }

    /**
     * Hook for clicks that do not target a render step.
     *
     * @return {@code true} when consumed
     */
    public boolean onClickOutsideRenderStep(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ) {
        return false;
    }

    @Override
    public List<String> getTooltip() {
        List<String> tooltip = new ArrayList<>();
        if (hoveredLocationRenderStep != null) {
            hoveredLocationRenderStep.getTooltip(tooltip);
        }
        return tooltip;
    }

    @Override
    public void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth,
        int displayHeight) {
        if (hoveredLocationRenderStep != null) {
            hoveredLocationRenderStep.drawCustomTooltip(fontRenderer, mouseX, mouseY, displayWidth, displayHeight);
        }
    }

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    @Override
    public boolean onKeyPressed(int keyCode) {
        if (keyPressAction != null) {
            if (keyPressAction.test(keyCode)) {
                return true;
            }
        }

        if (hoveredLocationRenderStep != null && hoveredLocationRenderStep.onKeyPressed(keyCode)) {
            manager.forceRefresh();
            return true;
        }

        return false;
    }

    /**
     * Installs a callback that runs before default step/outside click behavior.
     * <p>
     * The supplied {@link ClickPos} is mutable and reused; do not retain it.
     *
     * @param action callback returning {@code true} when it consumes a click
     * @return this renderer
     */
    public UniversalInteractableRenderer withClickAction(@Nonnull Predicate<ClickPos> action) {
        this.clickAction = action;
        return this;
    }

    /**
     * Installs a callback that runs before the hovered step's action-key handler.
     *
     * @param keyPressAction callback returning {@code true} when it consumes a key
     * @return this renderer
     */
    public UniversalInteractableRenderer withKeyPressAction(@Nonnull IntPredicate keyPressAction) {
        this.keyPressAction = keyPressAction;
        return this;
    }

    /**
     * Dispatches a click from a native overlay associated with a known step.
     *
     * @return whether the click was consumed
     */
    public boolean onRenderStepClick(UniversalLocationInteractableStep<?> step, boolean isDoubleClick, int mouseX,
        int mouseY, int blockX, int blockZ) {
        setHoveredRenderStep(step);
        boolean handled = onMapClick(isDoubleClick, mouseX, mouseY, blockX, blockZ);
        if (handled) manager.forceRefresh();
        return handled;
    }

    /**
     * Dispatches a key from a native overlay associated with a known step.
     *
     * @return whether the key was consumed
     */
    public boolean onRenderStepKeyPressed(UniversalLocationInteractableStep<?> step, int keyCode) {
        setHoveredRenderStep(step);
        return onKeyPressed(keyCode);
    }

    /** Sets native-overlay hover to the associated render step. */
    public void setRenderStepHover(UniversalLocationInteractableStep<?> step) {
        setHoveredRenderStep(step);
    }

    /** Clears native-overlay hover only if it still points at {@code step}. */
    public void clearRenderStepHover(UniversalLocationInteractableStep<?> step) {
        if (hoveredLocationRenderStep == step) setHoveredRenderStep(null);
    }

    /** Clears all cached hover state. */
    public void clearRenderStepHover() {
        setHoveredRenderStep(null);
    }

    private void setHoveredRenderStep(@Nullable UniversalLocationInteractableStep<?> step) {
        hoveredLocationRenderStep = step;
        hoveredRenderStep = step instanceof UniversalInteractableStep<?>waypointStep ? waypointStep : null;
    }

}
