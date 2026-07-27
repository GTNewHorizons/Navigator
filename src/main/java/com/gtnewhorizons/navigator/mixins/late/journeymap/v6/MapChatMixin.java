package com.gtnewhorizons.navigator.mixins.late.journeymap.v6;

import net.minecraft.client.gui.GuiChat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import journeymap.client.ui.fullscreen.MapChat;

@Mixin(MapChat.class)
public abstract class MapChatMixin extends GuiChat {

    @Shadow(remap = false)
    protected boolean hidden;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    private void navigator$unfocusHiddenInput(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (hidden && inputField != null && inputField.isFocused()) inputField.setFocused(false);
    }

    @Inject(method = "setHidden", at = @At("TAIL"), remap = false)
    private void navigator$restoreInputState(boolean hidden, CallbackInfo ci) {
        if (inputField != null) inputField.setFocused(!hidden);
    }
}
