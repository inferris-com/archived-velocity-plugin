package com.inferris.server;

public class PlayerCountManager {
    private static Integer overriddenCount = null;

    public static Integer getOverriddenCount() {
        return overriddenCount;
    }

    public static void setOverriddenCount(Integer count) {
        overriddenCount = count;
    }

    public static void resetOverriddenCount() {
        overriddenCount = null;
    }

    public static boolean isOverridden() {
        return overriddenCount != null;
    }
}
