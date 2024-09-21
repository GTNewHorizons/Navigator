package com.gtnewhorizons.navigator.api.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class DrawUtils {

    public static void drawGradientRect(double minPixelX, double minPixelY, double maxPixelX, double maxPixelY,
        double z, int colorA, int colorB) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        float alpha = (colorA >> 24 & 255) / 255.0f;
        float red = (colorA >> 16 & 255) / 255.0f;
        float green = (colorA >> 8 & 255) / 255.0f;
        float blue = (colorA & 255) / 255.0f;
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(maxPixelX, minPixelY, z);
        tessellator.addVertex(minPixelX, minPixelY, z);

        alpha = (colorB >> 24 & 255) / 255.0f;
        red = (colorB >> 16 & 255) / 255.0f;
        green = (colorB >> 8 & 255) / 255.0f;
        blue = (colorB & 255) / 255.0f;
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertex(minPixelX, maxPixelY, z);
        tessellator.addVertex(maxPixelX, maxPixelY, z);

        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawGradientRect(double minPixelX, double minPixelY, double maxPixelX, double maxPixelY,
        int colorA, int colorB) {
        drawGradientRect(minPixelX, minPixelY, maxPixelX, maxPixelY, 300, colorA, colorB);
    }

    public static void drawQuad(ResourceLocation texture, double x, double y, double width, double height, int color,
        int alpha) {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        addRectToBufferWithUV(tessellator, x, y, width, height, color, alpha, 0, 0, 1, 1);
        tessellator.draw();
    }

    public static void drawQuad(ResourceLocation texture, double x, double y, double width, double height, int color,
        float alpha) {
        drawQuad(texture, x, y, width, height, color, (int) alpha);
    }

    public static void drawQuad(IIcon icon, double x, double y, double width, double height, int color, int alpha) {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(TextureMap.locationBlocksTexture);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        addRectToBufferWithUV(
            tessellator,
            x,
            y,
            width,
            height,
            color,
            alpha,
            icon.getMinU(),
            icon.getMinV(),
            icon.getMaxU(),
            icon.getMaxV());
        tessellator.draw();
    }

    public static void drawQuad(IIcon icon, double x, double y, double width, double height, int color, float alpha) {
        drawQuad(icon, x, y, width, height, color, (int) alpha);
    }

    public static void addRectToBuffer(Tessellator tessellator, double x, double y, double w, double h, int color,
        int alpha) {
        int[] c = ints(color, alpha);
        tessellator.setColorRGBA(c[0], c[1], c[2], c[3]);
        tessellator.addVertex(x, y + h, 0D);
        tessellator.addVertex(x + w, y + h, 0D);
        tessellator.addVertex(x + w, y, 0D);
        tessellator.addVertex(x, y, 0D);
    }

    public static void addRectToBufferWithUV(Tessellator tessellator, double x, double y, double w, double h, int color,
        int alpha, double u0, double v0, double u1, double v1) {
        int[] c = ints(color, alpha);
        tessellator.setColorRGBA(c[0], c[1], c[2], c[3]);
        tessellator.addVertexWithUV(x, y + h, 0D, u0, v1);
        tessellator.addVertexWithUV(x + w, y + h, 0D, u1, v1);
        tessellator.addVertexWithUV(x + w, y, 0D, u1, v0);
        tessellator.addVertexWithUV(x, y, 0D, u0, v0);
    }

    public static void drawRect(double x, double y, double w, double h, int color, int alpha) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        addRectToBuffer(tessellator, x, y, w, h, color, alpha);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void setupDrawing() {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    }

    public static void drawSimpleLabel(GuiScreen gui, String text, double textX, double textY, int fontColor,
        int bgColor, boolean centered) {
        GL11.glPushMatrix();
        double textWidth = gui.mc.fontRenderer.getStringWidth(text);
        double xOffsetL = centered ? -textWidth / 2.0 - 2 : -2;
        double xOffsetR = centered ? textWidth / 2.0 + 2 : textWidth + 2;
        drawRect(textX + xOffsetL, textY - 2, xOffsetR, gui.mc.fontRenderer.FONT_HEIGHT + 2, bgColor, 180);
        if (centered) gui.drawCenteredString(gui.mc.fontRenderer, text, (int) textX, (int) textY, fontColor);
        else gui.drawString(gui.mc.fontRenderer, text, (int) textX, (int) textY, fontColor);
        GL11.glPopMatrix();
    }

    public static void drawSimpleLabel(String text, double textX, double textY, int fontColor, int bgColor,
        boolean centered) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRenderer;
        GL11.glPushMatrix();
        double dTextX = textX - (double) (int) textX;
        double dTextY = textY - (double) (int) textY;
        double textWidth = fontRenderer.getStringWidth(text);
        double xOffsetL = centered ? -textWidth / 2.0 - 2 : -2;
        double xOffsetR = centered ? textWidth / 2.0 + 2 : textWidth + 2;
        GL11.glTranslated(dTextX, dTextY, 0.0);
        drawRect(textX + xOffsetL, textY - 2, xOffsetR, fontRenderer.FONT_HEIGHT + 2, bgColor, 180);
        if (centered) fontRenderer.drawStringWithShadow(text, (int) (textX - textWidth / 2), (int) textY, fontColor);
        else fontRenderer.drawString(text, (int) textX, (int) textY, fontColor);
        GL11.glPopMatrix();
    }

    public static void drawHollowRect(double x, double y, double w, double h, int col, int alpha) {
        drawHollowRect(x, y, w, h, col, alpha, 1);
    }

    public static void drawHollowRect(double x, double y, double w, double h, int col, int alpha, double thickness) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        addRectToBuffer(tessellator, x, y + thickness, thickness, h - 1 - thickness, col, alpha);
        addRectToBuffer(tessellator, x + w - thickness, y + thickness, thickness, h - 1 - thickness, col, alpha);
        addRectToBuffer(tessellator, x, y, w, thickness, col, alpha);
        addRectToBuffer(tessellator, x, y + h - thickness, w, thickness, col, alpha);

        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawSimpleTooltip(GuiScreen gui, List<String> text, double x, double y, int fontColor,
        int bgColor) {
        if (text.isEmpty()) return;

        int maxTextWidth = 0;
        for (String str : text) {
            int strWidth = gui.mc.fontRenderer.getStringWidth(str);
            if (strWidth > maxTextWidth) maxTextWidth = strWidth;
        }

        int boxWidth = maxTextWidth + 6;
        int boxHeight = text.size() * (gui.mc.fontRenderer.FONT_HEIGHT + 2) + 6;

        double dx = x - (double) (int) x;
        double dy = y - (double) (int) y;

        GL11.glPushMatrix();

        drawRect(x, y, boxWidth, boxHeight, bgColor, 180);
        GL11.glTranslated(dx, dy, 301);
        for (int i = 0; i < text.size(); i++) {
            gui.drawString(
                gui.mc.fontRenderer,
                text.get(i),
                (int) x + 3,
                (int) y + 3 + i * (gui.mc.fontRenderer.FONT_HEIGHT + 2),
                fontColor);
        }

        GL11.glPopMatrix();
    }

    public static void drawLabel(String text, double textX, double textY, int fontColor, int bgColor,
        boolean centered) {
        drawLabel(text, textX, textY, fontColor, bgColor, centered, 1.0);
    }

    public static void drawLabel(String text, double textX, double textY, int fontColor, int bgColor, boolean centered,
        double fontScale) {
        drawLabel(text, textX, textY, fontColor, bgColor, centered, true, fontScale);
    }

    public static void drawLabel(String text, double textX, double textY, int fontColor, int bgColor, boolean centered,
        boolean fontShadow, double fontScale) {
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        GL11.glPushMatrix();

        if (fontScale != 1.0) {
            textX /= fontScale;
            textY /= fontScale;
            GL11.glScaled(fontScale, fontScale, 0);
        }

        double dTextX = textX - (double) (int) textX;
        double dTextY = textY - (double) (int) textY;
        double textWidth = fontRenderer.getStringWidth(text);
        double xOffsetL = centered ? -textWidth / 2.0 - 2 : -2;
        GL11.glTranslated(dTextX, dTextY, 0.0);
        drawRect(textX + xOffsetL, textY - 2, textWidth + 2, fontRenderer.FONT_HEIGHT + 2, bgColor, 180);
        if (fontShadow) {
            fontRenderer.drawStringWithShadow(
                text,
                (centered ? (int) (textX - textWidth / 2.0) : (int) textX),
                (int) textY,
                fontColor);
        } else {
            fontRenderer
                .drawString(text, (centered ? (int) (textX - textWidth / 2.0) : (int) textX), (int) textY, fontColor);
        }
        GL11.glPopMatrix();
    }

    public static float[] floats(int rgb) {
        return new float[] { (float) (rgb >> 16 & 255) / 255.0F, (float) (rgb >> 8 & 255) / 255.0F,
            (float) (rgb & 255) / 255.0F };
    }

    public static int[] ints(int rgb, int alpha) {
        return new int[] { (rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, alpha & 255 };
    }
}
