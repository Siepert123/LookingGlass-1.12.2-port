package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketRequestTE extends PacketHandlerBase {
    public static FMLProxyPacket createPacket(int x, int y, int z, int dimension) {
        ByteBuf data = createDataBuffer(PacketRequestTE.class);

        data.writeInt(dimension);
        data.writeInt(x).writeInt(y).writeInt(z);

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        if (ModConfigs.disabled) return;

        int dimension = data.readInt();
        BlockPos pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());

        if (!DimensionManager.isDimensionRegistered(dimension)) return;
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        if (world == null) return;
        TileEntity te = world.getTileEntity(pos);
        if (te != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
            ServerPacketDispatcher.getInstance().addPacket(player, PacketTileEntityNBT.createPacket(pos.getX(), pos.getY(), pos.getZ(), nbt, dimension));
        }
    }
}
