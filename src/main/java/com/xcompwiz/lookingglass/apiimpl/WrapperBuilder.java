package com.xcompwiz.lookingglass.apiimpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class WrapperBuilder {
    private final Constructor<?> itemCtor;

    public WrapperBuilder(Class<?> clazz) {
        try {
            this.itemCtor = clazz.getConstructor(String.class);
        } catch (Exception e) {
            throw new RuntimeException("LookingGlass has derped.", e);
        }
    }

    public Object newInstance(String owner) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return this.itemCtor.newInstance(owner);
    }
}
