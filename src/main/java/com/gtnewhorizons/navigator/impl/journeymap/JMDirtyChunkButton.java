package com.gtnewhorizons.navigator.impl.journeymap;

import com.gtnewhorizons.navigator.api.journeymap.buttons.JMLayerButton;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;

public class JMDirtyChunkButton extends JMLayerButton {

    public static final JMDirtyChunkButton instance = new JMDirtyChunkButton();

    public JMDirtyChunkButton() {
        super(DirtyChunkButtonManager.instance);
    }
}
