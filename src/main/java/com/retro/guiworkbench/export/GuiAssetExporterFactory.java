package com.retro.guiworkbench.export;

import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportMode;
import com.retro.guiworkbench.export.model.ExportOptionsData;
import com.retro.guiworkbench.export.sources.GameplayGuiFilter;
import com.retro.guiworkbench.export.sources.ITextureSource;
import com.retro.guiworkbench.export.sources.TextureSourceFactory;
import com.retro.guiworkbench.runtime.IScreenRegistry;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;

import java.util.List;

public final class GuiAssetExporterFactory
{
    private GuiAssetExporterFactory()
    {
    }

    public static ExportBlueprint createBlueprintFromConfig(ExportMode mode, IScreenRegistry screenRegistry)
    {
        ExportOptionsData options = new ExportOptionsData(
                GuiWorkbenchConfig.IncludeVanilla.get(),
                GuiWorkbenchConfig.IncludeMods.get(),
                GuiWorkbenchConfig.GenerateEditableCopies.get(),
                GuiWorkbenchConfig.EditableScale.get(),
                GuiWorkbenchConfig.GenerateGuides.get());

        return ExportBlueprint.create(
                FMLPaths.GAMEDIR.get(),
                GuiWorkbenchConfig.OutputFolderName.get(),
                mode,
                options,
                GameplayGuiFilter.cleanWhitelist(GuiWorkbenchConfig.ModWhitelist.get()),
                screenRegistry.namespaces());
    }

    public static GuiAssetExporter createForClient(
            Minecraft minecraft,
            ExportBlueprint blueprint,
            IScreenRegistry screenRegistry)
    {
        return new GuiAssetExporter(blueprint, TextureSourceFactory.create(minecraft, blueprint), screenRegistry);
    }

    public static GuiAssetExporter create(
            ExportBlueprint blueprint,
            List<ITextureSource> sources,
            IScreenRegistry screenRegistry)
    {
        return new GuiAssetExporter(blueprint, sources, screenRegistry);
    }
}
