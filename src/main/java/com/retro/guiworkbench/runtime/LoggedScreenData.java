package com.retro.guiworkbench.runtime;

import java.util.Objects;

public record LoggedScreenData(
        String namespace,
        String screenClass,
        String title,
        String lastOpenedUtc,
        int openCount)
{
    public LoggedScreenData
    {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(screenClass, "screenClass");
        title = title == null ? "" : title;
        lastOpenedUtc = lastOpenedUtc == null ? "" : lastOpenedUtc;
        if (openCount < 0)
        {
            throw new IllegalArgumentException("openCount must be non-negative.");
        }
    }

    public LoggedScreenData recordOpen(String newTitle, String openedUtc)
    {
        String keptTitle = (title == null || title.isBlank()) && newTitle != null ? newTitle : title;
        return new LoggedScreenData(namespace, screenClass, keptTitle, openedUtc, openCount + 1);
    }
}
