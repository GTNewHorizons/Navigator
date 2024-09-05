package com.gtnewhorizons.navigator;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.config.GeneralConfig;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.registerKeyBinding(NavigatorApi.ACTION_KEY);
        if (GeneralConfig.enableDebugLayers) {
            NavigatorApi.registerLayerManager(DirtyChunkLayerManager.INSTANCE);
        }
        FMLCommonHandler.instance()
            .bus()
            .register(new EventHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {}

    @Override
    public void postInit(FMLPostInitializationEvent event) {}

}
