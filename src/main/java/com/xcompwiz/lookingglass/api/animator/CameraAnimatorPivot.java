package com.xcompwiz.lookingglass.api.animator;

import com.xcompwiz.lookingglass.api.view.IViewCamera;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CameraAnimatorPivot implements ICameraAnimator {
    private static final int[][] presets = {{2, 5}, {5, 9}, {9, 15}, {1, 3}, {2, 1}, {0, 2}};
    private static final int[] defaults = {1, 3};

    private final IViewCamera camera;
    private BlockPos target;

    private boolean positionSet = false;

    private int xCenter, yCenter, zCenter;
    private int yUp = 0;
    private int radius = 0;
    private float pitch = 0.0F;

    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    public CameraAnimatorPivot(IViewCamera camera) {
        this.camera = camera;
    }

    @Override
    public void setTarget(BlockPos target) {
        this.target = target;
        this.positionSet = false;
    }

    @Override
    public void update(long dt) {
        if (this.camera == null) return;
        this.camera.addRotations(dt*0.1F, 0);
        this.camera.setPitch(-this.pitch);

        double x = Math.cos(Math.toRadians(this.camera.getYaw() + 90)) * this.radius;
        double z = Math.sin(Math.toRadians(this.camera.getYaw() + 90)) * this.radius;
        this.camera.setLocation(this.xCenter + 0.5 - x, this.yCenter - 0.5 + this.yUp, this.zCenter + 0.5 - z);
    }

    @Override
    public void refresh() {
        if (this.camera == null) return;
        if (this.target == null) return;
        if (!this.positionSet) this.checkCameraY();

        int chunkX = this.xCenter >> 4;
        int chunkY = this.yCenter >> 4;
        int chunkZ = this.zCenter >> 4;

        int[][] presets = this.getPresets();
        for (int i = 0; i < presets.length; i++) {
            if (this.checkPath(presets[i][0], presets[i][1], chunkX, chunkY, chunkZ)) {
                this.yUp = presets[i][0];
                this.radius = presets[i][1];
                this.pitch = (float) Math.toDegrees(Math.atan((double) -this.yUp / this.radius));
                return;
            }
        }
        int[] defaults = this.getDefaults();
        this.yUp = defaults[0];
        this.radius = defaults[1];
        this.pitch = (float) Math.toDegrees(Math.atan((double) -this.yUp / this.radius));
    }

    public int[] getDefaults() {
        return defaults;
    }

    public int[][] getPresets() {
        return presets;
    }

    private boolean checkPath(int up, int distance, int chunkX, int chunkY, int chunkZ) {
        if ((this.yCenter & 15) > 15 - up) {
            if (isAboveNullLayer(chunkX, chunkY, chunkZ)) return false;
            if ((this.xCenter & 15) < distance) {
                if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ)) return false;
                if ((this.zCenter & 15) < distance) {
                    if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ - 1)) return false;
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
                } else if ((this.zCenter & 15) > 15 - distance) {
                    if (isAboveNullLayer(chunkX - 1, chunkY, chunkZ + 1)) return false;
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
                }
            } else if ((this.xCenter & 15) > 15 - distance) {
                if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ)) return false;
                if ((this.zCenter & 15) < distance) {
                    if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ - 1)) return false;
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
                } else if ((this.zCenter & 15) > 15 - distance) {
                    if (isAboveNullLayer(chunkX + 1, chunkY, chunkZ + 1)) return false;
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
                }
            } else {
                if ((this.zCenter & 15) < distance) {
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ - 1)) return false;
                } else if ((this.zCenter & 15) > 15 - distance) {
                    if (isAboveNullLayer(chunkX, chunkY, chunkZ + 1)) return false;
                }
            }
        }
        for (int j = -distance; j <= distance; ++j) {
            for (int k = -distance; k <= distance; ++k) {
                if (!this.camera.getBlockData()
                        .isAirBlock(this.mutable.setPos(this.xCenter + j, this.yCenter + up, this.zCenter + k))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isAboveNullLayer(int x, int y, int z) {
        if (y + 1 > 15) return true;
        int x2 = x << 4;
        int z2 = z << 4;
        int y2 = (y << 4) + 15;
        int yl = (y + 1) << 4;
        for (int i = 0; i < 15; i++)
            for (int j = 0; j < 15; j++)
                if (!this.isBlockNormalCube(this.camera.getBlockData(), this.mutable.setPos(x2 + i, y2, z2 + i))) return false;
        return this.camera.chunkLevelsExist(x, z, yl, yl + 15);
    }

    private boolean isBlockNormalCube(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.isNormalCube();
    }

    private void checkCameraY() {
        int x = this.target.getX();
        int y = this.target.getY();
        int z = this.target.getZ();
        int yBackup = y;
        if (this.camera.chunkExists(x, z)) {
            IBlockAccess world = this.camera.getBlockData();
            IBlockState state = world.getBlockState(this.mutable.setPos(x, y, z));
            if (state.getCollisionBoundingBox(world, this.mutable) != null) {
                do {
                    state = world.getBlockState(this.mutable.setPos(x, --y, z));
                } while (y > 0 && state.getCollisionBoundingBox(world, this.mutable) != null);
                if (y == 0) y = yBackup;
                else y += 2;
            } else {
                do {
                    state = world.getBlockState(this.mutable.setPos(x, ++y, z));
                } while (y < 256 && state.getCollisionBoundingBox(world, this.mutable) == null);
                if (y == 256) y = yBackup;
                else ++y;
            }
            this.setCenterPoint(x, y, z);
        }
    }

    private void setCenterPoint(int x, int y, int z) {
        this.xCenter = x;
        this.yCenter = y;
        this.zCenter = z;
        this.positionSet = true;
    }
}
