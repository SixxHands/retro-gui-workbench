package com.retro.guiworkbench.export.sources;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TextureSourceFactory
{
    private TextureSourceFactory()
    {
    }

    public static List<ITextureSource> create(Minecraft minecraft, ExportBlueprint blueprint)
    {
        Objects.requireNonNull(blueprint, "blueprint");
        List<ITextureSource> sources = new ArrayList<>();
        if (blueprint.options().includeVanilla())
        {
            sources.add(new VanillaTextureSource(minecraft));
        }

        if (blueprint.options().includeMods())
        {
            sources.add(createModJarSource(blueprint.gameDirectory().resolve("mods")));
        }

        return List.copyOf(sources);
    }

    public static ITextureSource createModJarSource(Path modsDirectory)
    {
        Objects.requireNonNull(modsDirectory, "modsDirectory");
        return new ModJarTextureSource(modsDirectory);
    }
}
