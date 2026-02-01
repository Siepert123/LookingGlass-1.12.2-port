package com.xcompwiz.lookingglass.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.world.World;

import java.util.Random;

public abstract class CommandBaseAdv extends CommandBase {
    public static Integer getSenderDimension(ICommandSender sender) {
        World world = sender.getEntityWorld();
        return world.provider.getDimension();
    }

    public static double handleRelativeNumber(double origin, String arg) throws CommandException {
        return handleRelativeNumber(origin, arg, -30000000, 30000000);
    }
    public static double handleRelativeNumber(double origin, String arg, int min, int max) throws CommandException {
        boolean random = arg.startsWith("?");
        boolean relative = random || arg.startsWith("~");
        double d1 = relative ? origin : 0.0;

        if (!relative || arg.length() > 1) {
            boolean flag1 = arg.contains(".");

            if (relative) {
                arg = arg.substring(1);
            }

            double d2 = parseDouble(arg);
            if (random) {
                Random rnd = new Random();
                d1 += (rnd.nextDouble() * 2 - 1) * d2;
            } else {
                d1 += d2;
            }

            if (!flag1 && !relative) {
                d1 += 0.5;
            }
        }

        if (min != 0 || max != 0) {
            if (d1 < min) throw new NumberInvalidException("commands.generic.double.tooSmall", d1, min);
            if (d1 > max) throw new NumberInvalidException("commands.generic.double.tooBig", d1, max);
        }

        return d1;
    }
}