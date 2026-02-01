package com.xcompwiz.lookingglass.api.animator;

import net.minecraft.util.math.BlockPos;

public interface ICameraAnimator {
    void setTarget(BlockPos target);
    void refresh();
    void update(long dt);
}
