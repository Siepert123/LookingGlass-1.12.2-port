package com.xcompwiz.lookingglass.client.render;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorld;
import com.xcompwiz.lookingglass.entity.EntityPortal;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderPortal extends Render<EntityPortal> {
    public RenderPortal(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityPortal entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.world instanceof ProxyWorld && ModConfigs.disableRenderInRenderPortal) return; //Render-in-render? No! (maybe)
        IWorldView activeView = entity.getActiveView();
        if (activeView == null) return;

        int texture = activeView.getTexture();
        if (texture == 0) return;

        int width = 2;
        int height = 3;
        double left = -width / 2.0;
        double top = 0.0;

        activeView.markDirty();
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.bindTexture(texture);
        GlStateManager.color(1, 1, 1);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        builder.pos(left, top, 0.0).tex(0.0, 0.0).endVertex();
        builder.pos(width + left, top, 0.0).tex(1.0, 0.0).endVertex();
        builder.pos(width + left, height + top, 0.0).tex(1.0, 1.0).endVertex();
        builder.pos(left, height + top, 0.0).tex(0.0, 1.0).endVertex();
        tessellator.draw();

        GlStateManager.bindTexture(0);
        GlStateManager.color(0, 0, 1);
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        builder.pos(left, height + top, 0.0).endVertex();
        builder.pos(width + left, height + top, 0.0).endVertex();
        builder.pos(width + left, top, 0.0).endVertex();
        builder.pos(left, top, 0.0).endVertex();
        tessellator.draw();

        GlStateManager.color(1, 1, 1);
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
    }

    @Override
    protected boolean bindEntityTexture(EntityPortal entity) {
        return false;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPortal entity) {
        return null;
    }
}
