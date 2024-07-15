package com.gtnewhorizons.navigator.api.model.buttons;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.SupportedMods;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

public abstract class ButtonManager {

    protected boolean isActive = false;
    protected BooleanConsumer onToggle;

    /**
     * @param mod   the mod requesting the icon
     * @param theme the theme of the icon. "Victorian" or "Vault" for Journeymap, empty string for Xaeros.
     * @return the {@link ResourceLocation} of the icon to be displayed on the button
     */
    public abstract ResourceLocation getIcon(SupportedMods mod, String theme);

    public abstract String getButtonText();

    public final void setOnToggle(BooleanConsumer onToggle) {
        this.onToggle = onToggle;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        NavigatorApi.getDistinctButtons()
            .forEach(ButtonManager::deactivate);
        isActive = true;
        if (onToggle != null) {
            onToggle.accept(true);
        }
    }

    public void deactivate() {
        isActive = false;
        if (onToggle != null) {
            onToggle.accept(false);
        }
    }

    public void toggle() {
        if (isActive) {
            deactivate();
        } else {
            activate();
        }
    }
}
