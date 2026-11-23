package com.retro.guiworkbench.export;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.ExportOptionsData;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;
import com.retro.guiworkbench.export.sources.TextureSourceFactory;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import com.retro.guiworkbench.runtime.OpenedScreenRegistryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModJarTextureSourceTest
{
    @Test
    void exportsWhitelistedGameplayGuiFromJarAndSkipsExcludedTextures(@TempDir Path tempDirectory) throws IOException
    {
        IScreenRegistry screenRegistry = OpenedScreenRegistryFactory.create();
        Path modsDirectory = tempDirectory.resolve("mods");
        Files.createDirectories(modsDirectory);
        writeSampleModJar(modsDirectory.resolve("sample-mod.jar"));

        ExportOptionsData options = new ExportOptionsData(false, true, true, 2, true);
        ExportBlueprint blueprint = ExportBlueprint.create(
                tempDirectory,
                "export_out",
                ExportMode.All,
                options,
                Set.of("toms_storage"),
                Set.of());

        GuiAssetExporter exporter = GuiAssetExporterFactory.create(
                blueprint,
                List.of(TextureSourceFactory.createModJarSource(modsDirectory)),
                screenRegistry);
        Path packRoot = exporter.export();

        assertTrue(Files.exists(packRoot.resolve("pack.mcmeta")));
        assertTrue(Files.exists(packRoot.resolve("assets/toms_storage/textures/gui/terminal.png")));
        assertTrue(Files.exists(packRoot.resolve("_editable/toms_storage/terminal__x2.png")));
        assertTrue(Files.exists(packRoot.resolve("_editable/toms_storage/terminal__x2__guide.png")));
        assertTrue(Files.exists(packRoot.resolve("_manifest/textures.json")));
        assertTrue(Files.exists(packRoot.resolve("_manifest/export_report.txt")));

        assertFalse(Files.exists(packRoot.resolve("assets/toms_storage/textures/gui/title_logo.png")));
        assertFalse(Files.exists(packRoot.resolve("assets/other_mod/textures/gui/container.png")));
    }

    @Test
    void exportsOnlyOpenedNamespacesWhenModeIsOpenedOnly(@TempDir Path tempDirectory) throws IOException
    {
        IScreenRegistry screenRegistry = OpenedScreenRegistryFactory.create();
        Path modsDirectory = tempDirectory.resolve("mods");
        Files.createDirectories(modsDirectory);
        writeSampleModJar(modsDirectory.resolve("sample-mod.jar"));

        ExportOptionsData options = new ExportOptionsData(false, true, false, 2, false);
        ExportBlueprint blueprint = ExportBlueprint.create(
                tempDirectory,
                "opened_out",
                ExportMode.OpenedOnly,
                options,
                Set.of(),
                Set.of("other_mod"));

        GuiAssetExporter exporter = GuiAssetExporterFactory.create(
                blueprint,
                List.of(TextureSourceFactory.createModJarSource(modsDirectory)),
                screenRegistry);
        Path packRoot = exporter.export();

        assertTrue(Files.exists(packRoot.resolve("assets/other_mod/textures/gui/container.png")));
        assertFalse(Files.exists(packRoot.resolve("assets/toms_storage/textures/gui/terminal.png")));
    }

    @Test
    void translatesCorruptModJarIntoGuiWorkbenchExceptionWithPathAndCause(@TempDir Path tempDirectory) throws IOException
    {
        IScreenRegistry screenRegistry = OpenedScreenRegistryFactory.create();
        Path modsDirectory = tempDirectory.resolve("mods");
        Files.createDirectories(modsDirectory);
        Path corruptJar = modsDirectory.resolve("broken-mod.jar");
        Files.writeString(corruptJar, "not a zip archive");

        ExportOptionsData options = new ExportOptionsData(false, true, false, 2, false);
        ExportBlueprint blueprint = ExportBlueprint.create(
                tempDirectory,
                "broken_out",
                ExportMode.All,
                options,
                Set.of(),
                Set.of());

        GuiAssetExporter exporter = GuiAssetExporterFactory.create(
                blueprint,
                List.of(TextureSourceFactory.createModJarSource(modsDirectory)),
                screenRegistry);

        GuiWorkbenchException error = assertThrows(GuiWorkbenchException.class, exporter::export);
        assertTrue(error.getMessage().contains(corruptJar.toString()));
        assertNotNull(error.getCause());
    }

    private static void writeSampleModJar(Path jarPath) throws IOException
    {
        byte[] png = createTinyPng();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jarPath)))
        {
            addEntry(zip, "assets/toms_storage/textures/gui/terminal.png", png);
            addEntry(zip, "assets/toms_storage/textures/gui/title_logo.png", png);
            addEntry(zip, "assets/other_mod/textures/gui/container.png", png);
        }
    }

    private static void addEntry(ZipOutputStream zip, String name, byte[] bytes) throws IOException
    {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
    }

    private static byte[] createTinyPng() throws IOException
    {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
