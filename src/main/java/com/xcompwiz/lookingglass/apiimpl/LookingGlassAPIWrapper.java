package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.IWorldViewAPI;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is the API wrapper (instance) class for the WorldView API at version 1.
 * @author xcompwiz
 */
@SuppressWarnings("deprecation")
public class LookingGlassAPIWrapper extends APIWrapper implements IWorldViewAPI {
    public LookingGlassAPIWrapper(String modname) {
        super(modname);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IWorldView createWorldView(Integer dimensionID, BlockPos pos, int width, int height) {
        return ProxyWorldManager.createWorldView(dimensionID, pos.toImmutable(), width, height);
    }
}
