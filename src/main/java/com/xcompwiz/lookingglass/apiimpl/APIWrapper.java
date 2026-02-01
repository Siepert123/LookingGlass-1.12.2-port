package com.xcompwiz.lookingglass.apiimpl;

public class APIWrapper {
    private final String modname;

    public APIWrapper(String modname) {
        this.modname = modname;
    }

    public String getOwnerMod() {
        return this.modname;
    }
}
