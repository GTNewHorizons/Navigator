package com.gtnewhorizons.navigator;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.config.GeneralConfig;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;

@EventBusSubscriber(side = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.registerKeyBinding(NavigatorApi.ACTION_KEY);
        if (GeneralConfig.enableDebugLayers) {
            NavigatorApi.registerLayerManager(DirtyChunkLayerManager.INSTANCE);
        }
    }

    @SubscribeEvent
    public static void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NavigatorApi.layerManagers.forEach(LayerManager::clearFullCache);
    }
}
