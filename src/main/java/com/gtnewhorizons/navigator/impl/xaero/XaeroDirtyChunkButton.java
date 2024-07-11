package com.gtnewhorizons.navigator.impl.xaero;

import com.gtnewhorizons.navigator.api.xaero.buttons.XaeroLayerButton;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;

public class XaeroDirtyChunkButton extends XaeroLayerButton {

    public static final XaeroDirtyChunkButton instance = new XaeroDirtyChunkButton();

    public XaeroDirtyChunkButton() {
        super(DirtyChunkButtonManager.instance);
    }
}
