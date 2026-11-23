package com.retro.guiworkbench.export.sources;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class GameplayGuiFilter
{
    private static final List<String> IncludedFragments = List.of(
            "gui",
            "screen",
            "container",
            "inventory",
            "slot",
            "terminal",
            "backpack",
            "storage",
            "tab",
            "button",
            "bar");

    private static final List<String> ExcludedFragments = List.of(
            "title",
            "main_menu",
            "realms",
            "logo",
            "accessibility",
            "options");

    private GameplayGuiFilter()
    {
    }

    // Rough filter to grab gameplay menus and skip title screens, options, and Realms clutter.
    public static boolean isGameplayGuiPath(String resourcePath)
    {
        if (resourcePath == null || resourcePath.isBlank())
        {
            return false;
        }

        String lowered = resourcePath.toLowerCase(Locale.ROOT);
        if (!containsAny(lowered, IncludedFragments))
        {
            return false;
        }

        return !containsAny(lowered, ExcludedFragments);
    }

    public static Set<String> cleanWhitelist(Collection<? extends String> rawEntries)
    {
        Set<String> cleaned = new LinkedHashSet<>();
        if (rawEntries == null)
        {
            return Set.of();
        }

        for (String value : rawEntries)
        {
            if (value != null && !value.isBlank())
            {
                cleaned.add(value.trim().toLowerCase(Locale.ROOT));
            }
        }

        return Set.copyOf(cleaned);
    }

    private static boolean containsAny(String value, List<String> fragments)
    {
        for (String fragment : fragments)
        {
            if (value.contains(fragment))
            {
                return true;
            }
        }

        return false;
    }
}
