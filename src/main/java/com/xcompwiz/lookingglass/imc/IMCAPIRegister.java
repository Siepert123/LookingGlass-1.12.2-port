package com.xcompwiz.lookingglass.imc;

import com.xcompwiz.lookingglass.LookingGlass;
import com.xcompwiz.lookingglass.api.APIInstanceProvider;
import com.xcompwiz.lookingglass.apiimpl.InternalAPI;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.lang.reflect.Method;

public class IMCAPIRegister implements IMCHandler.IMCProcessor {
    @Override
    public void process(FMLInterModComms.IMCMessage message) {
        if (!message.isStringMessage()) return;
        LookingGlass.logger().info("Receiving API registration request from [{}] for method {}", message.getSender(), message.getStringValue());
        callbackRegistration(message.getStringValue(), message.getSender());
    }

    public static void callbackRegistration(String method, String modname) {
        String[] splitName = method.split("\\.");
        String methodName = splitName[splitName.length - 1];
        String className = method.substring(0, method.length() - methodName.length() - 1);

        APIInstanceProvider provider = InternalAPI.getAPIProviderInstance(modname);
        if (provider == null) {
            LookingGlass.logger().error("Could not initialize API provider instance for {}", modname);
            return;
        }
        LookingGlass.logger().info("Trying to call (reflection) {} {}", className, methodName);

        try {
            Class<?> reflectClass = Class.forName(className);
            Method reflectMethod = reflectClass.getDeclaredMethod(methodName, APIInstanceProvider.class);
            reflectMethod.invoke(null, provider);
            LookingGlass.logger().info("API provided to {}", modname);
        } catch (ClassNotFoundException e) {
            LookingGlass.logger().error("Could not find class {}", className);
        } catch (NoSuchMethodException e) {
            LookingGlass.logger().error("Could not find method {}", methodName);
        } catch (Exception e) {
            LookingGlass.logger().error("Exception while calling the method {}.{}", className, methodName);
            e.printStackTrace(System.err);
        }
    }
}
