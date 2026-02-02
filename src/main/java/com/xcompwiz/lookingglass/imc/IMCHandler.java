package com.xcompwiz.lookingglass.imc;

import com.google.common.collect.ImmutableList;
import com.xcompwiz.lookingglass.LookingGlass;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IMCHandler {
    interface IMCProcessor {
        void process(FMLInterModComms.IMCMessage message);
    }

    private static final Map<String, IMCProcessor> processors = new HashMap<>();

    static {
        registerProcessor("API", new IMCAPIRegister());
    }

    private static void registerProcessor(String key, IMCProcessor processor) {
        processors.put(key, processor);
    }

    public static void process(ImmutableList<FMLInterModComms.IMCMessage> messages) {
        for (FMLInterModComms.IMCMessage message : messages) {
            String key = message.key.toLowerCase(Locale.ENGLISH);
            IMCProcessor process = processors.get(key);
            if (process == null) {
                LookingGlass.logger().error("IMC message '{}' from [{}] unrecognized", key, message.getSender());
                continue;
            }
            try {
                process.process(message);
            } catch (Exception e) {
                LookingGlass.logger().error("Failed to process IMC message '{}' from [{}]", key, message.getSender());
                e.printStackTrace(System.err);
            }
        }
    }
}
