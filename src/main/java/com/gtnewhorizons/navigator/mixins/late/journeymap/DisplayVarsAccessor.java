package com.gtnewhorizons.navigator.mixins.late.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import journeymap.client.ui.minimap.DisplayVars;
import journeymap.client.ui.minimap.Shape;

@Mixin(DisplayVars.class)
public interface DisplayVarsAccessor {

    @Accessor(remap = false)
    float getDrawScale();

    @Accessor(remap = false)
    double getFontScale();

    @Accessor(remap = false)
    Shape getShape();

    @Accessor(remap = false)
    int getMinimapWidth();

}
