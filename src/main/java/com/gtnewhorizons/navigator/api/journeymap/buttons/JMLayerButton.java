package com.gtnewhorizons.navigator.api.journeymap.buttons;

import com.gtnewhorizons.navigator.api.model.SupportedMods;
import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;
import com.gtnewhorizons.navigator.api.model.buttons.LayerButton;

import journeymap.client.ui.theme.ThemeToggle;

public class JMLayerButton extends LayerButton {

    private ThemeToggle button;
    private boolean isActive = false;

    public JMLayerButton(ButtonManager manager) {
        super(manager, SupportedMods.JourneyMap);
    }

    @Override
    public void updateState(boolean active) {
        isActive = active;
        if (button != null) {
            button.setToggled(active, false);
        }
    }

    public void setButton(ThemeToggle button) {
        this.button = button;
    }

    public boolean isActive() {
        return isActive;
    }
}
