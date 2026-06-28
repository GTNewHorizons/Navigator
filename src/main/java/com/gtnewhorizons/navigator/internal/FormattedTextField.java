package com.gtnewhorizons.navigator.internal;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

/**
 * Yoinked from NotEnoughItems' {@code codechicken.nei.FormattedTextField}
 */
public class FormattedTextField extends GuiTextField {

    public interface TextFormatter {

        TextFormatter DEFAULT = (text) -> text;

        String format(String text);
    }

    protected FontRenderer fontRenderer;
    protected String rawText = "";
    protected String formattedText = "";
    protected boolean editable = true;
    protected int editableColor = 14737632;
    protected int notEditableColor = 7368816;
    protected TextFormatter formatter = TextFormatter.DEFAULT;
    protected int lineScrollOffset = 0;
    protected int frame = 0;

    public FormattedTextField(FontRenderer fontRenderer, int xPosition, int yPosition, int width, int height) {
        super(fontRenderer, xPosition, yPosition, width, height);
        this.fontRenderer = fontRenderer;
    }

    public void setFormatter(TextFormatter formatter) {
        this.formatter = formatter == null ? TextFormatter.DEFAULT : formatter;
    }

    @Override
    public void updateCursorCounter() {
        super.updateCursorCounter();
        ++this.frame;
    }

    @Override
    public void setFocused(boolean focus) {
        if (focus && !this.isFocused()) {
            this.frame = 0;
        }

        super.setFocused(focus);
    }

    @Override
    public void setText(String rawText) {
        super.setText(EnumChatFormatting.getTextWithoutFormattingCodes(rawText));
    }

    @Override
    public void setSelectionPos(int position) {
        String text = this.getText();
        int length = text.length();

        super.setSelectionPos(position);

        position = Math.max(0, Math.min(position, length));

        if (this.fontRenderer != null) {

            if (this.lineScrollOffset > length) {
                this.lineScrollOffset = length;
            }

            int width = this.getWidth();
            String visible = this.fontRenderer.trimStringToWidth(text.substring(this.lineScrollOffset), width);
            int visibleEnd = visible.length() + this.lineScrollOffset;

            if (position == this.lineScrollOffset) {
                this.lineScrollOffset -= this.fontRenderer.trimStringToWidth(text, width, true)
                    .length();
            }

            if (position > visibleEnd) {
                this.lineScrollOffset += position - visibleEnd;
            } else if (position <= this.lineScrollOffset) {
                this.lineScrollOffset -= this.lineScrollOffset - position;
            }

            this.lineScrollOffset = Math.max(0, Math.min(this.lineScrollOffset, length));
        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        this.editableColor = color;
    }

    @Override
    public void setDisabledTextColour(int color) {
        super.setDisabledTextColour(color);
        this.notEditableColor = color;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.editable = enabled;
    }

    @Override
    public void drawTextBox() {

        if (!this.getVisible()) {
            return;
        }

        if (this.getEnableBackgroundDrawing()) {
            drawRect(
                this.xPosition - 1,
                this.yPosition - 1,
                this.xPosition + this.width + 1,
                this.yPosition + this.height + 1,
                -6250336);
            drawRect(
                this.xPosition,
                this.yPosition,
                this.xPosition + this.width,
                this.yPosition + this.height,
                -16777216);
        }

        if (!this.rawText.equals(getText())) {
            this.rawText = getText();
            this.formattedText = this.formatter.format(this.rawText);
            if (!EnumChatFormatting.getTextWithoutFormattingCodes(this.formattedText)
                .equalsIgnoreCase(this.rawText)) {
                this.formattedText = this.rawText;
            }
        }

        // Nothing to draw when empty and unfocused; SearchBar paints its own placeholder.
        if (!isFocused() && this.formattedText.isEmpty()) {
            return;
        }

        int firstCharacterIndex = getFormattedTextShift(this.lineScrollOffset);
        String rawTextClipped = this.fontRenderer
            .trimStringToWidth(this.rawText.substring(this.lineScrollOffset), this.getWidth());
        String textClipped = this.formattedText
            .substring(firstCharacterIndex, getFormattedTextShift(this.lineScrollOffset + rawTextClipped.length()));
        int cursorPosition = getFormattedTextShift(getCursorPosition());
        int selectionEnd = getFormattedTextShift(getSelectionEnd());

        int color = this.editable ? this.editableColor : this.notEditableColor;
        int cursorA = cursorPosition - firstCharacterIndex;
        int cursorB = selectionEnd - firstCharacterIndex;
        boolean flag = cursorA >= 0 && cursorA <= textClipped.length();
        boolean flag1 = isFocused() && this.frame / 6 % 2 == 0 && flag;
        boolean flag2 = getCursorPosition() < this.rawText.length() || this.rawText.length() >= getMaxStringLength();
        int x = getEnableBackgroundDrawing() ? this.xPosition + 4 : this.xPosition;
        int y = getEnableBackgroundDrawing() ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
        int x2 = x;

        if (cursorB > textClipped.length()) {
            cursorB = textClipped.length();
        }

        if (!textClipped.isEmpty()) {
            String s1 = flag ? textClipped.substring(0, cursorA) : textClipped;
            String colorA = getPreviousColor(firstCharacterIndex);
            x2 = this.fontRenderer.drawStringWithShadow(colorA + s1, x, y, color);
        }

        int k1 = x2;

        if (!flag) {
            k1 = cursorA > 0 ? x + this.width : x;
        } else if (flag2) {
            k1 = x2 - 1;
            --x2;
        }

        if (!textClipped.isEmpty() && flag && cursorA < textClipped.length()) {
            String colorB = getPreviousColor(firstCharacterIndex + cursorA);
            this.fontRenderer.drawStringWithShadow(colorB + textClipped.substring(cursorA), x2, y, color);
        }

        if (flag1) {
            if (flag2) {
                Gui.drawRect(k1, y - 1, k1 + 1, y + 1 + this.fontRenderer.FONT_HEIGHT, -3092272);
            } else {
                this.fontRenderer.drawStringWithShadow("_", k1, y, color);
            }
        }

        if (cursorB != cursorA) {
            int l1 = x + this.fontRenderer.getStringWidth(textClipped.substring(0, cursorB));
            drawCursorVertical(k1, y - 1, l1 - 1, y + 1 + this.fontRenderer.FONT_HEIGHT);
        }
    }

    private void drawCursorVertical(int x1, int y1, int x2, int y2) {
        int i1;

        if (x1 < x2) {
            i1 = x1;
            x1 = x2;
            x2 = i1;
        }

        if (y1 < y2) {
            i1 = y1;
            y1 = y2;
            y2 = i1;
        }

        if (x2 > this.xPosition + this.width) {
            x2 = this.xPosition + this.width;
        }

        if (x1 > this.xPosition + this.width) {
            x1 = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.instance;
        GL11.glColor4f(0.0F, 0.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glLogicOp(GL11.GL_OR_REVERSE);
        tessellator.startDrawingQuads();
        tessellator.addVertex(x1, y2, 0.0D);
        tessellator.addVertex(x2, y2, 0.0D);
        tessellator.addVertex(x2, y1, 0.0D);
        tessellator.addVertex(x1, y1, 0.0D);
        tessellator.draw();
        GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private int getFormattedTextShift(int position) {
        int shift = 0;

        for (int i = 0; i < position; i++) {
            while (this.formattedText.length() > i + shift + 1 && this.formattedText.charAt(i + shift) == '§'
                && isFormattingCode(this.formattedText.charAt(i + shift + 1))) {
                shift += 2;
            }
        }

        return Math.min(position + shift, this.formattedText.length());
    }

    private static boolean isFormattingCode(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
            || (c >= 'k' && c <= 'o')
            || c == 'r'
            || (c >= 'A' && c <= 'F')
            || (c >= 'K' && c <= 'O')
            || c == 'R';
    }

    private String getPreviousColor(int position) {
        position = Math.min(position, this.formattedText.length() - 1);
        while (position >= 0) {
            if (this.formattedText.charAt(position) == '§') {
                return this.formattedText.substring(position, position + 2);
            }
            position--;
        }
        return "";
    }
}
