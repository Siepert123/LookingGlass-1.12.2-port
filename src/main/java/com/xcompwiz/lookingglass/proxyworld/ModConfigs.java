package com.xcompwiz.lookingglass.proxyworld;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ModConfigs {
    private static final String CATEGORY_SERVER = "server";
    public static boolean disabled = false;
    public static int dataRate = 2048;
    public static byte renderDistance = 7;
    public static boolean alternativePortal = false;
    public static boolean disableRenderInRenderPortal = false;
    public static boolean forceLoadAllWorlds = false;

    public static void loadConfig(Configuration config) {
        Property off = config.get(CATEGORY_SERVER, "disabled", disabled);
        off.setComment("On the client this disabled world renders, entirely, preventing world requests. On the server this disables sending world info to all clients.");
        disabled = off.getBoolean(disabled);

        Property data = config.get(CATEGORY_SERVER, "datarate", dataRate);
        data.setComment("The number of bytes to send per tick before the server cuts off sending. Only applies to other-world chunks. Default: " + dataRate);
        dataRate = data.getInt(dataRate);

        if (dataRate <= 0) disabled = true;

        Property alternative = config.get(CATEGORY_SERVER, "alternativePortal", alternativePortal);
        alternative.setComment("Whether the portal should have an alternative animation (debug)");
        alternativePortal = alternative.getBoolean();

        Property renderInRender = config.get(CATEGORY_SERVER, "disableRenderInRender", disableRenderInRenderPortal);
        renderInRender.setComment("Whether to allow render portals to render render portals in them");
        disableRenderInRenderPortal = renderInRender.getBoolean();

        Property loading = config.get(CATEGORY_SERVER, "forceLoadAllWorlds", forceLoadAllWorlds);
        loading.setComment("When all else fails, enable this (default: false)");
        forceLoadAllWorlds = loading.getBoolean();

        if (config.hasChanged()) config.save();
    }
}
