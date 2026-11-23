package com.retro.guiworkbench.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class GuiWorkbenchConfig
{
    public static final ModConfigSpec Settings;

    public static final ModConfigSpec.BooleanValue EnableStaticScan;
    public static final ModConfigSpec.BooleanValue EnableRuntimeLogging;
    public static final ModConfigSpec.BooleanValue ExportOnStartup;
    public static final ModConfigSpec.BooleanValue ExportOnlyOpenedScreens;
    public static final ModConfigSpec.BooleanValue GenerateEditableCopies;
    public static final ModConfigSpec.IntValue EditableScale;
    public static final ModConfigSpec.BooleanValue GenerateGuides;
    public static final ModConfigSpec.BooleanValue IncludeVanilla;
    public static final ModConfigSpec.BooleanValue IncludeMods;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ModWhitelist;
    public static final ModConfigSpec.BooleanValue IgnoreNonContainerScreens;
    public static final ModConfigSpec.ConfigValue<String> OutputFolderName;

    static
    {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("passes");
        EnableStaticScan = builder
                .comment("Enables static jar/resource scanning. Heavy; usually best used on demand.")
                .define("enableStaticScan", true);
        EnableRuntimeLogging = builder
                .comment("Tracks newly opened gameplay screens during play.")
                .define("enableRuntimeLogging", false);
        builder.pop();

        builder.push("export");
        ExportOnStartup = builder
                .comment("Runs an export automatically after the client finishes booting.")
                .define("exportOnStartup", false);
        ExportOnlyOpenedScreens = builder
                .comment("When true, export commands default to only namespaces/screens logged during play.")
                .define("exportOnlyOpenedScreens", false);
        OutputFolderName = builder
                .comment("Folder created in the Minecraft game directory for generated resource-pack output.")
                .define("outputFolderName", "retro_gui_workbench_export");
        builder.pop();

        builder.push("output");
        GenerateEditableCopies = builder
                .comment("Create 2x/4x nearest-neighbor work copies under _editable.")
                .define("generateEditableCopies", true);
        EditableScale = builder
                .comment("Nearest-neighbor upscale factor for work copies.")
                .defineInRange("editableScale", 4, 1, 16);
        GenerateGuides = builder
                .comment("Generate lightweight guide overlays for editing.")
                .define("generateGuides", true);
        builder.pop();

        builder.push("filters");
        IncludeVanilla = builder
                .comment("Include vanilla gameplay GUI textures from minecraft:textures/gui.")
                .define("includeVanilla", true);
        IncludeMods = builder
                .comment("Include GUI textures found inside jars in the mods folder.")
                .define("includeMods", true);
        ModWhitelist = builder
                .comment("Only these namespaces are exported from jars when the list is non-empty.")
                .defineListAllowEmpty(
                        List.of("modWhitelist"),
                        () -> List.of(
                                "sophisticatedbackpacks",
                                "sophisticatedstorage",
                                "sophisticatedcore",
                                "toms_storage"),
                        () -> "",
                        value -> value instanceof String text && !text.isBlank());
        IgnoreNonContainerScreens = builder
                .comment("Runtime logging ignores obviously non-gameplay screens where possible.")
                .define("ignoreNonContainerScreens", true);
        builder.pop();

        Settings = builder.build();
    }

    private GuiWorkbenchConfig()
    {
    }
}
