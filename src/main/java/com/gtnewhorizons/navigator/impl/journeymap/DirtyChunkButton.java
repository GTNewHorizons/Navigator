package com.gtnewhorizons.navigator.impl.journeymap;

import com.gtnewhorizons.navigator.api.journeymap.buttons.JMLayerButton;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;

public class DirtyChunkButton extends JMLayerButton {

    public static final DirtyChunkButton instance = new DirtyChunkButton();

    public DirtyChunkButton() {
        super(DirtyChunkButtonManager.instance);
    }
}
