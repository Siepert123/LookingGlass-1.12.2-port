package com.xcompwiz.lookingglass.network;

import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.util.LinkedList;
import java.util.List;

public class ServerPacketDispatcher extends Thread {
    private static ServerPacketDispatcher instance;

    private final List<PacketHolder> packets;
    private boolean isRunning = true;

    private ServerPacketDispatcher() {
        this.packets = new LinkedList<>();
    }

    public static ServerPacketDispatcher getInstance() {
        if (instance == null) instance = new ServerPacketDispatcher();
        return instance;
    }

    public static void shutdown() {
        if (instance != null) instance.halt();
        instance = null;
    }

    public void addPacket(EntityPlayer player, FMLProxyPacket packet) {
        synchronized (this) {
            this.packets.add(new PacketHolder(player, packet));
            this.notify();
        }
    }

    public void removeAllPacketsOf(EntityPlayer player) {
        synchronized (this) {
            for (int i = 0; i < this.packets.size(); i++) {
                if (this.packets.get(i).belongsToPlayer(player)) {
                    this.packets.remove(i);
                    --i;
                }
            }
        }
    }

    public void tick() {
        int byteLimit = ModConfigs.dataRate;
        for (int bytes = 0; bytes < byteLimit && !this.packets.isEmpty();) {
            PacketHolder holder = this.packets.get(0);
            bytes += holder.sendPacket();
            this.packets.remove(0);
        }
    }

    public void halt() {
        synchronized (this) {
            this.isRunning = false;
            this.packets.clear();
        }
    }

    @Override
    public void run() {
        while (this.isRunning) {
            if (!this.packets.isEmpty()) {
                try {
                    synchronized (this) {
                        this.tick();
                        this.wait(20);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            } else {
                try {
                    synchronized (this) {
                        this.wait(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}
