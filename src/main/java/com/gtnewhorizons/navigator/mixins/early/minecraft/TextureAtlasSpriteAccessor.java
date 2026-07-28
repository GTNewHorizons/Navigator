package com.gtnewhorizons.navigator.mixins.early.minecraft;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlasSprite.class)
public interface TextureAtlasSpriteAccessor {

    @Accessor("useAnisotropicFiltering")
    boolean navigator$usesAnisotropicFiltering();
}
