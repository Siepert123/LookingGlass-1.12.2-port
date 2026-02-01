package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketTileEntityNBT extends PacketHandlerBase {
    public static FMLProxyPacket createPacket(int x, int y, int z, NBTTagCompound nbt, int dimension) {
        ByteBuf data =createDataBuffer(PacketTileEntityNBT.class);

        data.writeInt(dimension);
        data.writeInt(x).writeInt(y).writeInt(z);
        ByteBufUtils.writeTag(data, nbt);

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        int dimension = data.readInt();
        BlockPos pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
        NBTTagCompound nbt = ByteBufUtils.readTag(data);

        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dimension);
        if (proxyWorld == null) return;
        if (proxyWorld.provider.getDimension() != dimension) return;
        if (proxyWorld.isBlockLoaded(pos)) {
            TileEntity te = proxyWorld.getTileEntity(pos);

            if (te != null) {
                te.validate();
                te.readFromNBT(nbt);
            } else {
                te = TileEntity.create(proxyWorld, nbt);
                if (te != null) {
                    te.validate();
                    proxyWorld.addTileEntity(te);
                }
            }

            proxyWorld.setTileEntity(pos, te);
        }
    }
}
