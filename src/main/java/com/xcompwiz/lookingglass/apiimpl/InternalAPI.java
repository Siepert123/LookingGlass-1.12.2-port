package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.APIInstanceProvider;

import java.util.HashMap;

public class InternalAPI {
    private static final HashMap<String, APIInstanceProvider> instances = new HashMap<>();

    public synchronized static APIInstanceProvider getAPIProviderInstance(String modname) {
        return instances.computeIfAbsent(modname, APIProviderImpl::new);
    }
}
