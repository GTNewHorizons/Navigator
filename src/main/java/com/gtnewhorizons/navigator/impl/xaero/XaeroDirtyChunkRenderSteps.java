package com.gtnewhorizons.navigator.impl.xaero;

import static com.gtnewhorizons.navigator.api.NavigatorApi.CHUNK_WIDTH;

import java.util.Collections;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.navigator.api.model.locations.IWaypointAndLocationProvider;
import com.gtnewhorizons.navigator.api.util.DrawUtils;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.InteractableRenderStep;
import com.gtnewhorizons.navigator.impl.DirtyChunkLocation;

public class XaeroDirtyChunkRenderSteps implements InteractableRenderStep {

    public final DirtyChunkLocation dirtyChunkLocation;

    private double topX, topY;

    public XaeroDirtyChunkRenderSteps(DirtyChunkLocation dirtyChunkLocation) {
        this.dirtyChunkLocation = dirtyChunkLocation;
    }

    @Override
    public void draw(@Nullable GuiScreen gui, double cameraX, double cameraZ, double scale) {
        topX = (dirtyChunkLocation.getBlockX() - 0.5 - cameraX);
        topY = (dirtyChunkLocation.getBlockZ() - 0.5 - cameraZ);
        GL11.glPushMatrix();
        GL11.glTranslated(topX, topY, 0);
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

    @Override
    public boolean isMouseOver(double mouseX, double mouseY, double scale) {
        return mouseX >= topX && mouseY >= topY && mouseX <= topX + CHUNK_WIDTH && mouseY <= topY + CHUNK_WIDTH;
    }

    @Override
    public void drawTooltip(GuiScreen gui, double mouseX, double mouseY, double scale, int scaleAdj) {
        mouseX = (mouseX * scale + (gui.mc.displayWidth >> 1)) / scaleAdj;
        mouseY = (mouseY * scale + (gui.mc.displayHeight >> 1)) / scaleAdj;

        String tooltip = dirtyChunkLocation.isDirty() ? "Dirty Chunk" : "Clean Chunk";
        GL11.glPushMatrix();
        DrawUtils.drawSimpleTooltip(
            gui,
            Collections.singletonList(tooltip),
            mouseX + 16,
            mouseY - 12,
            0xFFFFFFFF,
            0x86000000);
        GL11.glPopMatrix();
    }

    @Override
    public void onActionButton() {
        dirtyChunkLocation.onWaypointCleared();
    }

    @Override
    public IWaypointAndLocationProvider getLocationProvider() {
        return dirtyChunkLocation;
    }
}
