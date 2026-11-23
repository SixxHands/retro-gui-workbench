package com.retro.guiworkbench.client;

import com.retro.guiworkbench.RetroGuiWorkbench;
import com.retro.guiworkbench.client.keybinds.KeybindSetData;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.export.GuiAssetExporter;
import com.retro.guiworkbench.export.GuiAssetExporterFactory;
import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import com.retro.guiworkbench.runtime.tracking.ScreenTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class RetroClientHooks
{
    private final KeybindSetData _keybinds;
    private final ClientWorkQueue _workQueue;
    private final IScreenRegistry _screenRegistry;
    private final ScreenTracker _screenTracker;
    private boolean _startupProcessed;

    public RetroClientHooks(
            KeybindSetData keybinds,
            ClientWorkQueue workQueue,
            IScreenRegistry screenRegistry,
            ScreenTracker screenTracker)
    {
        _keybinds = Objects.requireNonNull(keybinds, "keybinds");
        _workQueue = Objects.requireNonNull(workQueue, "workQueue");
        _screenRegistry = Objects.requireNonNull(screenRegistry, "screenRegistry");
        _screenTracker = Objects.requireNonNull(screenTracker, "screenTracker");
    }

    public void registerKeys(RegisterKeyMappingsEvent event)
    {
        event.register(_keybinds.toggleLoggingKey());
        event.register(_keybinds.exportOpenedKey());
        event.register(_keybinds.exportAllKey());
    }

    public void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null)
        {
            return;
        }

        if (!_startupProcessed)
        {
            _startupProcessed = true;
            _screenTracker.startFromConfig();
            if (GuiWorkbenchConfig.ExportOnStartup.get())
            {
                _workQueue.requestExportAll();
            }
        }

        if (minecraft.player == null)
        {
            return;
        }

        processKeybinds(minecraft);
        processQueuedWork(minecraft);
        _screenTracker.tick(minecraft);
    }

    private void processKeybinds(Minecraft minecraft)
    {
        while (_keybinds.toggleLoggingKey().consumeClick())
        {
            _screenTracker.toggleRuntimeLogging();
            sendClientMessage(
                    minecraft,
                    "Runtime GUI logging " + (_screenTracker.isRuntimeLoggingEnabled() ? "enabled" : "disabled"));
        }

        while (_keybinds.exportOpenedKey().consumeClick())
        {
            _workQueue.requestExportOpened();
        }

        while (_keybinds.exportAllKey().consumeClick())
        {
            _workQueue.requestExportAll();
        }
    }

    private void processQueuedWork(Minecraft minecraft)
    {
        Optional<Boolean> requestedState = _workQueue.consumeRequestedLoggingState();
        if (requestedState.isPresent())
        {
            boolean enabled = requestedState.get();
            _screenTracker.setRuntimeLoggingEnabled(enabled);
            sendClientMessage(minecraft, "Runtime GUI logging " + (enabled ? "enabled" : "disabled"));
        }

        if (_workQueue.consumeLoggingToggle())
        {
            _screenTracker.toggleRuntimeLogging();
            sendClientMessage(
                    minecraft,
                    "Runtime GUI logging " + (_screenTracker.isRuntimeLoggingEnabled() ? "enabled" : "disabled"));
        }

        if (_workQueue.consumeExportAll())
        {
            runExport(minecraft, ExportMode.All, "Exporting GUI assets (all configured targets)...");
        }

        if (_workQueue.consumeExportOpened())
        {
            runExport(minecraft, ExportMode.OpenedOnly, "Exporting GUI assets for opened/logged screens...");
        }
    }

    // Catch export failures here so one broken jar or locked folder prints in chat instead of killing the client.
    private void runExport(Minecraft minecraft, ExportMode mode, String startMessage)
    {
        sendClientMessage(minecraft, startMessage);
        try
        {
            ExportBlueprint blueprint = GuiAssetExporterFactory.createBlueprintFromConfig(mode, _screenRegistry);
            GuiAssetExporter exporter = GuiAssetExporterFactory.createForClient(minecraft, blueprint, _screenRegistry);
            Path outputPath = exporter.export();
            sendClientMessage(minecraft, "Output: " + outputPath);
        }
        catch (GuiWorkbenchException exception)
        {
            RetroGuiWorkbench.Logger.error("GUI export failed.", exception);
            sendClientMessage(minecraft, "GUI export failed: " + exception.getMessage());
        }
    }

    private static void sendClientMessage(Minecraft minecraft, String text)
    {
        if (minecraft.player != null)
        {
            minecraft.player.displayClientMessage(Component.literal(text), false);
        }

        RetroGuiWorkbench.Logger.info(text);
    }
}
