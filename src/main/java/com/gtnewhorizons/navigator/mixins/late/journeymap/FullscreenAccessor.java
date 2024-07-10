package com.gtnewhorizons.navigator.mixins.late.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import journeymap.client.render.map.GridRenderer;
import journeymap.client.ui.fullscreen.Fullscreen;

@Mixin(Fullscreen.class)
public interface FullscreenAccessor {

    @Accessor(remap = false)
    static GridRenderer getGridRenderer() {
        throw new IllegalStateException("Mixin accessor failed to apply");
    }

}
