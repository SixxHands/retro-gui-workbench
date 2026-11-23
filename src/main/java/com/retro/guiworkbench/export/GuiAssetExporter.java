package com.retro.guiworkbench.export;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportedTextureData;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;
import com.retro.guiworkbench.export.output.PackManifestWriter;
import com.retro.guiworkbench.export.sources.ITextureSource;
import com.retro.guiworkbench.runtime.IScreenRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GuiAssetExporter
{
    private static final String PackFolderName = "GuiWorkbenchPack";

    private final ExportBlueprint _blueprint;
    private final List<ITextureSource> _sources;
    private final IScreenRegistry _screenRegistry;

    GuiAssetExporter(ExportBlueprint blueprint, List<ITextureSource> sources, IScreenRegistry screenRegistry)
    {
        _blueprint = Objects.requireNonNull(blueprint, "blueprint");
        _sources = List.copyOf(sources);
        _screenRegistry = Objects.requireNonNull(screenRegistry, "screenRegistry");
    }

    public Path export()
    {
        Path outputRoot = _blueprint.gameDirectory().resolve(_blueprint.outputFolderName());
        Path packRoot = outputRoot.resolve(PackFolderName);
        Path manifestRoot = packRoot.resolve("_manifest");
        Path editableRoot = packRoot.resolve("_editable");
        List<ExportedTextureData> manifest = new ArrayList<>();

        try
        {
            Files.createDirectories(packRoot);
            Files.createDirectories(manifestRoot);
            Files.createDirectories(editableRoot);
            PackManifestWriter.writePackMcmeta(packRoot.resolve("pack.mcmeta"));

            for (ITextureSource source : _sources)
            {
                source.collect(_blueprint, packRoot, editableRoot, manifest);
            }

            _screenRegistry.save(manifestRoot.resolve("screens_opened.json"));
            PackManifestWriter.writeTexturesManifest(manifestRoot.resolve("textures.json"), manifest);
            PackManifestWriter.writeReport(manifestRoot.resolve("export_report.txt"), _blueprint, manifest.size());
        }
        catch (IOException exception)
        {
            throw new GuiWorkbenchException("GUI export failed for " + packRoot, exception);
        }

        return packRoot;
    }
}
