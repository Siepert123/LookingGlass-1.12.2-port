package com.xcompwiz.lookingglass.api;

public class APIVersionRemoved extends Exception {
    private static final long serialVersionUID	= -7702376017254522430L;

    public APIVersionRemoved(String message) {
        super(message);
    }
}
