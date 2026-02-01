package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.network.ServerPacketDispatcher;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinder;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketRequestChunk extends PacketHandlerBase {
    public static FMLProxyPacket createPacket(int x, int y, int z, int dimension) {
        ByteBuf data = createDataBuffer(PacketRequestChunk.class);

        data.writeInt(dimension);
        data.writeInt(x).writeInt(y).writeInt(z);

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
        int dimension = data.readInt();
        int x = data.readInt();
        int y = data.readInt();
        int z = data.readInt();

        if (!DimensionManager.isDimensionRegistered(dimension)) return;
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        if (world == null) return;
        Chunk chunk = world.getChunkFromChunkCoords(x, z);
        if (!chunk.isLoaded()) chunk = ChunkFinder.loadChunk(world.getChunkProvider(), x, z);
        ServerPacketDispatcher.getInstance().addPacket(player, PacketChunkInfo.createPacket(chunk, true, y, dimension));
    }
}
