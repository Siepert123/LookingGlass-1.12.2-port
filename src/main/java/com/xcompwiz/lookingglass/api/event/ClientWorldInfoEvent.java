package com.xcompwiz.lookingglass.api.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientWorldInfoEvent extends Event {
    public final int dimension;
    public final EntityPlayerMP player;

    public ClientWorldInfoEvent(int dimension, EntityPlayerMP player) {
        this.dimension = dimension;
        this.player = player;
    }
}
