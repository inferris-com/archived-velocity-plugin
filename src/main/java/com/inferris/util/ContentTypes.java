package com.inferris.util;

public enum ContentTypes {
    X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    PLAIN("text/plain");

    public final String type;
    ContentTypes(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
