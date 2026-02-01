package com.xcompwiz.lookingglass.api.animator;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CameraAnimatorPlayer implements ICameraAnimator {
    private final IViewCamera camera;

    private Entity reference;
    private Entity player;
    private BlockPos target;
    private boolean updateY;
    private float accumulate;

    public CameraAnimatorPlayer(IViewCamera camera, Entity reference, Entity player) {
        this.camera = camera;
        this.reference = reference;
        this.player = player;
    }

    @Override
    public void setTarget(BlockPos target) {
        this.target = target.toImmutable();
    }

    @Override
    public void refresh() {
        this.updateY = true;
    }

    @Override
    public void update(long dt) {
        if (this.reference.world.provider.getDimension() != this.player.world.provider.getDimension()) return;

        if ((this.accumulate += dt) >= 10000) {
            this.updateY = true;
            this.accumulate -= 10000;
        }
        if (this.updateY) this.updateTargetPosition();
        double dx = this.player.posX - this.reference.posY;
        double dy = this.player.posY - (this.reference.posY + this.player.getYOffset());
        double dz = this.player.posZ - this.reference.posZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float yaw = -(float) Math.atan2(dx, dz);
        yaw *= (float) (180 / Math.PI);
        float pitch = (float) Math.asin(dy / length);
        pitch *= (float) (180 / Math.PI);
        this.camera.setLocation(this.target.getX(), this.target.getY(), this.target.getZ());
        this.camera.setYaw(yaw);
        this.camera.setPitch(pitch);
    }

    private void updateTargetPosition() {
        this.updateY = false;
        int x = this.target.getX();
        int y = this.target.getY();
        int z = this.target.getZ();
        if (!this.camera.chunkExists(x, z)) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(this.target);
            IBlockAccess world = this.camera.getBlockData();
            IBlockState state = world.getBlockState(this.target);
            if (state.getCollisionBoundingBox(world, mutable) != null) {
                do {
                    state = world.getBlockState(mutable.setPos(x, --y, z));
                } while (y > 0 && state.getCollisionBoundingBox(world, mutable) != null);
                if (y == 0) y = this.target.getY();
                else y += 2;
            } else {
                do {
                    state = world.getBlockState(mutable.setPos(x, ++y, z));
                } while (y < 256 && state.getCollisionBoundingBox(world, mutable) == null);
                if (y == 256) y = this.target.getY();
                else ++y;
            }
            this.target = new BlockPos(x, y, z);
        }
    }
}
