package com.xcompwiz.lookingglass.network.packet;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.api.event.ClientWorldInfoEvent;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinder;
import com.xcompwiz.lookingglass.proxyworld.ChunkFinderManager;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketCreateView extends PacketHandlerBase {
    @SideOnly(Side.CLIENT)
    public static FMLProxyPacket createPacket(WorldView view) {
        ByteBuf data = createDataBuffer(PacketCreateView.class);

        int x = 0;
        int y = -1;
        int z = 0;
        if (view.pos != null) {
            x = view.pos.getX() >> 4;
            y = view.pos.getY() >> 4;
            z = view.pos.getZ() >> 4;
        }

        data.writeInt(view.getWorld().provider.getDimension());
        data.writeInt(x);
        data.writeInt(y);
        data.writeInt(z);
        data.writeByte(Math.min(ModConfigs.renderDistance, Minecraft.getMinecraft().gameSettings.renderDistanceChunks));

        return buildPacket(data);
    }

    @Override
    public void handle(ByteBuf data, EntityPlayer player) {
        if (ModConfigs.disabled) return;
        LookingGlass.logger().debug("Received view request");
        int dimension = data.readInt();
        int xPos = data.readInt();
        int yPos = data.readInt();
        int zPos = data.readInt();
        byte renderDistance = data.readByte();

        if (!DimensionManager.isDimensionRegistered(dimension)) return;
        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension);
        if (world == null) return;
        int x, y, z;
        if (yPos < 0) {
            BlockPos pos = world.getSpawnPoint();
            x = pos.getX() >> 4;
            y = pos.getY() >> 4;
            z = pos.getZ() >> 4;
        } else {
            x = xPos;
            y = yPos;
            z = zPos;
        }
        if (renderDistance > ModConfigs.renderDistance) renderDistance = ModConfigs.renderDistance;
        ChunkFinderManager.instance.addFinder(new ChunkFinder(new BlockPos(x, y, z), dimension, world.getChunkProvider(), player, renderDistance));
        MinecraftForge.EVENT_BUS.post(new ClientWorldInfoEvent(dimension, (EntityPlayerMP) player));
        LookingGlassPacketManager.bus.sendTo(PacketWorldInfo.createPacket(dimension), (EntityPlayerMP) player);
    }
}
