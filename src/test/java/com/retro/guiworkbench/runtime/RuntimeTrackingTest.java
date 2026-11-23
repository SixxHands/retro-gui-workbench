package com.retro.guiworkbench.runtime;

import com.retro.guiworkbench.client.ClientWorkQueue;
import com.retro.guiworkbench.client.ClientWorkQueueFactory;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;
import com.retro.guiworkbench.runtime.tracking.ScreenTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RuntimeTrackingTest
{
    @Test
    void recordsAndPersistsOpenedScreensAcrossSaveAndLoad(@TempDir Path tempDirectory)
    {
        IScreenRegistry registry = OpenedScreenRegistryFactory.create();
        registry.record("minecraft", "net.minecraft.ChestScreen", "Chest");
        registry.record("minecraft", "net.minecraft.ChestScreen", "IgnoredSecondTitle");

        Path jsonFile = tempDirectory.resolve("screens_opened.json");
        registry.save(jsonFile);

        IScreenRegistry reloaded = OpenedScreenRegistryFactory.create();
        reloaded.load(jsonFile);

        assertEquals(Set.of("minecraft"), reloaded.namespaces());
        LoggedScreenData entry = reloaded.records().iterator().next();
        assertEquals(2, entry.openCount());
        assertEquals("Chest", entry.title());
    }

    @Test
    void filtersNonGameplayScreensAndFindsKnownNamespaces()
    {
        assertTrue(ScreenTracker.isNonGameplayScreenName("TitleScreen"));
        assertTrue(ScreenTracker.isNonGameplayScreenName("CreateWorldScreen"));
        assertFalse(ScreenTracker.isNonGameplayScreenName("BackpackScreen"));

        assertEquals("sophisticatedbackpacks", ScreenTracker.findNamespace("net.p3pp3rf1y.sophisticatedbackpacks.Screen"));
        assertEquals("toms_storage", ScreenTracker.findNamespace("com.tom.simple.StorageTerminalScreen"));
        assertEquals("minecraft", ScreenTracker.findNamespace("net.minecraft.client.gui.screens.inventory.FurnaceScreen"));
        assertEquals("custommod", ScreenTracker.findNamespace("com.custommod.gui.MachineScreen"));
    }

    @Test
    void consumesQueuedClientRequestsExactlyOnce()
    {
        ClientWorkQueue workQueue = ClientWorkQueueFactory.create();
        workQueue.requestExportAll();
        workQueue.requestExportOpened();
        workQueue.requestLoggingState(true);
        workQueue.requestLoggingToggle();

        assertTrue(workQueue.consumeExportAll());
        assertFalse(workQueue.consumeExportAll());

        assertTrue(workQueue.consumeExportOpened());
        assertFalse(workQueue.consumeExportOpened());

        assertEquals(Optional.of(true), workQueue.consumeRequestedLoggingState());
        assertEquals(Optional.empty(), workQueue.consumeRequestedLoggingState());

        assertTrue(workQueue.consumeLoggingToggle());
        assertFalse(workQueue.consumeLoggingToggle());
    }

    @Test
    void translatesCorruptScreenRegistryJsonWithPathAndCause(@TempDir Path tempDirectory) throws IOException
    {
        Path corruptFile = tempDirectory.resolve("screens_opened.json");
        Files.writeString(corruptFile, "{not valid json");
        IScreenRegistry registry = OpenedScreenRegistryFactory.create();

        GuiWorkbenchException error = assertThrows(
                GuiWorkbenchException.class,
                () -> registry.load(corruptFile));

        assertTrue(error.getMessage().contains(corruptFile.toString()));
        assertNotNull(error.getCause());
    }
}
