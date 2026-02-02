package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.concurrent.Semaphore;
import java.util.zip.Deflater;

/**
 * Chunk serialization changed between 1.7.10 and 1.12.2, so I had to freestyle here. I hope it doesn't break...
 */
public class PacketChunkInfo extends PacketHandlerBase {
    private static byte[] inflateArray;
    private static byte[] dataArray;
    private static final Semaphore deflateGate = new Semaphore(1);

    private static int deflate(byte[] chunkData, byte[] compressedChunkData) {
        if (compressedChunkData == null) return 0;
        Deflater deflater = new Deflater(-1);
        int byteSize = 0;
        try {
            deflater.setInput(chunkData, 0, chunkData.length);
            deflater.finish();
            byteSize = deflater.deflate(compressedChunkData);
        } finally {
            deflater.end();
        }
        return byteSize;
    }

    public static FMLProxyPacket createPacket(Chunk chunk, boolean includeInit, int subID, int dimension) {
        LookingGlass.logger().debug("Sending chunk info");
        int x = chunk.x;
        int z = chunk.z;

        PacketBuffer buffer = new PacketBuffer(PacketHandlerBase.createDataBuffer(PacketChunkInfo.class));
        int blockMask = 0;
        int blockLightMask = 0;
        int skyLightMask = 0;
        ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
        for (int l = 0; l < storages.length; l++) {
            ExtendedBlockStorage storage = storages[l];
            int m = (1 << l);
            if (storage != null && (!includeInit || !storage.isEmpty()) && (subID & m) != 0) {
                blockMask |= m;
                blockLightMask |= m;
            }
        }
        if (chunk.getWorld().provider.hasSkyLight()) {
            for (int l = 0; l < storages.length; l++) {
                ExtendedBlockStorage storage = storages[l];
                int m = (1 << l);
                if (storage != null && (!includeInit || !storage.isEmpty()) && (subID & m) != 0) {
                    skyLightMask |= m;
                }
            }
        }

        buffer.writeInt(dimension);
        buffer.writeInt(x);
        buffer.writeInt(z);
        buffer.writeBoolean(includeInit); //basically "include biomes" here
        buffer.writeInt(blockMask);
        buffer.writeInt(blockLightMask);
        buffer.writeInt(skyLightMask);
        if (includeInit) {
            buffer.writeBytes(chunk.getBiomeArray());
        }
        for (int l = 0; l < storages.length; l++) {
            int m = 1 << l;
            if ((blockMask & m) != 0) {
                storages[l].getData().write(buffer);
            }
        }
        for (int l = 0; l < storages.length; l++) {
            int m = 1 << l;
            if ((blockLightMask & m) != 0) {
                buffer.writeBytes(storages[l].getBlockLight().getData());
            }
        }
        for (int l = 0; l < storages.length; l++) {
            int m = 1 << l;
            if ((skyLightMask & m) != 0) {
                buffer.writeBytes(storages[l].getSkyLight().getData());
            }
        }

        return buildPacket(buffer);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        LookingGlass.logger().debug("Received chunk info");
        int dimension = data.readInt();
        int x = data.readInt();
        int z = data.readInt();
        boolean includeInit = data.readBoolean();
        int blockMask = data.readInt();
        int blockLightMask = data.readInt();
        int skyLightMask = data.readInt();

        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dimension);
        if (proxyWorld == null) return;
        if (proxyWorld.provider.getDimension() != dimension) return;

        Chunk chunk = proxyWorld.getChunkProvider().getLoadedChunk(x, z);
        if (includeInit && (chunk == null || chunk.isEmpty())) {
            proxyWorld.doPreChunk(x, z, true);
        }
        proxyWorld.invalidateBlockReceiveRegion(x << 4, 0, z << 4, (x << 4) + 15, 256, (z << 4) + 15);
        chunk = proxyWorld.getChunkFromChunkCoords(x, z);
        if (chunk != null) {
            if (includeInit) {
                data.readBytes(chunk.getBiomeArray());
            }
            boolean flag = chunk.getWorld().provider.hasSkyLight();
            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
            PacketBuffer buffer = new PacketBuffer(data); //wrap
            for (int l = 0; l < storages.length; l++) {
                int m = 1 << l;
                if ((blockMask & m) != 0) {
                    if (storages[l] == null) storages[l] = new ExtendedBlockStorage(l, flag);
                    ExtendedBlockStorage storage = storages[l];
                    storage.getData().read(buffer);
                }
            }
            for (int l = 0; l < storages.length; l++) {
                int m = 1 << l;
                if ((blockLightMask & m) != 0) {
                    if (storages[l] == null) storages[l] = new ExtendedBlockStorage(l, flag);
                    ExtendedBlockStorage storage = storages[l];
                    buffer.readBytes(storage.getBlockLight().getData());
                }
            }
            for (int l = 0; l < storages.length; l++) {
                int m = 1 << l;
                if ((skyLightMask & m) != 0) {
                    if (storages[l] == null) storages[l] = new ExtendedBlockStorage(l, flag);
                    ExtendedBlockStorage storage = storages[l];
                    buffer.readBytes(storage.getSkyLight().getData());
                }
            }

            this.receivedChunk(proxyWorld, x, z);
            if (!includeInit || !(proxyWorld.provider instanceof WorldProviderSurface)) {
                chunk.resetRelightChecks();
            }
        }
    }

    public void receivedChunk(WorldClient world, int cx, int cz) {
        world.markBlockRangeForRenderUpdate(cx << 4, 0, cz << 4, (cx << 4) + 15, 256, (cz << 4) + 15);
        Chunk c = world.getChunkFromChunkCoords(cx, cz);
        if (c == null || c.isEmpty()) {
            LookingGlass.logger().debug("Ignoring chunk at {} {}", cx, cz);
            return;
        }

        for (WorldView activeView : ProxyWorldManager.getWorldViews(world.provider.getDimension())) {
            activeView.onChunkReceived(cx, cz);
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int x = cx << 4;
        int z = cz << 4;
        for (int y = 0; y < world.getActualHeight(); y += 16) {
            if (c.isEmptyBetween(y, y)) continue;
            for (int x2 = 0; x2 < 16; x2++) {
                for (int z2 = 0; z2 < 16; z2++) {
                    for (int y2 = 0; y2 < 16; y2++) {
                        int lx = x + x2;
                        int ly = y + y2;
                        int lz = z + z2;
                        IBlockState state = world.getBlockState(mutable.setPos(lx, ly, lz));
                        if (state.getBlock().hasTileEntity(state)) {
                            LookingGlassPacketManager.bus.sendToServer(PacketRequestTE.createPacket(lx, ly, lz, world.provider.getDimension()));
                        }
                    }
                }
            }
        }
    }
}
