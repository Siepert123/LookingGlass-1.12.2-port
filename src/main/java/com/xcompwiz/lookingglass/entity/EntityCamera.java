package com.xcompwiz.lookingglass.entity;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityCamera extends EntityPlayerSP {
    private ICameraAnimator animator;
    private BlockPos target;
    private boolean defaultSpawn = false;
    private float fovMultiplier = 1.0F;

    public EntityCamera(World world, BlockPos spawn) {
        super(Minecraft.getMinecraft(), world, Minecraft.getMinecraft().getConnection(), null, null);
        this.target = spawn;
        if (this.target == null) {
            this.defaultSpawn = true;
            BlockPos pos = world.provider.getSpawnPoint();
            int y = this.updateTargetPosition(pos);
            this.target = new BlockPos(pos.getX(), y, pos.getZ());
        }
        this.setPosition(this.target.getX(), this.target.getY(), this.target.getZ());
    }

    public void setAnimator(ICameraAnimator animator) {
        this.animator = animator;
        if (this.animator != null) this.animator.setTarget(this.target);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0);
    }

    public void updateWorldSpawn(BlockPos spawn) {
        if (this.defaultSpawn) {
            int y = this.updateTargetPosition(spawn);
            spawn = new BlockPos(spawn.getX(), y, spawn.getZ());
            this.setPositionAndUpdate(spawn.getX(), spawn.getY(), spawn.getZ());
            if (this.animator != null) this.animator.setTarget(spawn);
            this.refreshAnimator();
        }
    }

    private int updateTargetPosition(BlockPos target) {
        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();
        if (!this.world.getChunkFromBlockCoords(target).isEmpty()) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            IBlockState state = this.world.getBlockState(target);
            if (state.getCollisionBoundingBox(this.world, target) != null) {
                do {
                    state = this.world.getBlockState(mutable.setPos(x, --y, z));
                } while (y > 0 && state.getCollisionBoundingBox(this.world, mutable) != null);
                if (y == 0) y = target.getY();
                else ++y;
            } else {
                do {
                    state = this.world.getBlockState(mutable.setPos(x, ++y, z));
                } while (y < 256 && state.getCollisionBoundingBox(this.world, mutable) == null);
                if (y == 256) y = target.getY();
            }
            return y;
        }
        return target.getY();
    }

    public void refreshAnimator() {
        if (this.animator != null) this.animator.refresh();
    }

    public void tick(long dt) {
        if (this.animator != null) this.animator.update(dt);
    }

    @Override
    public float getFovModifier() {
        return this.fovMultiplier;
    }

    public void setFOVMult(float mult) {
        this.fovMultiplier = mult;
    }

    @Override
    public void onEntityUpdate() {

    }

    @Override
    public void onLivingUpdate() {

    }

    @Override
    public void onUpdate() {

    }

    @Override
    protected int getExperiencePoints(EntityPlayer player) {
        return 0;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public boolean writeToNBTAtomically(NBTTagCompound compound) {
        return false;
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound compound) {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {

    }

    @Override
    protected void collideWithNearbyEntities() {

    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {

    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public void onStruckByLightning(EntityLightningBolt lightningBolt) {

    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean getIsInvulnerable() {
        return true;
    }

    @Override
    public boolean isSneaking() {
        return false;
    }

    @Override
    public boolean isRiding() {
        return false;
    }

    @Override
    public boolean isBurning() {
        return false;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {

    }

    @Override
    public int getBrightnessForRender() {
        return 0;
    }

    @Override
    public float getBrightness() {
        return 0.0F;
    }

    @Override
    protected void handleJumpLava() {

    }

    @Override
    protected void handleJumpWater() {

    }

    @Override
    public boolean handleWaterMovement() {
        return false;
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {

    }

    @Override
    public void moveRelative(float strafe, float up, float forward, float friction) {

    }

    @Override
    public void moveToBlockPosAndAngles(BlockPos pos, float rotationYawIn, float rotationPitchIn) {

    }

    @Override
    protected boolean canBeRidden(Entity entityIn) {
        return false;
    }

    @Override
    protected boolean canDropLoot() {
        return false;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canAttackPlayer(EntityPlayer other) {
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }
}
