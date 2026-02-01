package com.xcompwiz.lookingglass.client.render;

import com.xcompwiz.lookingglass.LookingGlass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    @SideOnly(Side.CLIENT)
    public static void renderWorldToTexture(float renderTime, int framebuffer, int width, int height) {
        if (framebuffer == 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.skipRenderWorld) return;
        EntityRenderer entityRenderer = mc.entityRenderer;


        int heightBackup = mc.displayHeight;
        int widthBackup = mc.displayWidth;

        int thirdPersonBackup = mc.gameSettings.thirdPersonView;
        boolean hideGuiBackup = mc.gameSettings.hideGUI;
        int particleBackup = mc.gameSettings.particleSetting;
        boolean anaglyphBackup = mc.gameSettings.anaglyph;
        int renderDistanceBackup = mc.gameSettings.renderDistanceChunks;
        float FOVBackup = mc.gameSettings.fovSetting;

        try {
            mc.displayWidth = width;
            mc.displayHeight = height;

            mc.gameSettings.thirdPersonView = 0;
            mc.gameSettings.hideGUI = true;
            //mc.gameSettings.particleSetting = ;
            mc.gameSettings.anaglyph = false;
            //mc.gameSettings.renderDistanceChunks = ;
            //mc.gameSettings.fovSetting = ;

            //Set gl options
            GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight);
            GlStateManager.bindTexture(0);
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebuffer);
            GlStateManager.clearColor(1.0F, 0.0F, 0.0F, 0.5F);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

            int i1 = mc.gameSettings.limitFramerate;
            if (mc.isFramerateLimitBelowMax()) {
                entityRenderer.renderWorld(renderTime, (1000000000 / i1));
            } else {
                entityRenderer.renderWorld(renderTime, 0L);
            }
        } catch (Exception e) {
            try {
                Tessellator.getInstance().draw();
            } catch (Exception ignored) {}
            throw new RuntimeException("Error rendering proxy world", e);
        } finally {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

            GL11.glViewport(0, 0, widthBackup, heightBackup);
            GL11.glLoadIdentity();

            mc.gameSettings.thirdPersonView = thirdPersonBackup;
            mc.gameSettings.hideGUI = hideGuiBackup;
            mc.gameSettings.particleSetting = particleBackup;
            mc.gameSettings.anaglyph = anaglyphBackup;
            mc.gameSettings.renderDistanceChunks = renderDistanceBackup;
            mc.gameSettings.fovSetting = FOVBackup;

            mc.displayHeight = heightBackup;
            mc.displayWidth = widthBackup;
        }
    }
}
