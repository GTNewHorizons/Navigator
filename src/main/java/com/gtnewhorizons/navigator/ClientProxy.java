package com.gtnewhorizons.navigator;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.config.GeneralConfig;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkRenderer;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkWaypointManager;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkRenderer;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkWaypointManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.registerKeyBinding(NavigatorApi.ACTION_KEY);
        if (GeneralConfig.enableDebugLayers) {
            // Shared
            NavigatorApi.registerLayerManager(DirtyChunkLayerManager.instance);
            NavigatorApi.registerButtonManager(DirtyChunkButtonManager.instance);

            // Journeymap
            NavigatorApi.registerLayerRenderer(JMDirtyChunkRenderer.instance);
            NavigatorApi.registerWaypointManager(JMDirtyChunkWaypointManager.instance);

            // Xaero's maps
            NavigatorApi.registerLayerRenderer(XaeroDirtyChunkRenderer.instance);
            NavigatorApi.registerWaypointManager(XaeroDirtyChunkWaypointManager.instance);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {}

    @Override
    public void postInit(FMLPostInitializationEvent event) {}

}
