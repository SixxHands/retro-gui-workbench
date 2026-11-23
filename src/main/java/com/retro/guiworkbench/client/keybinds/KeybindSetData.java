package com.retro.guiworkbench.client.keybinds;

import net.minecraft.client.KeyMapping;

import java.util.Objects;

public record KeybindSetData(
        KeyMapping toggleLoggingKey,
        KeyMapping exportOpenedKey,
        KeyMapping exportAllKey)
{
    public KeybindSetData
    {
        Objects.requireNonNull(toggleLoggingKey, "toggleLoggingKey");
        Objects.requireNonNull(exportOpenedKey, "exportOpenedKey");
        Objects.requireNonNull(exportAllKey, "exportAllKey");
    }
}
