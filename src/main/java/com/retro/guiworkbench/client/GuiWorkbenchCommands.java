package com.retro.guiworkbench.client;

import com.mojang.brigadier.CommandDispatcher;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Objects;

public final class GuiWorkbenchCommands
{
    private final ClientWorkQueue _workQueue;
    private final IScreenRegistry _screenRegistry;

    public GuiWorkbenchCommands(ClientWorkQueue workQueue, IScreenRegistry screenRegistry)
    {
        _workQueue = Objects.requireNonNull(workQueue, "workQueue");
        _screenRegistry = Objects.requireNonNull(screenRegistry, "screenRegistry");
    }

    public void onRegisterCommands(RegisterCommandsEvent event)
    {
        register(event.getDispatcher());
    }

    private void register(CommandDispatcher<CommandSourceStack> commandRunner)
    {
        commandRunner.register(Commands.literal("guiwb")
                .then(Commands.literal("log")
                        .then(Commands.literal("start").executes(context -> setLogging(context.getSource(), true)))
                        .then(Commands.literal("stop").executes(context -> setLogging(context.getSource(), false)))
                        .then(Commands.literal("toggle").executes(context -> toggleLogging(context.getSource()))))
                .then(Commands.literal("export")
                        .then(Commands.literal("all").executes(context -> queueExport(context.getSource(), ExportMode.All)))
                        .then(Commands.literal("opened").executes(context -> queueExport(context.getSource(), ExportMode.OpenedOnly))))
                .then(Commands.literal("scan").executes(context -> queueExport(context.getSource(), ExportMode.All)))
                .then(Commands.literal("clearlog").executes(context -> clearLog(context.getSource()))));
    }

    private int setLogging(CommandSourceStack source, boolean enabled)
    {
        if (FMLEnvironment.dist != Dist.CLIENT)
        {
            source.sendFailure(Component.literal("Runtime GUI logging is a client-only feature."));
            return 0;
        }

        _workQueue.requestLoggingState(enabled);
        source.sendSuccess(
                () -> Component.literal("Queued runtime GUI logging: " + (enabled ? "enabled" : "disabled")),
                false);
        return 1;
    }

    private int toggleLogging(CommandSourceStack source)
    {
        if (FMLEnvironment.dist != Dist.CLIENT)
        {
            source.sendFailure(Component.literal("Runtime GUI logging is a client-only feature."));
            return 0;
        }

        _workQueue.requestLoggingToggle();
        source.sendSuccess(() -> Component.literal("Queued runtime GUI logging toggle."), false);
        return 1;
    }

    private int queueExport(CommandSourceStack source, ExportMode mode)
    {
        if (FMLEnvironment.dist != Dist.CLIENT)
        {
            source.sendFailure(Component.literal("GUI export can only run on the client."));
            return 0;
        }

        if (mode == ExportMode.All)
        {
            _workQueue.requestExportAll();
            source.sendSuccess(() -> Component.literal("Queued full GUI export on the client."), false);
            return 1;
        }

        _workQueue.requestExportOpened();
        source.sendSuccess(() -> Component.literal("Queued opened-screen GUI export on the client."), false);
        return 1;
    }

    private int clearLog(CommandSourceStack source)
    {
        _screenRegistry.clear();
        source.sendSuccess(() -> Component.literal("Cleared logged screens."), false);
        return 1;
    }
}
