package com.gtnewhorizons.navigator.impl.xaero;

import com.gtnewhorizons.navigator.api.xaero.buttons.XaeroLayerButton;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;

public class DirtyChunkButton extends XaeroLayerButton {

    public static final DirtyChunkButton instance = new DirtyChunkButton();

    public DirtyChunkButton() {
        super(DirtyChunkButtonManager.instance);
    }
}
