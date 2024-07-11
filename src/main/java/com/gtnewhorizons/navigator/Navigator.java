package com.gtnewhorizons.navigator;

import net.minecraft.client.settings.KeyBinding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = Navigator.MODID,
    version = Tags.VERSION,
    name = Navigator.MODNAME,
    acceptableRemoteVersions = "*",
    dependencies = "required-after:gtnhlib;")
public class Navigator {

    public static final String MODID = "navigator";
    public static final String MODNAME = "Navigator";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static KeyBinding actionKey;

    @SidedProxy(
        clientSide = "com.gtnewhorizons.navigator.ClientProxy",
        serverSide = "com.gtnewhorizons.navigator.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
