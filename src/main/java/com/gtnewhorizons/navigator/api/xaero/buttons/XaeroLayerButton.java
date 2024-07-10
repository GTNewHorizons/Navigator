package com.gtnewhorizons.navigator.api.xaero.buttons;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.buttons.LayerButton;

public class XaeroLayerButton extends LayerButton {

    private SizedGuiTexturedButton button;
    public final ResourceLocation textureLocation;

    public XaeroLayerButton(ButtonManager manager) {
        super(manager, SupportedMods.XaeroWorldMap);
        textureLocation = new ResourceLocation("xaeroworldmap", "textures/" + getIconName() + ".png");
    }

    @Override
    public void updateState(boolean active) {
        if (button != null) {
            button.setActive(active);
        }
    }

    public void setButton(SizedGuiTexturedButton button) {
        this.button = button;
        updateState(manager.isActive());
    }
}
