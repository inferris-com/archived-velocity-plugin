package com.inferris.common;

import java.awt.*;

public enum ColorType {
    HEADER(new Color(106, 137, 252)),
    HEADER2(new Color(30, 144, 255)),
    BRAND_PRIMARY(new Color(110, 69, 226)),
    BRAND_SECONDARY(new Color(0, 191, 255)),
    LUMINA(new Color(137, 116, 232));

    private final Color color;
    ColorType(Color color){
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}

