package com.xcompwiz.lookingglass.client.proxyworld;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;
import com.xcompwiz.lookingglass.api.view.IViewCamera;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.render.FrameBufferContainer;
import com.xcompwiz.lookingglass.entity.EntityCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldView implements IWorldView {
    private WorldClient world;
    public final BlockPos pos;
    public final EntityCamera camera;
    public final IViewCamera cameraWrapper;

    public final int width;
    public final int height;

    private boolean update;
    private boolean ready;
    private boolean hasChunks;
    private long lastRenderTime = -1L;

    private RenderGlobal renderGlobal;
    private ParticleManager effectRenderer;

    private FrameBufferContainer fbo;

    public WorldView(WorldClient world, BlockPos pos, int width, int height) {
        this.width = width;
        this.height = height;
        this.world = world;
        this.pos = pos;
        this.camera = new EntityCamera(this.world, this.pos);
        this.cameraWrapper = new ViewCameraImpl(this.camera);
        this.renderGlobal = new RenderGlobal(Minecraft.getMinecraft());
        this.effectRenderer = new ParticleManager(this.world, Minecraft.getMinecraft().getTextureManager());
        this.fbo = FrameBufferContainer.createNewFramebuffer(this, this.width, this.height);
    }

    public void cleanup() {
        this.fbo = null;
        FrameBufferContainer.removeWorldView(this);
    }

    @Override
    public boolean isReady() {
        return this.fbo != null && this.ready;
    }

    public boolean hasChunks() {
        return this.fbo != null && this.hasChunks;
    }

    @Override
    public void markDirty() {
        this.update = true;
    }

    public boolean markClean() {
        if (this.fbo == null) return false;
        this.ready = true;
        boolean temp = this.update;
        this.update = false;
        return temp;
    }

    public int getFramebuffer() {
        return this.fbo == null ? 0 : this.fbo.getFramebuffer();
    }

    public RenderGlobal getRenderGlobal() {
        return this.renderGlobal;
    }

    public ParticleManager getEffectRenderer() {
        return this.effectRenderer;
    }

    @Override
    public int getTexture() {
        return this.fbo == null ? 0 : this.fbo.getTexture();
    }

    @Override
    public void grab() {

    }

    @Override
    public boolean release() {
        return false;
    }

    public void onChunkReceived(int cx, int cz) {
        this.hasChunks = true;
        int camCX = MathHelper.floor(this.camera.posX) >> 4;
        int camCZ = MathHelper.floor(this.camera.posZ) >> 4;
        if (camCX >= cx - 1 && camCX <= cx + 1 && camCZ > cz - 1 && camCZ < cz + 1) this.camera.refreshAnimator();
    }

    public void updateWorldSpawn(BlockPos pos) {
        this.camera.updateWorldSpawn(pos);
    }

    public void startRender(long renderT) {
        if (this.lastRenderTime > 0) this.camera.tick(renderT - this.lastRenderTime);
        this.lastRenderTime = renderT;
    }

    @Override
    public void setAnimator(ICameraAnimator animator) {
        this.camera.setAnimator(animator);
    }

    @Override
    public IViewCamera getCamera() {
        return this.cameraWrapper;
    }

    public void replaceWorld(WorldClient world) {
        this.world = world;
        this.camera.setWorld(world);
        this.effectRenderer.clearEffects(world);
        this.renderGlobal.setWorldAndLoadRenderers(world);
    }

    public WorldClient getWorld() {
        return this.world;
    }
}
