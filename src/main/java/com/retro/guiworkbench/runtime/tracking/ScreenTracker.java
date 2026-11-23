package com.retro.guiworkbench.runtime.tracking;

import com.retro.guiworkbench.RetroGuiWorkbench;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class ScreenTracker
{
    private static final List<String> IgnoredScreenFragments = List.of(
            "title",
            "pause",
            "options",
            "advancements",
            "chat",
            "social",
            "disconnect",
            "multiplayer",
            "worldselection",
            "language",
            "controls",
            "telemetry",
            "recipebook");

    private static final List<NamespaceRuleData> KnownNamespaceRules = List.of(
            new NamespaceRuleData("sophisticatedbackpacks", "sophisticatedbackpacks"),
            new NamespaceRuleData("sophisticatedstorage", "sophisticatedstorage"),
            new NamespaceRuleData("sophisticatedcore", "sophisticatedcore"),
            new NamespaceRuleData("toms_storage", "toms_storage"),
            new NamespaceRuleData("tomsstorage", "toms_storage"),
            new NamespaceRuleData("tom.simple", "toms_storage"),
            new NamespaceRuleData("minecraft", "minecraft"));

    private static final List<String> KnownPackagePrefixes = List.of("com", "net", "org");

    private final IScreenRegistry _screenRegistry;
    private Screen _lastScreen;
    private boolean _runtimeLoggingEnabled;

    ScreenTracker(IScreenRegistry screenRegistry)
    {
        _screenRegistry = Objects.requireNonNull(screenRegistry, "screenRegistry");
    }

    public void startFromConfig()
    {
        _runtimeLoggingEnabled = GuiWorkbenchConfig.EnableRuntimeLogging.get();
    }

    public boolean isRuntimeLoggingEnabled()
    {
        return _runtimeLoggingEnabled;
    }

    public void setRuntimeLoggingEnabled(boolean enabled)
    {
        _runtimeLoggingEnabled = enabled;
        RetroGuiWorkbench.Logger.info("Runtime GUI logging {}", enabled ? "enabled" : "disabled");
    }

    public void toggleRuntimeLogging()
    {
        setRuntimeLoggingEnabled(!_runtimeLoggingEnabled);
    }

    public void tick(Minecraft minecraft)
    {
        if (!_runtimeLoggingEnabled || minecraft == null)
        {
            return;
        }

        Screen current = minecraft.screen;
        if (current == _lastScreen)
        {
            return;
        }

        _lastScreen = current;
        if (current == null)
        {
            return;
        }

        if (GuiWorkbenchConfig.IgnoreNonContainerScreens.get()
                && isNonGameplayScreenName(current.getClass().getSimpleName()))
        {
            return;
        }

        String screenClass = current.getClass().getName();
        String namespace = findNamespace(screenClass);
        String title = current.getTitle() == null ? "" : current.getTitle().getString();

        _screenRegistry.record(namespace, screenClass, title);
        RetroGuiWorkbench.Logger.info("Logged screen: [{}] {} ({})", namespace, screenClass, title);
    }

    public static boolean isNonGameplayScreenName(String simpleClassName)
    {
        if (simpleClassName == null || simpleClassName.isBlank())
        {
            return true;
        }

        String lowered = simpleClassName.toLowerCase(Locale.ROOT);
        if (lowered.contains("creat") && lowered.contains("world"))
        {
            return true;
        }

        for (String fragment : IgnoredScreenFragments)
        {
            if (lowered.contains(fragment))
            {
                return true;
            }
        }

        return false;
    }

    // No mod id for screens, so guess...
    public static String findNamespace(String screenClassName)
    {
        if (screenClassName == null || screenClassName.isBlank())
        {
            return "unknown";
        }

        String lowered = screenClassName.toLowerCase(Locale.ROOT);
        for (NamespaceRuleData rule : KnownNamespaceRules)
        {
            if (lowered.contains(rule.fragment()))
            {
                return rule.namespace();
            }
        }

        String[] parts = screenClassName.split("\\.");
        for (int index = 0; index < parts.length - 1; index++)
        {
            if (!KnownPackagePrefixes.contains(parts[index]))
            {
                return parts[index].toLowerCase(Locale.ROOT);
            }
        }

        return "unknown";
    }
}
