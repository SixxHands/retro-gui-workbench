package com.retro.guiworkbench;

import com.mojang.logging.LogUtils;
import com.retro.guiworkbench.client.ClientWorkQueue;
import com.retro.guiworkbench.client.ClientWorkQueueFactory;
import com.retro.guiworkbench.client.GuiWorkbenchCommands;
import com.retro.guiworkbench.client.RetroClientHooks;
import com.retro.guiworkbench.client.keybinds.KeybindFactory;
import com.retro.guiworkbench.client.keybinds.KeybindSetData;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import com.retro.guiworkbench.runtime.OpenedScreenRegistryFactory;
import com.retro.guiworkbench.runtime.tracking.ScreenTracker;
import com.retro.guiworkbench.runtime.tracking.ScreenTrackerFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(RetroGuiWorkbench.ModId)
public final class RetroGuiWorkbench
{
    public static final String ModId = "guiworkbench";
    public static final Logger Logger = LogUtils.getLogger();

    public RetroGuiWorkbench(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, GuiWorkbenchConfig.Settings);

        ClientWorkQueue workQueue = ClientWorkQueueFactory.create();
        IScreenRegistry screenRegistry = OpenedScreenRegistryFactory.create();
        GuiWorkbenchCommands commands = new GuiWorkbenchCommands(workQueue, screenRegistry);
        NeoForge.EVENT_BUS.addListener(commands::onRegisterCommands);

        // Keybinds live on the mod bus and ticks live on the game bus. Keep both behind Dist.CLIENT so dedicated servers never touch client classes.
        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            KeybindSetData keybinds = KeybindFactory.create();
            ScreenTracker screenTracker = ScreenTrackerFactory.create(screenRegistry);
            RetroClientHooks clientHooks = new RetroClientHooks(keybinds, workQueue, screenRegistry, screenTracker);
            modEventBus.addListener(clientHooks::registerKeys);
            NeoForge.EVENT_BUS.addListener(clientHooks::onClientTick);
        }

        Logger.info("Retro GUI Workbench initialized.");
    }
}
