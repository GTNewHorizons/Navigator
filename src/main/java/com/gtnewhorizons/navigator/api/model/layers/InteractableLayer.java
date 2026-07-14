package com.gtnewhorizons.navigator.api.model.layers;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

/** Input and tooltip contract implemented by renderers that can consume map interaction. */
public interface InteractableLayer {

    /** Updates the renderer's hovered step from GUI-space mouse coordinates. */
    void onMouseMove(int mouseX, int mouseY);

    /**
     * Dispatches a map click.
     *
     * @return {@code true} when Navigator consumed the click
     */
    boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ);

    /** @return tooltip lines for the current hovered step */
    List<String> getTooltip();

    /** Draws any step-specific custom tooltip content. */
    void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    boolean onKeyPressed(int keyCode);
}
