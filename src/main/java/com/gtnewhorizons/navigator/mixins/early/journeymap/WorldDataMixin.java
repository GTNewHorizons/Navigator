package com.gtnewhorizons.navigator.mixins.early.journeymap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gnu.trove.map.TIntObjectMap;
import journeymap.client.data.WorldData;

@Mixin(value = WorldData.class, remap = false)
public abstract class WorldDataMixin {
    // TODO: remove this once the issue is fixed in upstream JM

    @Shadow(remap = false)
    public static TIntObjectMap<String> dimNames;

    @Inject(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getResourceManager()Lnet/minecraft/client/resources/IResourceManager;"),
        cancellable = true)
    private static void navigator$FixWorldData(CallbackInfo ci) {

        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener(new IResourceManagerReloadListener() {

                @Override
                public void onResourceManagerReload(IResourceManager p_110549_1_) {
                    dimNames.clear();
                }
            });
        ci.cancel();
    }

}
