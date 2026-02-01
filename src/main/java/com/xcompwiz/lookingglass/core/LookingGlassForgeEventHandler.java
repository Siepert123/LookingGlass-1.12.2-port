package com.xcompwiz.lookingglass.core;

import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LookingGlassForgeEventHandler {
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) return;
        Chunk chunk = event.getChunk();
        synchronized (chunk.getEntityLists()) {
            for (int i = 0; i < chunk.getEntityLists().length; i++) {
                ClassInheritanceMultiMap<Entity> list = chunk.getEntityLists()[i];
                for (Entity entity : list) {
                    if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (!event.getWorld().isRemote) return;
        synchronized (event.getWorld().getLoadedEntityList()) {
            for (Entity entity : event.getWorld().getLoadedEntityList()) {
                if (entity instanceof EntityPortal) ((EntityPortal) entity).releaseActiveView();
            }
        }
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().register(EntityEntryBuilder.create().entity(EntityPortal.class)
                .factory(EntityPortal::new).id("lookingglass:portal", 216).name("lookingglass.portal")
                .tracker(64, 10, false).build());
    }
}
