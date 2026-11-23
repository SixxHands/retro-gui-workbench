package com.retro.guiworkbench.export.sources;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.ExportedTextureData;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;
import com.retro.guiworkbench.export.output.TextureFileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class ModJarTextureSource implements ITextureSource
{
    private final Path _modsDirectory;

    ModJarTextureSource(Path modsDirectory)
    {
        _modsDirectory = modsDirectory;
    }

    @Override
    public void collect(
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest) throws IOException
    {
        if (!Files.isDirectory(_modsDirectory))
        {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(_modsDirectory, "*.jar"))
        {
            for (Path jarPath : stream)
            {
                exportJar(jarPath, blueprint, packRoot, editableRoot, manifest);
            }
        }
    }

    private static void exportJar(
            Path jarPath,
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest)
    {
        try (ZipFile zip = new ZipFile(jarPath.toFile()))
        {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                exportEntryIfMatching(zip, entry, jarPath, blueprint, packRoot, editableRoot, manifest);
            }
        }
        catch (IOException exception)
        {
            throw new GuiWorkbenchException("Failed reading mod jar " + jarPath, exception);
        }
    }

    private static void exportEntryIfMatching(
            ZipFile zip,
            ZipEntry entry,
            Path jarPath,
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest) throws IOException
    {
        if (entry.isDirectory())
        {
            return;
        }

        String name = entry.getName();
        if (!name.startsWith("assets/") || !name.endsWith(".png"))
        {
            return;
        }

        String[] parts = name.split("/", 4);
        if (parts.length < 4)
        {
            return;
        }

        String namespace = parts[1].toLowerCase(Locale.ROOT);
        String resourcePath = parts[2] + "/" + parts[3];
        if (!shouldExport(namespace, resourcePath, blueprint))
        {
            return;
        }

        // Read the PNG once so we can write the raw texture and feed the upscaler without re-opening the zip entry.
        byte[] bytes;
        try (InputStream stream = zip.getInputStream(entry))
        {
            bytes = stream.readAllBytes();
        }

        Path target = TextureFileWriter.writeTexture(
                bytes,
                namespace,
                resourcePath,
                packRoot,
                editableRoot,
                blueprint.options());

        manifest.add(new ExportedTextureData(
                namespace,
                resourcePath,
                jarPath.getFileName().toString(),
                target.toString()));
    }

    private static boolean shouldExport(String namespace, String resourcePath, ExportBlueprint blueprint)
    {
        if (!GameplayGuiFilter.isGameplayGuiPath(resourcePath))
        {
            return false;
        }

        if (!blueprint.whitelist().isEmpty() && !blueprint.whitelist().contains(namespace))
        {
            return false;
        }

        return blueprint.mode() != ExportMode.OpenedOnly || blueprint.openedNamespaces().contains(namespace);
    }
}
