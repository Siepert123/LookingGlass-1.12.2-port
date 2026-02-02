package com.xcompwiz.lookingglass.network;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.network.packet.PacketHandlerBase;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LookingGlassPacketManager {
    public static final String CHANNEL = "lookingglass";
    public static FMLEventChannel bus;

    private static final Byte2ObjectMap<PacketHandlerBase> handlers = new Byte2ObjectOpenHashMap<>();
    private static final Object2ByteMap<Class<? extends PacketHandlerBase>> idMap = new Object2ByteOpenHashMap<>();

    public static void registerPacketHandler(PacketHandlerBase handler, byte id) {
        if (handlers.get(id) != null) throw new RuntimeException("Multiple id registrations for packet type on " + CHANNEL + "channel");
        handlers.put(id, handler);
        idMap.put(handler.getClass(), id);
    }

    public static byte getId(PacketHandlerBase handler) {
        return getId(handler.getClass());
    }
    public static byte getId(Class<? extends PacketHandlerBase> clazz) {
        if (!idMap.containsKey(clazz)) throw new RuntimeException("Attempted to get id for unregistered network message handler");
        return idMap.get(clazz);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onPacketData(FMLNetworkEvent.ClientCustomPacketEvent event) {
        FMLProxyPacket packet = event.getPacket();
        this.onPacketData(event.getManager(), packet, Minecraft.getMinecraft().player);
    }

    @SubscribeEvent
    public void onPacketData(FMLNetworkEvent.ServerCustomPacketEvent event) {
        FMLProxyPacket packet = event.getPacket();
        this.onPacketData(event.getManager(), packet, ((NetHandlerPlayServer)event.getHandler()).player);
    }

    public void onPacketData(NetworkManager manager, FMLProxyPacket packet, EntityPlayer player) {
        try {
            if (packet == null || packet.payload() == null) throw new RuntimeException("Empty packet sent to " + CHANNEL + " channel");
            ByteBuf data = packet.payload();
            byte type = data.readByte();
            try {
                PacketHandlerBase handler = handlers.get(type);
                if (handler == null) throw new RuntimeException("Unrecognized packet sent to " + CHANNEL + " channel");
                handler.handle(data, player);
            } catch (Exception e) {
                LookingGlass.logger().warn("PacketHandler: Failed handle packet type {}", type);
                LookingGlass.logger().warn(e.toString());
                e.printStackTrace(System.err);
            }
        } catch (Exception e) {
            LookingGlass.logger().warn("PacketHandler: Failed to read packet");
            LookingGlass.logger().warn(e.toString());
            e.printStackTrace(System.err);
        }
    }
}
