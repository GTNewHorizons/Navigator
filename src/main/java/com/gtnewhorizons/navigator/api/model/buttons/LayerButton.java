package com.gtnewhorizons.navigator.api.model.buttons;

import com.gtnewhorizons.navigator.api.model.SupportedMods;

public abstract class LayerButton {

    protected final ButtonManager manager;

    public LayerButton(ButtonManager manager, SupportedMods map) {
        manager.registerButton(map, this);
        this.manager = manager;
        // Grab lang key and texture information from manager in extended constructor
    }

    public abstract void updateState(boolean active);

    public String getButtonTextKey() {
        return manager.getButtonTextKey();
    }

    public String getIconName() {
        return manager.getIconName();
    }

    public void toggle() {
        manager.toggle();
    }

    /**
     * Whether the button should be added to the GUI.
     * 
     * @return true if the button should be added, false otherwise
     */
    public boolean isEnabled() {
        return true;
    }
}
