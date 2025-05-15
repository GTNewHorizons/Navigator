package com.gtnewhorizons.navigator.internal;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class SearchBar extends GuiTextField {

    private Consumer<String> textConsumer;

    private String oldText = "";

    public SearchBar(int x, int y, int width, int height) {
        super(Minecraft.getMinecraft().fontRenderer, x, y, width, height);
    }

    @Override
    public void mouseClicked(int x, int y, int button) {
        if (isHovered(x, y) && button == 1) {
            setText("");
        }

        super.mouseClicked(x, y, button);
    }

    @Override
    public void drawTextBox() {
        super.drawTextBox();
        if (!isFocused() && getText().isEmpty()) {
            Minecraft.getMinecraft().fontRenderer.drawString("Search...", xPosition + 2, yPosition + 4, 0x808080);
        }

        if (!getText().equals(oldText)) {
            oldText = getText();
            if (textConsumer != null) {
                textConsumer.accept(getText());
            }
        }
    }

    public void setTextConsumer(Consumer<String> textConsumer) {
        this.textConsumer = textConsumer;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseX <= xPosition + width
            && mouseY >= yPosition
            && mouseY <= yPosition + height;
    }
}
