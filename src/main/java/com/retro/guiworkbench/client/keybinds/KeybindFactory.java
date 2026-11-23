package com.retro.guiworkbench.client.keybinds;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeybindFactory
{
    private static final String KeyCategory = "key.categories.guiworkbench";

    private KeybindFactory()
    {
    }

    public static KeybindSetData create()
    {
        return new KeybindSetData(
                new KeyMapping("key.guiworkbench.toggle_logging", GLFW.GLFW_KEY_F8, KeyCategory),
                new KeyMapping("key.guiworkbench.export_opened", GLFW.GLFW_KEY_F9, KeyCategory),
                new KeyMapping("key.guiworkbench.export_all", GLFW.GLFW_KEY_F10, KeyCategory));
    }
}
