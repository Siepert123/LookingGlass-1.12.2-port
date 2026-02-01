package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCloseView extends PacketHandlerBase {
    @SideOnly(Side.CLIENT)
    public static FMLProxyPacket createPacket(WorldView view) {
        return buildPacket(createDataBuffer(PacketCloseView.class));
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
    }
}
