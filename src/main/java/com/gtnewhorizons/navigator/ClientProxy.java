package com.gtnewhorizons.navigator;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.journeymap.DirtyChunkButton;
import com.gtnewhorizons.navigator.impl.journeymap.DirtyChunkRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    public static final KeyBinding ACTION_KEY = new KeyBinding(
        "key.navigator.action",
        Keyboard.KEY_DELETE,
        "key.categories.navigator");

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Config.init(event);
        ClientRegistry.registerKeyBinding(ACTION_KEY);
        if (Config.enableDebugLayers) {
            Navigator.LOG.info("Debug layers enabled");
            NavigatorApi.registerSharedButtonManager(DirtyChunkButtonManager.instance);
            NavigatorApi.registerJourneyMapButton(DirtyChunkButton.instance);
            NavigatorApi.registerSharedLayerManager(DirtyChunkLayerManager.instance);
            NavigatorApi.registerJourneyMapRenderer(DirtyChunkRenderer.instance);
            NavigatorApi.registerXaeroMapRenderer(com.gtnewhorizons.navigator.impl.xaero.DirtyChunkRenderer.instance);
            NavigatorApi.registerXaeroMapButton(com.gtnewhorizons.navigator.impl.xaero.DirtyChunkButton.instance);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {}

    @Override
    public void postInit(FMLPostInitializationEvent event) {}

}
