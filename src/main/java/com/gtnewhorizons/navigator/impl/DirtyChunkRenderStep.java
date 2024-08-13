package com.gtnewhorizons.navigator.impl;

import java.util.List;

import net.minecraft.util.EnumChatFormatting;

import com.gtnewhorizons.navigator.api.model.steps.UniversalInteractableStep;
import com.gtnewhorizons.navigator.api.util.DrawUtils;

public class DirtyChunkRenderStep extends UniversalInteractableStep<DirtyChunkLocation> {

    public DirtyChunkRenderStep(DirtyChunkLocation location) {
        super(location);
    }

    @Override
    public void draw(double topX, double topY, float drawScale, double zoom) {
        float alpha = 0.5f;
        alpha *= alpha * 204;
        int color = location.isDirty() ? 0xFF0000 : 0x00FFAA;
        DrawUtils.drawRect(topX, topY, getAdjustedWidth(), getAdjustedHeight(), color, alpha);

        if (location.isDirty()) {
            int borderAlpha = 204;
            int borderColor = 0xFFD700;
            DrawUtils
                .drawHollowRect(topX, topY, getAdjustedWidth(), getAdjustedHeight(), borderColor, borderAlpha, false);
            DrawUtils.drawLabel(
                "D",
                topX + getAdjustedWidth() / 2,
                topY + getAdjustedHeight() / 2,
                0xFFFFFFFF,
                0xB4000000,
                false,
                getFontScale());
        }
    }

    @Override
    public void getTooltip(List<String> list) {
        list.add(location.isDirty() ? "Dirty Chunk" : "Clean Chunk");
        list.add(EnumChatFormatting.DARK_GREEN + "Example Second Line");
    }
}
