package com.xcompwiz.lookingglass.utils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;

public class MathUtils {
    public static Vec3d readCoordinates(ByteBuf data) {
        return new Vec3d(data.readDouble(), data.readDouble(), data.readDouble());
    }
    public static void writeCoordinates(ByteBuf data, Vec3d vec) {
        data.writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z);
    }
}
