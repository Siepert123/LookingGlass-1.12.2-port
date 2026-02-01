package com.xcompwiz.lookingglass.client;

import com.xcompwiz.lookingglass.client.render.RenderPortal;
import com.xcompwiz.lookingglass.core.CommonProxy;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void init() {
        Render<EntityPortal> render;
        render = new RenderPortal(Minecraft.getMinecraft().getRenderManager());
        RenderingRegistry.registerEntityRenderingHandler(EntityPortal.class, render);
    }
}
