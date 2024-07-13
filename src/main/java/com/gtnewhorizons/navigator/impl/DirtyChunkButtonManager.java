package com.gtnewhorizons.navigator.impl;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;

public class DirtyChunkButtonManager extends ButtonManager {

    public static final DirtyChunkButtonManager instance = new DirtyChunkButtonManager();

    @Override
    public ResourceLocation getIcon(SupportedMods mod, String theme) {
        return new ResourceLocation(Navigator.MODID, "textures/icon/nodes.png");
    }

    @Override
    public String getButtonText() {
        return EnumChatFormatting.AQUA + StatCollector.translateToLocal("navigator.button.dirty_chunk");
    }
}
