package com.xcompwiz.lookingglass.api.view;

import com.xcompwiz.lookingglass.api.animator.ICameraAnimator;

public interface IWorldView {
    int getTexture();
    void markDirty();
    boolean isReady();
    void setAnimator(ICameraAnimator animator);
    IViewCamera getCamera();
    @Deprecated
    void grab();
    @Deprecated
    boolean release();
}
