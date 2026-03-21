package com.retro.guiworkbench.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class GuiWorkbenchConfig {
    public static final ModConfigSpec SPEC;

    public static ModConfigSpec.BooleanValue ENABLE_STATIC_SCAN;
    public static ModConfigSpec.BooleanValue ENABLE_RUNTIME_LOGGING;
    public static ModConfigSpec.BooleanValue EXPORT_ON_STARTUP;
    public static ModConfigSpec.BooleanValue EXPORT_ONLY_OPENED_SCREENS;
    public static ModConfigSpec.BooleanValue GENERATE_EDITABLE_COPIES;
    public static ModConfigSpec.IntValue EDITABLE_SCALE;
    public static ModConfigSpec.BooleanValue GENERATE_GUIDES;
    public static ModConfigSpec.BooleanValue INCLUDE_VANILLA;
    public static ModConfigSpec.BooleanValue INCLUDE_MODS;
    public static ModConfigSpec.ConfigValue<List<? extends String>> MOD_WHITELIST;
    public static ModConfigSpec.BooleanValue IGNORE_NON_CONTAINER_SCREENS;
    public static ModConfigSpec.ConfigValue<String> OUTPUT_FOLDER_NAME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("passes");
        ENABLE_STATIC_SCAN = builder
                .comment("Enables static jar/resource scanning. Heavy; usually best used on demand.")
                .define("enableStaticScan", true);
        ENABLE_RUNTIME_LOGGING = builder
                .comment("Tracks newly opened gameplay screens during play.")
                .define("enableRuntimeLogging", false);
        builder.pop();

        builder.push("export");
        EXPORT_ON_STARTUP = builder
                .comment("Runs an export automatically after the client finishes booting.")
                .define("exportOnStartup", false);
        EXPORT_ONLY_OPENED_SCREENS = builder
                .comment("When true, export commands default to only namespaces/screens logged during play.")
                .define("exportOnlyOpenedScreens", false);
        OUTPUT_FOLDER_NAME = builder
                .comment("Folder created in the Minecraft game directory for generated resource-pack output.")
                .define("outputFolderName", "retro_gui_workbench_export");
        builder.pop();

        builder.push("output");
        GENERATE_EDITABLE_COPIES = builder
                .comment("Create 2x/4x nearest-neighbor work copies under _editable.")
                .define("generateEditableCopies", true);
        EDITABLE_SCALE = builder
                .comment("Nearest-neighbor upscale factor for work copies.")
                .defineInRange("editableScale", 4, 1, 16);
        GENERATE_GUIDES = builder
                .comment("Generate lightweight guide overlays for editing.")
                .define("generateGuides", true);
        builder.pop();

        builder.push("filters");
        INCLUDE_VANILLA = builder
                .comment("Include vanilla gameplay GUI textures from minecraft:textures/gui.")
                .define("includeVanilla", true);
        INCLUDE_MODS = builder
                .comment("Include GUI textures found inside jars in the mods folder.")
                .define("includeMods", true);
        MOD_WHITELIST = builder
                .comment("Only these namespaces are exported from jars when the list is non-empty.")
                .defineList("modWhitelist",
                        List.of("sophisticatedbackpacks", "sophisticatedstorage", "sophisticatedcore", "toms_storage"),
                        o -> o instanceof String s && !s.isBlank());
        IGNORE_NON_CONTAINER_SCREENS = builder
                .comment("Runtime logging ignores obviously non-gameplay screens where possible.")
                .define("ignoreNonContainerScreens", true);
        builder.pop();

        SPEC = builder.build();
    }

    private GuiWorkbenchConfig() {}

    public static void init() {
        // Intentionally empty; class-load side effects create the spec.
    }
}
