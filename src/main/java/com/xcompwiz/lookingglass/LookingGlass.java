package com.xcompwiz.lookingglass;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.apiimpl.APIProviderImpl;
import com.xcompwiz.lookingglass.command.CommandCreateView;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.core.LookingGlassForgeEventHandler;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import com.xcompwiz.lookingglass.imc.IMCHandler;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.network.packet.*;
import com.xcompwiz.lookingglass.proxyworld.LookingGlassEventHandler;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = LookingGlass.MODID, name = "LookingGlass", version = LookingGlass.VERSION)
public class LookingGlass {
    public static final String MODID = "lookingglass";
    public static final String VERSION = "@VERSION@";

    @Mod.Instance(LookingGlass.MODID)
    public static LookingGlass instance;

    @SidedProxy(
            clientSide = "com.xcompwiz.lookingglass.client.ClientProxy",
            serverSide = "com.xcompwiz.lookingglass.core.CommonProxy"
    )
    public static CommonProxy sidedProxy;

    private static Logger logger;
    public static Logger logger() {
        return logger;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        LookingGlassPacketManager.registerPacketHandler(new PacketCreateView(), (byte) 10);
        LookingGlassPacketManager.registerPacketHandler(new PacketCloseView(), (byte) 11);
        LookingGlassPacketManager.registerPacketHandler(new PacketWorldInfo(), (byte) 100);
        LookingGlassPacketManager.registerPacketHandler(new PacketChunkInfo(), (byte) 101);
        LookingGlassPacketManager.registerPacketHandler(new PacketTileEntityNBT(), (byte) 102);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestWorldInfo(), (byte) 200);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestChunk(), (byte) 201);
        LookingGlassPacketManager.registerPacketHandler(new PacketRequestTE(), (byte) 202);

        LookingGlassPacketManager.bus = NetworkRegistry.INSTANCE.newEventDrivenChannel(LookingGlassPacketManager.CHANNEL);
        LookingGlassPacketManager.bus.register(new LookingGlassPacketManager());

        ModConfigs.loadConfig(new Configuration(event.getSuggestedConfigurationFile()));

        File configRoot = event.getSuggestedConfigurationFile().getParentFile();
        MinecraftForge.EVENT_BUS.register(new LookingGlassEventHandler(new File(configRoot.getParentFile(), "logs/proxyworlds.log")));
        MinecraftForge.EVENT_BUS.register(new LookingGlassForgeEventHandler());

        APIProviderImpl.init();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        sidedProxy.init();
    }

    @EventHandler
    public void handleIMC(FMLInterModComms.IMCEvent event) {
        ImmutableList<FMLInterModComms.IMCMessage> messages = event.getMessages();
        IMCHandler.process(messages);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ((ServerCommandManager)server.getCommandManager()).registerCommand(new CommandCreateView());
        ServerPacketDispatcher.getInstance().start();
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        if (ModConfigs.forceLoadAllWorlds) {
            for (int dim : DimensionManager.getIDs()) {
                DimensionManager.keepDimensionLoaded(dim, true);
            }
        }
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent event) {
        ServerPacketDispatcher.getInstance().halt();
        ServerPacketDispatcher.shutdown();
    }
}
