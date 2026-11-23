package com.retro.guiworkbench.export;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.ExportOptionsData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ExportBlueprintTest
{
    @Test
    void rejectsBlankOutputFolderName(@TempDir Path tempDirectory)
    {
        ExportOptionsData options = new ExportOptionsData(true, true, true, 4, true);
        assertThrows(
                IllegalArgumentException.class,
                () -> ExportBlueprint.create(tempDirectory, "   ", ExportMode.All, options, Set.of(), Set.of()));
    }

    @Test
    void rejectsOutOfRangeEditableScale(@TempDir Path tempDirectory)
    {
        ExportOptionsData tooSmall = new ExportOptionsData(true, true, true, 0, true);
        ExportOptionsData tooLarge = new ExportOptionsData(true, true, true, 17, true);

        assertThrows(
                IllegalArgumentException.class,
                () -> ExportBlueprint.create(tempDirectory, "out", ExportMode.All, tooSmall, Set.of(), Set.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> ExportBlueprint.create(tempDirectory, "out", ExportMode.All, tooLarge, Set.of(), Set.of()));
    }

    @Test
    void copiesWhitelistAndOpenedNamespacesDefensively(@TempDir Path tempDirectory)
    {
        Set<String> mutableWhitelist = new HashSet<>(Set.of("toms_storage"));
        Set<String> mutableOpened = new HashSet<>(Set.of("minecraft"));
        ExportOptionsData options = new ExportOptionsData(true, true, true, 4, true);

        ExportBlueprint blueprint = ExportBlueprint.create(
                tempDirectory,
                " export_out ",
                ExportMode.OpenedOnly,
                options,
                mutableWhitelist,
                mutableOpened);

        mutableWhitelist.add("changed");
        mutableOpened.add("changed");

        assertEquals("export_out", blueprint.outputFolderName());
        assertEquals(Set.of("toms_storage"), blueprint.whitelist());
        assertEquals(Set.of("minecraft"), blueprint.openedNamespaces());
    }
}
