package com.gtnewhorizons.navigator.mixins.late.journeymap.v6;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.gtnewhorizons.navigator.internal.journeymap.v6.JourneyMapV6Plugin;

import journeymap.client.ui.fullscreen.Fullscreen;
import journeymap.client.ui.fullscreen.MapChat;

@Mixin(Fullscreen.class)
public abstract class FullscreenMixin {

    @Shadow(remap = false)
    private MapChat chat;

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    private void navigator$onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        if ((chat == null || chat.isHidden()) && JourneyMapV6Plugin.onSearchKeyTyped(typedChar, keyCode)) ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void navigator$onMouseClicked(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if ((chat == null || chat.isHidden()) && JourneyMapV6Plugin.onSearchMouseClicked(mouseX, mouseY, button)) {
            ci.cancel();
        }
    }

    @Inject(method = "isSearchFocused", at = @At("RETURN"), cancellable = true, remap = false)
    private void navigator$includeNavigatorSearch(CallbackInfoReturnable<Boolean> cir) {
        if (JourneyMapV6Plugin.isSearchFocused()) cir.setReturnValue(true);
    }
}
