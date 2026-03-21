package com.retro.guiworkbench.client;

import com.retro.guiworkbench.RetroGuiWorkbench;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.export.ExportMode;
import com.retro.guiworkbench.export.GuiAssetExporter;
import com.retro.guiworkbench.runtime.ScreenTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = RetroGuiWorkbench.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class RetroClientHooks {
    private static final String CATEGORY = "key.categories.retro_gui_workbench";

    private static final KeyMapping TOGGLE_LOGGING = new KeyMapping(
            "key.retro_gui_workbench.toggle_logging",
            GLFW.GLFW_KEY_F8,
            CATEGORY
    );

    private static final KeyMapping EXPORT_OPENED = new KeyMapping(
            "key.retro_gui_workbench.export_opened",
            GLFW.GLFW_KEY_F9,
            CATEGORY
    );

    private static final KeyMapping EXPORT_ALL = new KeyMapping(
            "key.retro_gui_workbench.export_all",
            GLFW.GLFW_KEY_F10,
            CATEGORY
    );

    private static boolean startupProcessed = false;

    private RetroClientHooks() {}


    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_LOGGING);
        event.register(EXPORT_OPENED);
        event.register(EXPORT_ALL);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        if (!startupProcessed) {
            startupProcessed = true;
            ScreenTracker.bootstrapFromConfig();
            if (GuiWorkbenchConfig.EXPORT_ON_STARTUP.get()) {
                ClientWorkQueue.requestExportAll();
            }
        }

        if (minecraft.player == null) {
            return;
        }

        handleKeybinds(minecraft);
        handleQueuedWork(minecraft);
        ScreenTracker.tick(minecraft);
    }

    private static void handleKeybinds(Minecraft minecraft) {
        while (TOGGLE_LOGGING.consumeClick()) {
            ScreenTracker.toggleRuntimeLogging();
            message(minecraft, "Runtime GUI logging " + (ScreenTracker.isRuntimeLoggingEnabled() ? "enabled" : "disabled"));
        }

        while (EXPORT_OPENED.consumeClick()) {
            ClientWorkQueue.requestExportOpened();
        }

        while (EXPORT_ALL.consumeClick()) {
            ClientWorkQueue.requestExportAll();
        }
    }

    private static void handleQueuedWork(Minecraft minecraft) {
        Boolean requestedState = ClientWorkQueue.consumeRequestedLoggingState();
        if (requestedState != null) {
            ScreenTracker.setRuntimeLoggingEnabled(requestedState);
            message(minecraft, "Runtime GUI logging " + (requestedState ? "enabled" : "disabled"));
        }

        if (ClientWorkQueue.consumeLoggingToggle()) {
            ScreenTracker.toggleRuntimeLogging();
            message(minecraft, "Runtime GUI logging " + (ScreenTracker.isRuntimeLoggingEnabled() ? "enabled" : "disabled"));
        }

        if (ClientWorkQueue.consumeExportAll()) {
            message(minecraft, "Exporting GUI assets (all configured targets)...");
            message(minecraft, "Output: " + GuiAssetExporter.export(minecraft, ExportMode.ALL));
        }

        if (ClientWorkQueue.consumeExportOpened()) {
            message(minecraft, "Exporting GUI assets for opened/logged screens...");
            message(minecraft, "Output: " + GuiAssetExporter.export(minecraft, ExportMode.OPENED_ONLY));
        }
    }

    private static void message(Minecraft minecraft, String message) {
        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(Component.literal(message), false);
        }
        RetroGuiWorkbench.LOGGER.info(message);
    }
}
