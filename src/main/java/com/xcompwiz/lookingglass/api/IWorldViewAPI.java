package com.xcompwiz.lookingglass.api;

import com.xcompwiz.lookingglass.api.view.IWorldView;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Deprecated
public interface IWorldViewAPI {
    @SideOnly(Side.CLIENT)
    IWorldView createWorldView(Integer dimensionID, BlockPos pos, int width, int height);
}
