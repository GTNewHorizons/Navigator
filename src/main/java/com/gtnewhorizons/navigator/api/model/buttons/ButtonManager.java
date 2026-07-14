package com.gtnewhorizons.navigator.api.model.buttons;

import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.SupportedMods;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

/**
 * Logical toggle shared by a layer across map integrations.
 * <p>
 * Activating one button deactivates every other distinct registered Navigator button. A consumer callback may be
 * installed with {@link #setOnToggle(BooleanConsumer)}; the owning layer manager installs a separate internal
 * notification callback.
 */
public abstract class ButtonManager {

    protected boolean isActive = false;
    protected BooleanConsumer onToggle;
    private BooleanConsumer notifyLayerToggled;

    /**
     * @param mod   the mod requesting the icon
     * @param theme current map UI theme; JourneyMap supplies its theme name and Xaero normally supplies an empty string
     * @return the {@link ResourceLocation} of the icon to be displayed on the button
     */
    public abstract ResourceLocation getIcon(SupportedMods mod, String theme);

    /** @return localized or display-ready tooltip text for the button */
    public abstract String getButtonText();

    /**
     * @param onToggle consumer callback invoked after the active state changes
     */
    public final void setOnToggle(BooleanConsumer onToggle) {
        this.onToggle = onToggle;
    }

    /**
     * Installs the owning manager's state callback.
     * <p>
     * Consumers normally do not call this method; {@link com.gtnewhorizons.navigator.api.model.layers.LayerManager}
     * configures it in its constructor.
     */
    public final void setLayerNotify(BooleanConsumer layerToggled) {
        this.notifyLayerToggled = layerToggled;
    }

    /** @return current logical active state */
    public boolean isActive() {
        return isActive;
    }

    /** Activates this button after deactivating other distinct Navigator buttons. */
    public void activate() {
        NavigatorApi.getDistinctButtons(this)
            .forEach(ButtonManager::deactivate);
        isActive = true;
        if (onToggle != null) {
            onToggle.accept(true);
        }

        if (notifyLayerToggled != null) {
            notifyLayerToggled.accept(true);
        }
    }

    /** Deactivates this button and notifies callbacks. */
    public void deactivate() {
        isActive = false;
        if (onToggle != null) {
            onToggle.accept(false);
        }

        if (notifyLayerToggled != null) {
            notifyLayerToggled.accept(false);
        }
    }

    /** Toggles between {@link #activate()} and {@link #deactivate()}. */
    public void toggle() {
        if (isActive) {
            deactivate();
        } else {
            activate();
        }
    }
}
