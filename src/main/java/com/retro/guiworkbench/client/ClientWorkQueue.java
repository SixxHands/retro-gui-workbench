package com.retro.guiworkbench.client;

public final class ClientWorkQueue {
    private static volatile boolean requestExportAll;
    private static volatile boolean requestExportOpened;
    private static volatile Boolean requestLoggingState;
    private static volatile boolean requestLoggingToggle;

    private ClientWorkQueue() {}

    public static void requestExportAll() {
        requestExportAll = true;
    }

    public static void requestExportOpened() {
        requestExportOpened = true;
    }

    public static void requestLoggingState(boolean enabled) {
        requestLoggingState = enabled;
    }

    public static void requestLoggingToggle() {
        requestLoggingToggle = true;
    }

    public static boolean consumeExportAll() {
        boolean out = requestExportAll;
        requestExportAll = false;
        return out;
    }

    public static boolean consumeExportOpened() {
        boolean out = requestExportOpened;
        requestExportOpened = false;
        return out;
    }

    public static Boolean consumeRequestedLoggingState() {
        Boolean out = requestLoggingState;
        requestLoggingState = null;
        return out;
    }

    public static boolean consumeLoggingToggle() {
        boolean out = requestLoggingToggle;
        requestLoggingToggle = false;
        return out;
    }
}
