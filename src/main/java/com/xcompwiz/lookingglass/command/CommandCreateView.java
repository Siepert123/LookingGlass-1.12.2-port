package com.xcompwiz.lookingglass.command;

import com.xcompwiz.lookingglass.entity.EntityPortal;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandCreateView extends CommandBaseAdv {
    @Override
    public String getName() {
        return "lg-viewdim";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + this.getName() + " <dimensionID> [dimension] [x y z]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int targetDim = 0;
        Integer dim = null;
        BlockPos pos = null;

        if (args.length > 0) {
            String sTarget = args[0];
            targetDim = parseInt(sTarget);
        } else {
            throw new WrongUsageException("A target dimension is required!");
        }
        if (args.length > 4) {
            dim = parseInt(args[1]);
            Entity caller = sender.getCommandSenderEntity();
            int x = (int) handleRelativeNumber((caller != null ? caller.posX : 0.0), args[2]);
            int y = (int) handleRelativeNumber((caller != null ? caller.posY : 0.0), args[3], 0, 0);
            int z = (int) handleRelativeNumber((caller != null ? caller.posZ : 0.0), args[4]);
            pos = new BlockPos(x, y, z);
        }
        if (pos == null) {
            dim = getSenderDimension(sender);
            pos = sender.getPosition();
        }

        WorldServer world = DimensionManager.getWorld(dim);
        if (world == null) throw new CommandException("lookingglass.commands.generic.world.notloaded");

        EntityPortal portal = new EntityPortal(world, targetDim, pos.getX(), pos.getY(), pos.getZ());
        world.spawnEntity(portal);
    }
}
