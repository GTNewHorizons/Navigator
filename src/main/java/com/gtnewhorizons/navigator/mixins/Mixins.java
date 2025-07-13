package com.gtnewhorizons.navigator.mixins;

import org.jetbrains.annotations.NotNull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;
import com.gtnewhorizons.navigator.config.ModuleConfig;

public enum Mixins implements IMixins {

    // spotless:off
    ENABLE_STENCIL(new MixinBuilder("Force enables stencil buffer")
        .addRequiredMod(TargetedMod.XAEROMINIMAP)
        .addRequiredMod(TargetedMod.XAEROWORLDMAP)
        .setPhase(Phase.EARLY)
        .setApplyIf(() -> ModuleConfig.enableXaeroMinimapModule)
        .addClientMixins("minecraft.ForgeHooksClientMixin")),
    JOURNEYMAP_API(new MixinBuilder()
        .addRequiredMod(TargetedMod.JOURNEYMAP)
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModuleConfig.enableJourneyMapModule)
        .addClientMixins(
            "journeymap.DisplayVarsAccessor",
            "journeymap.FullscreenAccessor",
            "journeymap.FullscreenMixin",
            "journeymap.MiniMapMixin",
            "journeymap.RenderWaypointBeaconMixin",
            "journeymap.WaypointManagerMixin",
            "journeymap.TextureCacheMixin")),
    XAEROS_GUI(new MixinBuilder()
        .addRequiredMod(TargetedMod.XAEROWORLDMAP)
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModuleConfig.enableXaeroWorldMapModule)
        .addClientMixins("xaerosworldmap.GuiMapMixin")),
    XAEROS_MINIMAP_WAYPOINT(new MixinBuilder()
        .addRequiredMod(TargetedMod.XAEROMINIMAP)
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModuleConfig.enableXaeroMinimapModule)
        .addClientMixins("xaerosminimap.WaypointsIngameRendererMixin")),
    XAEROS_MINIMAP_RENDERER(new MixinBuilder()
        .addRequiredMod(TargetedMod.XAEROMINIMAP)
        .addRequiredMod(TargetedMod.XAEROWORLDMAP)
        .setPhase(Phase.LATE)
        .setApplyIf(() -> ModuleConfig.enableXaeroMinimapModule)
        .addClientMixins("xaerosminimap.MinimapRendererMixin"));
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @NotNull
    @Override
    public MixinBuilder getBuilder() {
        return this.builder;
    }
}
