package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorld;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.Collection;

public class PacketWorldInfo extends PacketHandlerBase {
    public static FMLProxyPacket createPacket(int dimension) {
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        if (world == null) {
            LookingGlass.logger().warn("Server-side world for dimension {} is null", dimension);
            return null;
        }
        BlockPos pos = world.getSpawnPoint();
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int skyLightSubtracted = world.getSkylightSubtracted();
        float thunderingStrength = world.thunderingStrength;
        float rainingStrength = world.rainingStrength;
        long worldTime = world.provider.getWorldTime();

        ByteBuf data = createDataBuffer(PacketWorldInfo.class);

        data.writeInt(dimension);
        data.writeInt(posX).writeInt(posY).writeInt(posZ);
        data.writeInt(skyLightSubtracted);
        data.writeFloat(thunderingStrength);
        data.writeFloat(rainingStrength);
        data.writeLong(worldTime);

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        int dimension = data.readInt();
        BlockPos spawn = new BlockPos(data.readInt(), data.readInt(), data.readInt());
        int skyLightSubtracted = data.readInt();
        float thunderingStrength = data.readFloat();
        float rainingStrength = data.readFloat();
        long worldTime = data.readLong();

        WorldClient proxyWorld = ProxyWorldManager.getProxyWorld(dimension);

        if (proxyWorld == null) return;
        if (proxyWorld.provider.getDimension() != dimension) return;

        Collection<WorldView> views = ProxyWorldManager.getWorldViews(dimension);
        for (WorldView view : views) {
            view.updateWorldSpawn(spawn);
        }
        proxyWorld.setSpawnPoint(spawn);
        proxyWorld.setSkylightSubtracted(skyLightSubtracted);
        proxyWorld.rainingStrength = rainingStrength;
        proxyWorld.thunderingStrength = thunderingStrength;
        proxyWorld.setWorldTime(worldTime);
    }
}
