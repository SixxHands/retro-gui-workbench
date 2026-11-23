package com.retro.guiworkbench.runtime;

public final class OpenedScreenRegistryFactory
{
    private OpenedScreenRegistryFactory()
    {
    }

    public static IScreenRegistry create()
    {
        return new OpenedScreenRegistry();
    }
}
