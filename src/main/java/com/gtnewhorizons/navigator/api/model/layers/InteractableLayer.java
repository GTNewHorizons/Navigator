package com.gtnewhorizons.navigator.api.model.layers;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

public interface InteractableLayer {

    void onMouseMove(int mouseX, int mouseY);

    boolean onMapClick(boolean isDoubleClick, int mouseX, int mouseY, int blockX, int blockZ);

    List<String> getTooltip();

    void drawCustomTooltip(FontRenderer fontRenderer, int mouseX, int mouseY, int displayWidth, int displayHeight);

    /**
     * @param keyCode The key code of the key that was pressed
     * @return true if the key press was handled, false otherwise
     */
    boolean onKeyPressed(int keyCode);
}
