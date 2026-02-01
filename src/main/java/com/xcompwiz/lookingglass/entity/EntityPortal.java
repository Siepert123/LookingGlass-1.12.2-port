package com.xcompwiz.lookingglass.entity;

import com.xcompwiz.lookingglass.api.animator.CameraAnimatorPivot;
import com.xcompwiz.lookingglass.api.animator.CameraAnimatorPlayer;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import com.xcompwiz.lookingglass.proxyworld.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityPortal extends Entity {
    public static final DataParameter<Integer> TARGET_DIMENSION = EntityDataManager.createKey(EntityPortal.class, DataSerializers.VARINT);

    private long lifetime = 1000L;

    @SideOnly(Side.CLIENT)
    private IWorldView activeView;

    public EntityPortal(World world) {
        super(world);
        this.dataManager.register(TARGET_DIMENSION, 0);
    }
    public EntityPortal(World world, int dimension, int posX, int posY, int posZ) {
        this(world);
        this.setTarget(dimension);
        this.setPosition(posX, posY, posZ);
    }

    private void setTarget(int dimension) {
        this.dataManager.set(TARGET_DIMENSION, dimension);
    }

    private int getTarget() {
        return this.dataManager.get(TARGET_DIMENSION);
    }

    @Override
    protected void entityInit() {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void setDead() {
        super.setDead();
        this.releaseActiveView();
    }

    @Override
    public void onUpdate() {
        --this.lifetime;
        if (this.lifetime <= 0) {
            this.setDead();
            return;
        }
        super.onUpdate();
    }

    @SideOnly(Side.CLIENT)
    public IWorldView getActiveView() {
        if (!this.world.isRemote) return null;
        if (this.activeView == null) {
            this.activeView = ProxyWorldManager.createWorldView(this.getTarget(), null, 160, 240);
            if (this.activeView != null) {
                if (ModConfigs.alternativePortal) {
                    this.activeView.setAnimator(new CameraAnimatorPlayer(this.activeView.getCamera(),
                            this, Minecraft.getMinecraft().player));
                } else {
                    this.activeView.setAnimator(new CameraAnimatorPivot(this.activeView.getCamera()));
                }
            }
        }
        return this.activeView;
    }

    @SideOnly(Side.CLIENT)
    public void releaseActiveView() {
        if (this.activeView != null) ProxyWorldManager.destroyWorldView((WorldView) this.activeView);
        this.activeView = null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        //I do not agree with the capitalization but compatibility with 1.7.10 is more important than my opinion
        nbt.setInteger("Dimension", this.getTarget());
        nbt.setLong("lifetime", this.lifetime);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.setTarget(nbt.getInteger("Dimension"));
        this.lifetime = nbt.getLong("lifetime");
    }
}
