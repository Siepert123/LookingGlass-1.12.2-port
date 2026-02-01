package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.hook.WorldViewAPI2;
import com.xcompwiz.lookingglass.api.view.IWorldView;
import com.xcompwiz.lookingglass.client.proxyworld.ProxyWorldManager;
import com.xcompwiz.lookingglass.client.proxyworld.WorldView;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This is the API wrapper (instance) class for the WorldView API at version 2.
 * @author xcompwiz
 */
public class LookingGlassAPI2Wrapper extends APIWrapper implements WorldViewAPI2 {
    public LookingGlassAPI2Wrapper(String modname) {
        super(modname);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IWorldView createWorldView(Integer dimensionID, BlockPos pos, int width, int height) {
        return ProxyWorldManager.createWorldView(dimensionID, pos.toImmutable(), width, height);
    }

    @Override
    public void cleanupWorldView(IWorldView view) {
        if (view == null) return;
        if (!(view instanceof WorldView)) throw new RuntimeException("[%s] is misusing the LookingGlass API. Cannot cleanup custom IWorldView objects.");
        ProxyWorldManager.destroyWorldView((WorldView) view);
    }
}
