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
        int color = location.isDirty() ? 0xFF0000 : 0x00FFAA;
        DrawUtils.drawRect(topX, topY, getAdjustedWidth(), getAdjustedHeight(), color, 120);

        if (location.isDirty()) {
            DrawUtils.drawHollowRect(topX, topY, getAdjustedWidth(), getAdjustedHeight(), 0xFFD700, 204);
            DrawUtils.drawLabel(
                "D",
                topX + getAdjustedWidth() / 2,
                topY + getAdjustedHeight() / 2,
                0xFFFFFFFF,
                0xB4000000,
                false,
                fontScale);
        }
    }

    @Override
    public void getTooltip(List<String> list) {
        list.add(location.isDirty() ? "Dirty Chunk" : "Clean Chunk");
        list.add(EnumChatFormatting.DARK_GREEN + "Example Second Line");
    }
}
