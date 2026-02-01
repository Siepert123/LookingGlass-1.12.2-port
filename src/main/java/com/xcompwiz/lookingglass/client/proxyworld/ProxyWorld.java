package com.xcompwiz.lookingglass.client.proxyworld;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@SideOnly(Side.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProxyWorld extends WorldClient {
    public ProxyWorld(int dimensionID) {
        super(Minecraft.getMinecraft().getConnection(),
                new WorldSettings(0L, GameType.SURVIVAL, true, false, WorldType.DEFAULT),
                dimensionID,
                Minecraft.getMinecraft().gameSettings.difficulty,
                Minecraft.getMinecraft().world.profiler
        );
    }

    @Override
    public void playSound(double x, double y, double z,
                          SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {

    }

    @Override
    public void playSound(@Nullable EntityPlayer player, BlockPos pos,
                          SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playSound(BlockPos pos,
                          SoundEvent soundIn, SoundCategory category, float volume, float pitch, boolean distanceDelay) {

    }

    @Override
    public void playSound(@Nullable EntityPlayer player, double x, double y, double z,
                          SoundEvent soundIn, SoundCategory category, float volume, float pitch) {

    }

    @Override
    public void playBroadcastSound(int id, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {

    }

    @Override
    public void playEvent(@Nullable EntityPlayer player, int type, BlockPos pos, int data) {

    }

    @Override
    public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ, @Nullable NBTTagCompound compound) {
        for (WorldView activeView : ProxyWorldManager.getWorldViews(this.provider.getDimension())) {
            activeView.getEffectRenderer().addEffect(
                    new ParticleFirework.Starter(
                            this, x, y, z, motionX, motionY, motionZ, activeView.getEffectRenderer(), compound
                    )
            );
        }
    }
}
