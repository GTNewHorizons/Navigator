package com.gtnewhorizons.navigator.impl;

import com.gtnewhorizons.navigator.api.model.buttons.ButtonManager;

public class DirtyChunkButtonManager extends ButtonManager {

    public static final DirtyChunkButtonManager instance = new DirtyChunkButtonManager();

    public DirtyChunkButtonManager() {
        super("visualprospecting.button.dirtychunk", "nodes");
    }
}
