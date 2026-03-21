package com.retro.guiworkbench;

import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(RetroGuiWorkbench.MODID)
public final class RetroGuiWorkbench {
    public static final String MODID = "retro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RetroGuiWorkbench(IEventBus modEventBus, ModContainer modContainer) {
        GuiWorkbenchConfig.init();
        modContainer.registerConfig(ModConfig.Type.COMMON, GuiWorkbenchConfig.SPEC);
        LOGGER.info("Retro GUI Workbench initialized.");
    }
}
