package com.gtnewhorizons.navigator;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizons.navigator.mixins.Mixins;

@LateMixin
public class NavigatorLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.navigator.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return IMixins.getLateMixins(Mixins.class, loadedMods);
    }
}
