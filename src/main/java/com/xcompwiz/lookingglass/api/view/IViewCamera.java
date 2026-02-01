package com.xcompwiz.lookingglass.api.view;

import net.minecraft.world.IBlockAccess;

public interface IViewCamera {
    void addRotations(float yaw, int pitch);
    void setYaw(float f);
    float getYaw();
    void setPitch(float f);
    float getPitch();

    void setLocation(double x, double y, double z);
    double getX();
    double getY();
    double getZ();

    IBlockAccess getBlockData();
    boolean chunkExists(int x, int z);
    boolean chunkLevelsExist(int x, int z, int yl1, int yl2);
}
