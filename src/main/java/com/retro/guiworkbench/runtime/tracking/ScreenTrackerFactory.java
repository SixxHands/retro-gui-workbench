package com.retro.guiworkbench.runtime.tracking;

import com.retro.guiworkbench.runtime.IScreenRegistry;

public final class ScreenTrackerFactory
{
    private ScreenTrackerFactory()
    {
    }

    public static ScreenTracker create(IScreenRegistry screenRegistry)
    {
        return new ScreenTracker(screenRegistry);
    }
}
