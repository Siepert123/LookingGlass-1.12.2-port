package com.xcompwiz.lookingglass.client.render;

import com.google.common.collect.MapMaker;
import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

public class FrameBufferContainer {
    private static ConcurrentMap<WorldView, FrameBufferContainer> weakFBOMap = new MapMaker().weakKeys().makeMap();
    private static Collection<FrameBufferContainer> framebuffers = new HashSet<>();

    public static FrameBufferContainer createNewFramebuffer(WorldView view, int width, int height) {
        FrameBufferContainer fbo = new FrameBufferContainer(width, height);
        weakFBOMap.put(view, fbo);
        framebuffers.add(fbo);
        return fbo;
    }

    public static void removeWorldView(WorldView view) {
        weakFBOMap.remove(view);
    }

    public static void clearAll() {
        for (FrameBufferContainer fbo : framebuffers) {
            fbo.release();
        }
        framebuffers.clear();
    }

    public static synchronized void detectFreedWorldViews() {
        Collection<FrameBufferContainer> unpairedFBOs = new HashSet<>(framebuffers);
        unpairedFBOs.removeAll(weakFBOMap.values());
        if (unpairedFBOs.isEmpty()) return;
        LookingGlass.logger().info("Freeing {} loose framebuffers from expired world views", unpairedFBOs.size());
        for (FrameBufferContainer fbo : unpairedFBOs) {
            fbo.release();
        }
        framebuffers.removeAll(unpairedFBOs);
    }

    public final int width;
    public final int height;

    private int framebuffer;
    private int depthBuffer;
    private int texture;

    private FrameBufferContainer(int width, int height) {
        this.width = width;
        this.height = height;
        this.allocateFramebuffer();
    }

    private void release() {
        this.freeFramebuffer();
    }

    public int getFramebuffer() {
        return this.framebuffer;
    }

    public int getTexture() {
        return this.texture;
    }

    private synchronized void freeFramebuffer() {
        try {
            if (this.texture != 0) GL11.glDeleteTextures(this.texture);
            this.texture = 0;
            if (this.depthBuffer != 0) EXTFramebufferObject.glDeleteRenderbuffersEXT(this.depthBuffer);
            this.depthBuffer = 0;
            if (this.framebuffer != 0) EXTFramebufferObject.glDeleteFramebuffersEXT(this.framebuffer);
            this.framebuffer = 0;
        } catch (Exception e) {
            LookingGlass.logger().error("Error while cleaning up a world view framebuffer.");
        }
    }

    private void allocateFramebuffer() {
        if (this.framebuffer != 0) return;

        this.framebuffer = EXTFramebufferObject.glGenFramebuffersEXT();
        this.depthBuffer = EXTFramebufferObject.glGenRenderbuffersEXT();

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, this.framebuffer);

        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, this.depthBuffer);
        //Assuming stencil bits are available as MinecraftForgeClient.getStencilBits() doesn't exist anymore
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24,
                this.width, this.height);

        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT, this.depthBuffer);
        //EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
        //        EXTFramebufferObject.GL_RENDERBUFFER_EXT, this.depthBuffer);

        this.texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height,
                0, GL11.GL_RGBA, GL11.GL_INT, (ByteBuffer) null);
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, this.texture, 0);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
    }
}
