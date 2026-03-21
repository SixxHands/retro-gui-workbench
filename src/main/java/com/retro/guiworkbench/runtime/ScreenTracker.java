package com.retro.guiworkbench.runtime;

import com.retro.guiworkbench.RetroGuiWorkbench;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.Locale;

public final class ScreenTracker {
    private static Screen lastScreen;
    private static boolean runtimeLoggingEnabled;

    private ScreenTracker() {}

    public static void bootstrapFromConfig() {
        runtimeLoggingEnabled = GuiWorkbenchConfig.ENABLE_RUNTIME_LOGGING.get();
    }

    public static boolean isRuntimeLoggingEnabled() {
        return runtimeLoggingEnabled;
    }

    public static void setRuntimeLoggingEnabled(boolean enabled) {
        runtimeLoggingEnabled = enabled;
        RetroGuiWorkbench.LOGGER.info("Runtime GUI logging {}", enabled ? "enabled" : "disabled");
    }

    public static void toggleRuntimeLogging() {
        setRuntimeLoggingEnabled(!runtimeLoggingEnabled);
    }

    public static void tick(Minecraft minecraft) {
        if (!runtimeLoggingEnabled || minecraft == null) {
            return;
        }

        Screen current = minecraft.screen;
        if (current == lastScreen) {
            return;
        }

        lastScreen = current;
        if (current == null) {
            return;
        }

        if (GuiWorkbenchConfig.IGNORE_NON_CONTAINER_SCREENS.get() && isLikelyNonGameplayScreen(current)) {
            return;
        }

        String screenClass = current.getClass().getName();
        String namespace = guessNamespace(screenClass);
        String title = current.getTitle() == null ? "" : current.getTitle().getString();

        OpenedScreenRegistry.record(namespace, screenClass, title);
        RetroGuiWorkbench.LOGGER.info("Logged screen: [{}] {} ({})", namespace, screenClass, title);
    }

    private static boolean isLikelyNonGameplayScreen(Screen screen) {
        String name = screen.getClass().getSimpleName().toLowerCase(Locale.ROOT);
        return name.contains("title")
                || name.contains("pause")
                || name.contains("options")
                || name.contains("advancements")
                || name.contains("chat")
                || name.contains("social")
                || name.contains("disconnect")
                || name.contains("multiplayer")
                || name.contains("worldselection")
                || name.contains("creat") && name.contains("world")
                || name.contains("language")
                || name.contains("controls")
                || name.contains("telemetry")
                || name.contains("recipebook");
    }

    private static String guessNamespace(String screenClassName) {
        String lowered = screenClassName.toLowerCase(Locale.ROOT);
        if (lowered.contains("sophisticatedbackpacks")) return "sophisticatedbackpacks";
        if (lowered.contains("sophisticatedstorage")) return "sophisticatedstorage";
        if (lowered.contains("sophisticatedcore")) return "sophisticatedcore";
        if (lowered.contains("toms_storage") || lowered.contains("tomsstorage") || lowered.contains("tom.simple")) return "toms_storage";
        if (lowered.startsWith("net.minecraft") || lowered.contains("minecraft")) return "minecraft";

        String[] parts = screenClassName.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("com") || parts[i].equals("net") || parts[i].equals("org")) {
                continue;
            }
            return parts[i].toLowerCase(Locale.ROOT);
        }
        return "unknown";
    }
}
