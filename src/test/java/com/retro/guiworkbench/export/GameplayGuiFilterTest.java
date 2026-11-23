package com.retro.guiworkbench.export;

import com.retro.guiworkbench.export.sources.GameplayGuiFilter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GameplayGuiFilterTest
{
    @Test
    void acceptsGameplayContainerAndStorageTextures()
    {
        assertTrue(GameplayGuiFilter.isGameplayGuiPath("textures/gui/container/furnace.png"));
        assertTrue(GameplayGuiFilter.isGameplayGuiPath("textures/gui/backpack_inventory.png"));
        assertTrue(GameplayGuiFilter.isGameplayGuiPath("textures/gui/storage_terminal.png"));
    }

    @Test
    void rejectsTitleOptionsAndNonGuiTextures()
    {
        assertFalse(GameplayGuiFilter.isGameplayGuiPath("textures/gui/title/minecraft.png"));
        assertFalse(GameplayGuiFilter.isGameplayGuiPath("textures/gui/options_background.png"));
        assertFalse(GameplayGuiFilter.isGameplayGuiPath("textures/gui/realms/icon.png"));
        assertFalse(GameplayGuiFilter.isGameplayGuiPath("textures/block/stone.png"));
        assertFalse(GameplayGuiFilter.isGameplayGuiPath(""));
    }

    @Test
    void cleansWhitelistByLowercasingAndSkippingBlanks()
    {
        List<String> raw = Arrays.asList(" SophisticatedBackpacks ", "", null, "TOMS_STORAGE", "toms_storage");
        Set<String> cleaned = GameplayGuiFilter.cleanWhitelist(raw);

        assertEquals(Set.of("sophisticatedbackpacks", "toms_storage"), cleaned);
    }
}
