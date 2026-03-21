package com.retro.guiworkbench.runtime;

public final class LoggedScreenRecord {
    public String namespace;
    public String screenClass;
    public String title;
    public String lastOpenedUtc;
    public int openCount;

    public LoggedScreenRecord(String namespace, String screenClass, String title, String lastOpenedUtc, int openCount) {
        this.namespace = namespace;
        this.screenClass = screenClass;
        this.title = title;
        this.lastOpenedUtc = lastOpenedUtc;
        this.openCount = openCount;
    }
}
