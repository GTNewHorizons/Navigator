package com.gtnewhorizons.navigator.mixins.early.minecraft;

import net.minecraftforge.client.ForgeHooksClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// used to enable the stencil buffer for on-minimap rendering
@Mixin(value = ForgeHooksClient.class, remap = false)
public abstract class ForgeHooksClientMixin {

    // this is only a mixin because it needs to run before the minecraft window is created
    @Inject(method = "createDisplay", at = @At("HEAD"))
    private static void visualprospecting$enableStencilBuffer(CallbackInfo ci) {
        // give me my stencil buffer, forge.
        System.setProperty("forge.forceDisplayStencil", "true");
    }
}
