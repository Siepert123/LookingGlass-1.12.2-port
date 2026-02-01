package com.xcompwiz.lookingglass.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketHolder {
    final EntityPlayer player;
    final FMLProxyPacket packet;

    public PacketHolder(EntityPlayer player, FMLProxyPacket packet) {
        this.player = player;
        this.packet = packet;
    }

    public boolean belongsToPlayer(EntityPlayer player) {
        return this.player.equals(player);
    }

    public int sendPacket() {
        if (this.packet != null) {
            LookingGlassPacketManager.bus.sendTo(this.packet, (EntityPlayerMP) this.player);
            return this.packet.payload().writerIndex();
        }
        return 0;
    }
}
