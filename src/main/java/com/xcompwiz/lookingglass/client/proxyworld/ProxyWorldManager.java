package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.render.FrameBufferContainer;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.packet.PacketCreateView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

@SideOnly(Side.CLIENT)
public class ProxyWorldManager {
    private static Map<Integer, WorldClient> proxyWorlds = new HashMap<>();
    private static Collection<WorldClient> proxyWorldSet = Collections.unmodifiableCollection(proxyWorlds.values());
    private static Map<Integer, Collection<WorldView>> worldViewSets = new HashMap<>();

    public static void handleWorldChange(WorldClient world) {
        if (ModConfigs.disabled) return;
        if (world == null) return;
        int dimID = world.provider.getDimension();
        if (!proxyWorlds.containsKey(dimID)) return;
        proxyWorlds.put(dimID, world);
        Collection<WorldView> worldViews = worldViewSets.get(dimID);
        for (WorldView view : worldViews) {
            view.replaceWorld(world);
        }
    }

    public static synchronized void detectFreedWorldViews() {
        FrameBufferContainer.detectFreedWorldViews();
        HashSet<Integer> emptyLists = new HashSet<>();
        for (Map.Entry<Integer, Collection<WorldView>> entry : worldViewSets.entrySet()) {
            if (entry.getValue().isEmpty()) emptyLists.add(entry.getKey());
        }
        for (Integer dimID : emptyLists) {
            unloadProxyWorld(dimID);
        }
    }

    public static synchronized WorldClient getProxyWorld(int dimID) {
        if (ModConfigs.disabled) return null;
        WorldClient proxyWorld = proxyWorlds.get(dimID);
        if (proxyWorld == null) {
            if (!DimensionManager.isDimensionRegistered(dimID)) return null;
            if (Minecraft.getMinecraft().player instanceof EntityCamera) return null;
            WorldClient world = Minecraft.getMinecraft().world;
            if (world != null && world.provider.getDimension() == dimID) proxyWorld = world;
            if (proxyWorld == null) proxyWorld = new ProxyWorld(dimID);
            proxyWorlds.put(dimID, proxyWorld);
            worldViewSets.put(dimID, Collections.newSetFromMap(new WeakHashMap<>()));
        }
        return proxyWorld;
    }

    private static synchronized void unloadProxyWorld(int dimID) {
        Collection<WorldView> set = worldViewSets.remove(dimID);
        if (set != null && !set.isEmpty()) LookingGlass.logger().warn("Unloading ProxyWorld with live views");
        WorldClient proxyWorld = proxyWorlds.remove(dimID);
        WorldClient world = Minecraft.getMinecraft().world;
        if (world != null && world == proxyWorld) return;
        if (proxyWorld != null) MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(proxyWorld));
    }

    public static void clearProxyWorlds() {
        while (!proxyWorlds.isEmpty()) {
            unloadProxyWorld(proxyWorlds.keySet().iterator().next());
        }
    }

    public static Collection<WorldClient> getProxyWorlds() {
        return proxyWorldSet;
    }

    public static Collection<WorldView> getWorldViews(int dimID) {
        Collection<WorldView> set = worldViewSets.get(dimID);
        if (set == null) return Collections.emptySet();
        return set;
    }

    public static synchronized WorldView createWorldView(int dimID, BlockPos spawn, int width, int height) {
        if (ModConfigs.disabled) return null;
        if (!DimensionManager.isDimensionRegistered(dimID)) return null;

        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dimID);
        if (proxyWorld == null) return null;

        Collection<WorldView> worldViews = worldViewSets.get(dimID);
        if (worldViews == null) return null;

        WorldView view = new WorldView(proxyWorld, spawn, width, height);

        Minecraft mc = Minecraft.getMinecraft();
        Entity backup = mc.getRenderViewEntity();
        mc.setRenderViewEntity(view.camera);
        view.getRenderGlobal().setWorldAndLoadRenderers(proxyWorld);
        mc.setRenderViewEntity(backup);

        LookingGlassPacketManager.bus.sendToServer(PacketCreateView.createPacket(view));
        worldViews.add(view);
        return view;
    }

    public static synchronized void destroyWorldView(WorldView view) {
        Collection<WorldView> set = worldViewSets.get(view.getWorld().provider.getDimension());
        if (set != null) set.remove(view);
        view.cleanup();
    }
}
