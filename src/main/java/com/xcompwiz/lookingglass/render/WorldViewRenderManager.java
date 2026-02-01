package com.xcompwiz.lookingglass.render;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.client.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.PrintStream;
import java.util.Collection;

public class WorldViewRenderManager {
    public static void onRenderTick(PrintStream logger) {
        Minecraft mc = Minecraft.getMinecraft();
        Collection<WorldClient> worlds = ProxyWorldManager.getProxyWorlds();
        if (worlds == null || worlds.isEmpty()) return;

        long renderT = Minecraft.getSystemTime();
        WorldClient worldBackup = mc.world;
        RenderGlobal renderBackup = mc.renderGlobal;
        ParticleManager effectBackup = mc.effectRenderer;
        EntityPlayerSP playerBackup = mc.player;
        Entity renderViewBackup = mc.getRenderViewEntity();
        RenderManager renderManager = mc.getRenderManager();

        float fov = playerBackup.getFovModifier();
        ItemStack currentClientItem = playerBackup.inventory.getCurrentItem();

        for (WorldClient proxyWorld : worlds) {
            if (proxyWorld == null) continue;
            mc.world = proxyWorld;
            renderManager.setWorld(mc.world);
            for (WorldView activeView : ProxyWorldManager.getWorldViews(proxyWorld.provider.getDimension())) {
                if (activeView.hasChunks() && activeView.markClean()) {
                    activeView.startRender(renderT);

                    mc.renderGlobal = activeView.getRenderGlobal();
                    mc.effectRenderer = activeView.getEffectRenderer();
                    mc.setRenderViewEntity(activeView.camera);
                    mc.player = activeView.camera;
                    activeView.camera.setFOVMult(fov);
                    activeView.camera.inventory.currentItem = playerBackup.inventory.currentItem;
                    activeView.camera.inventory.mainInventory.set(playerBackup.inventory.currentItem, currentClientItem);

                    try {
                        mc.renderGlobal.updateClouds();
                        mc.world.doVoidFogParticles(
                                MathHelper.floor(activeView.camera.posX),
                                MathHelper.floor(activeView.camera.posY),
                                MathHelper.floor(activeView.camera.posZ)
                        );
                        mc.effectRenderer.updateEffects();
                    } catch (Exception e) {
                        LookingGlass.logger().error("Client Proxy Dimension had error while rendering: {}", e.getLocalizedMessage());
                        e.printStackTrace(logger);
                    }

                    try {
                        RenderUtils.renderWorldToTexture(0.1F, activeView.getFramebuffer(), activeView.width, activeView.height);
                    } catch (Exception e) {
                        LookingGlass.logger().error("Client Proxy Dimension had error while buffering: {}", e.getLocalizedMessage());
                        e.printStackTrace(logger);
                    }
                }
            }
        }

        Vec3d fog = worldBackup.getFogColor(1.0F);
        GlStateManager.clearColor((float) fog.x, (float) fog.y, (float) fog.z, 0.0F);

        mc.setRenderViewEntity(renderViewBackup);
        mc.player = playerBackup;
        mc.effectRenderer = effectBackup;
        mc.renderGlobal = renderBackup;
        mc.world = worldBackup;
        renderManager.setWorld(mc.world);
    }
}
