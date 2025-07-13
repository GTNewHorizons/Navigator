package com.gtnewhorizons.navigator.mixins;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    JOURNEYMAP(null, "journeymap"),
    XAEROMINIMAP("xaero.common.core.XaeroMinimapPlugin", "XaeroMinimap"),
    XAEROWORLDMAP("xaero.map.core.XaeroWorldMapPlugin", "XaeroWorldMap");

    private final TargetModBuilder builder;

    TargetedMod(String coreModClass, String modId) {
        this.builder = new TargetModBuilder().setCoreModClass(coreModClass)
            .setModId(modId);
    }

    @NotNull
    @Override
    public TargetModBuilder getBuilder() {
        return builder;
    }
}
