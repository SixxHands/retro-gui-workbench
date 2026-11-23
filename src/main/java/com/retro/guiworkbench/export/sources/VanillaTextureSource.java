package com.retro.guiworkbench.export.sources;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.ExportedTextureData;
import com.retro.guiworkbench.export.output.TextureFileWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class VanillaTextureSource implements ITextureSource
{
    private final Minecraft _minecraft;

    VanillaTextureSource(Minecraft minecraft)
    {
        _minecraft = minecraft;
    }

    @Override
    public void collect(
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest) throws IOException
    {
        if (_minecraft == null || _minecraft.getResourceManager() == null)
        {
            return;
        }

        if (blueprint.mode() == ExportMode.OpenedOnly && !blueprint.openedNamespaces().contains("minecraft"))
        {
            return;
        }

        Predicate<ResourceLocation> filter = location -> location.getNamespace().equals("minecraft")
                && location.getPath().startsWith("textures/gui")
                && location.getPath().endsWith(".png")
                && GameplayGuiFilter.isGameplayGuiPath(location.getPath());

        Map<ResourceLocation, Resource> resources = _minecraft.getResourceManager().listResources("textures/gui", filter);
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet())
        {
            exportSingle(entry.getKey(), entry.getValue(), blueprint, packRoot, editableRoot, manifest);
        }
    }

    private static void exportSingle(
            ResourceLocation location,
            Resource resource,
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest) throws IOException
    {
        byte[] bytes;
        try (InputStream stream = resource.open())
        {
            bytes = stream.readAllBytes();
        }

        Path target = TextureFileWriter.writeTexture(
                bytes,
                location.getNamespace(),
                location.getPath(),
                packRoot,
                editableRoot,
                blueprint.options());

        manifest.add(new ExportedTextureData(
                location.getNamespace(),
                location.getPath(),
                "resource_manager",
                target.toString()));
    }
}
