package com.gtnewhorizons.navigator.mixins.late.journeymap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import journeymap.client.ui.component.Button;
import journeymap.client.ui.component.ButtonList;
import journeymap.client.ui.component.JmUI;
import journeymap.client.ui.dialog.FullscreenActions;

@Mixin(FullscreenActions.class)
public abstract class FullscreenActionsMixin extends JmUI {

    @Shadow(remap = false)
    Button buttonAbout;

    @Unique
    private Button navigator$resetCacheButton;

    public FullscreenActionsMixin() {
        super("");
    }

    @Inject(
        method = "initGui",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Ljourneymap/client/ui/dialog/FullscreenActions;buttonAbout:Ljourneymap/client/ui/component/Button;"),
        require = 1,
        remap = false)
    private void visualprospecting$onInitGui(CallbackInfo ci) {
        navigator$resetCacheButton = new Button(I18n.format("visualprospecting.button.resetprogress"));
        navigator$resetCacheButton.setTooltip(I18n.format("visualprospecting.button.resetprogress.tooltip"));
        buttonList.add(navigator$resetCacheButton);
    }

    @Inject(method = "layoutButtons", at = @At("RETURN"), remap = false, require = 1)
    private void visualprospecting$onLayoutButtons(CallbackInfo ci) {
        final ButtonList row = new ButtonList(buttonAbout, navigator$resetCacheButton);
        row.layoutCenteredHorizontal(width / 2, height / 4, true, 4);
    }

    @Inject(
        method = "actionPerformed",
        at = @At("HEAD"),
        remap = false,
        cancellable = true,
        require = 1,
        locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void visualprospecting$onButtonClicked(GuiButton guibutton, CallbackInfo ci) {
        // if (guibutton == resetVisualProspectingCacheButton) {
        // UIManager.getInstance().open(ResetClientCacheConfirmation.class);
        // ci.cancel();
        // }
    }
}
