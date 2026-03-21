package com.retro.guiworkbench.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.retro.guiworkbench.client.ClientWorkQueue;
import com.retro.guiworkbench.export.ExportMode;
import com.retro.guiworkbench.runtime.OpenedScreenRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "retro", bus = EventBusSubscriber.Bus.GAME)
public final class GuiWorkbenchCommands {
    private GuiWorkbenchCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("guiwb")
                .then(Commands.literal("log")
                        .then(Commands.literal("start").executes(ctx -> setLogging(ctx.getSource(), true)))
                        .then(Commands.literal("stop").executes(ctx -> setLogging(ctx.getSource(), false)))
                        .then(Commands.literal("toggle").executes(ctx -> toggleLogging(ctx.getSource()))))
                .then(Commands.literal("export")
                        .then(Commands.literal("all").executes(ctx -> queueExport(ctx.getSource(), ExportMode.ALL)))
                        .then(Commands.literal("opened").executes(ctx -> queueExport(ctx.getSource(), ExportMode.OPENED_ONLY))))
                .then(Commands.literal("scan").executes(ctx -> queueExport(ctx.getSource(), ExportMode.ALL)))
                .then(Commands.literal("clearlog").executes(ctx -> clearLog(ctx.getSource()))));
    }

    private static int setLogging(CommandSourceStack source, boolean enabled) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientWorkQueue.requestLoggingState(enabled);
            source.sendSuccess(() -> Component.literal("Queued runtime GUI logging: " + (enabled ? "enabled" : "disabled")), false);
            return 1;
        }
        source.sendFailure(Component.literal("Runtime GUI logging is a client-only feature."));
        return 0;
    }

    private static int toggleLogging(CommandSourceStack source) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientWorkQueue.requestLoggingToggle();
            source.sendSuccess(() -> Component.literal("Queued runtime GUI logging toggle."), false);
            return 1;
        }
        source.sendFailure(Component.literal("Runtime GUI logging is a client-only feature."));
        return 0;
    }

    private static int queueExport(CommandSourceStack source, ExportMode mode) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (mode == ExportMode.ALL) {
                ClientWorkQueue.requestExportAll();
                source.sendSuccess(() -> Component.literal("Queued full GUI export on the client."), false);
            } else {
                ClientWorkQueue.requestExportOpened();
                source.sendSuccess(() -> Component.literal("Queued opened-screen GUI export on the client."), false);
            }
            return 1;
        }
        source.sendFailure(Component.literal("GUI export is a client-driven feature in this starter source."));
        return 0;
    }

    private static int clearLog(CommandSourceStack source) {
        OpenedScreenRegistry.clear();
        source.sendSuccess(() -> Component.literal("Cleared logged screens."), false);
        return 1;
    }
}
