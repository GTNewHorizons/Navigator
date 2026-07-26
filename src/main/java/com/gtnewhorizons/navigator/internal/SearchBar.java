package com.gtnewhorizons.navigator.internal;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;

import com.gtnewhorizons.navigator.api.util.Util;
import com.gtnewhorizons.navigator.internal.nei.NEISearchFormatter;

public class SearchBar extends FormattedTextField {

    private Consumer<String> textConsumer;

    private String oldText = "";

    public SearchBar(int x, int y, int width, int height) {
        super(Minecraft.getMinecraft().fontRenderer, x, y, width, height);
        setFormatter(resolveFormatter());
    }

    private static TextFormatter resolveFormatter() {
        if (Util.isNEIInstalled()) {
            try {
                return NEISearchFormatter.create();
            } catch (Throwable ignored) {}
        }
        return TextFormatter.DEFAULT;
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
            fontRenderer.drawString("Search...", xPosition + 2, yPosition + 4, 0x808080);
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
        oldText = getText();
        if (textConsumer != null) textConsumer.accept(oldText);
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseX <= xPosition + width
            && mouseY >= yPosition
            && mouseY <= yPosition + height;
    }
}
