package com.xcompwiz.lookingglass.proxyworld;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.render.PerspectiveRenderManager;
import com.xcompwiz.lookingglass.render.WorldViewRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class LookingGlassEventHandler {
    private final PrintStream logger;

    @SideOnly(Side.CLIENT)
    private WorldClient previousWorld;

    @SideOnly(Side.CLIENT)
    private int tickCounter;

    public LookingGlassEventHandler(File logFile) {
        PrintStream stream = null;
        try {
            stream = new PrintStream(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        } finally {
            this.logger = stream;
        }
        if (this.logger == null) throw new RuntimeException("Could not set up proxyworlds debug logger");
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        if (++this.tickCounter % 200 == 0) ProxyWorldManager.detectFreedWorldViews();

        if (mc.world != this.previousWorld) {
            if (this.previousWorld != null) this.previousWorld.removeAllEntities();
            this.previousWorld = mc.world;

            ProxyWorldManager.handleWorldChange(mc.world);
        }

        WorldClient worldBackup = mc.world;
        for (WorldClient proxyWorld : ProxyWorldManager.getProxyWorlds()) {
            if (proxyWorld.getLastLightningBolt() > 0) proxyWorld.setLastLightningBolt(proxyWorld.getLastLightningBolt() - 1);
            if (worldBackup == proxyWorld) continue;
            try {
                mc.world = proxyWorld;
                proxyWorld.tick();
            } catch (Exception e) {
                LookingGlass.logger().error("Client proxy dimension had error while ticking: {}", e.toString());
                e.printStackTrace(this.logger);
            }
        }
        mc.world = worldBackup;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (Minecraft.getMinecraft().world == null) return;
        if (event.phase == TickEvent.Phase.START) {
            PerspectiveRenderManager.onRenderTick(this.logger);
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            WorldViewRenderManager.onRenderTick(this.logger);
            return;
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ChunkFinderManager.instance.tick();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ProxyWorldManager.clearProxyWorlds();
    }
}
