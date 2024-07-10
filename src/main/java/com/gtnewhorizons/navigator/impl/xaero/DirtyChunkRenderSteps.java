package com.gtnewhorizons.navigator.impl.xaero;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class DirtyChunkRenderSteps implements XaeroRenderStep {

    public final DirtyChunkLocation dirtyChunkLocation;

    public DirtyChunkRenderSteps(DirtyChunkLocation dirtyChunkLocation) {
        this.dirtyChunkLocation = dirtyChunkLocation;
    }

    @Override
    public void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale) {
        GL11.glPushMatrix();
        GL11.glTranslated(
            dirtyChunkLocation.getBlockX() - 0.5 - cameraX,
            dirtyChunkLocation.getBlockZ() - 0.5 - cameraZ,
            0);
        float alpha = 0.5f;
        alpha *= alpha * 204;
        int color = dirtyChunkLocation.isDirty() ? 0xFF0000 : 0x00FFAA;
        int mainColor = color | (((int) alpha) << 24);
        DrawUtils.drawGradientRect(0, 0, 16, 16, 0, mainColor, mainColor);

        if (dirtyChunkLocation.isDirty()) {
            final int borderAlpha = 204;
            final int borderColor = 0xFFD700 | (borderAlpha << 24);
            DrawUtils.drawGradientRect(0, 0, 15, 1, 0, borderColor, borderColor);
            DrawUtils.drawGradientRect(15, 0, 16, 15, 0, borderColor, borderColor);
            DrawUtils.drawGradientRect(1, 15, 16, 16, 0, borderColor, borderColor);
            DrawUtils.drawGradientRect(0, 1, 1, 16, 0, borderColor, borderColor);
            if (gui != null) {
                DrawUtils.drawSimpleLabel(gui, "D", 13, 13, 0xFFFFFFFF, 0xB4000000, false);
            }
        }
        GL11.glPopMatrix();
    }
}
