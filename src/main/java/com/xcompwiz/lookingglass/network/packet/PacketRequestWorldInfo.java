package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketRequestWorldInfo extends PacketHandlerBase {
    @SideOnly(Side.CLIENT)
    public static FMLProxyPacket createPacket(int x, int y, int z, int dimension) {
        ByteBuf data = createDataBuffer(PacketRequestWorldInfo.class);

        data.writeInt(dimension);

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
        int dimension = data.readInt();

        if (!DimensionManager.isDimensionRegistered(dimension)) return;
        MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dimension, (EntityPlayerMP) player));
        LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(dimension), (EntityPlayerMP) player);
    }
}
