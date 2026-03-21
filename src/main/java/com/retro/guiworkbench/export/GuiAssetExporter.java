package com.retro.guiworkbench.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.retro.guiworkbench.RetroGuiWorkbench;
import com.retro.guiworkbench.config.GuiWorkbenchConfig;
import com.retro.guiworkbench.runtime.OpenedScreenRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class GuiAssetExporter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GuiAssetExporter() {}

    public static Path export(Minecraft minecraft, ExportMode mode) {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path outputRoot = gameDir.resolve(GuiWorkbenchConfig.OUTPUT_FOLDER_NAME.get());
        Path packRoot = outputRoot.resolve("GuiWorkbenchPack");
        Path manifestRoot = packRoot.resolve("_manifest");
        Path editableRoot = packRoot.resolve("_editable");

        Set<String> openedNamespaces = OpenedScreenRegistry.namespaces();
        Set<String> whitelist = normalizedWhitelist();
        List<Map<String, Object>> manifest = new ArrayList<>();

        try {
            Files.createDirectories(packRoot);
            Files.createDirectories(manifestRoot);
            Files.createDirectories(editableRoot);
            writePackMcmeta(packRoot.resolve("pack.mcmeta"));

            if (GuiWorkbenchConfig.INCLUDE_VANILLA.get()) {
                exportVanilla(minecraft, packRoot, editableRoot, manifest, mode, openedNamespaces);
            }

            if (GuiWorkbenchConfig.INCLUDE_MODS.get()) {
                exportJarAssets(gameDir.resolve("mods"), packRoot, editableRoot, manifest, mode, whitelist, openedNamespaces);
            }

            OpenedScreenRegistry.write(manifestRoot.resolve("screens_opened.json"));
            writeManifest(manifestRoot.resolve("textures.json"), manifest);
            writeSimpleReport(manifestRoot.resolve("export_report.txt"), manifest, mode, whitelist, openedNamespaces);
        } catch (IOException e) {
            RetroGuiWorkbench.LOGGER.error("GUI export failed.", e);
        }

        return packRoot;
    }

    private static void exportVanilla(Minecraft minecraft,
                                      Path packRoot,
                                      Path editableRoot,
                                      List<Map<String, Object>> manifest,
                                      ExportMode mode,
                                      Set<String> openedNamespaces) throws IOException {
        if (minecraft == null || minecraft.getResourceManager() == null) {
            return;
        }

        if (mode == ExportMode.OPENED_ONLY && !openedNamespaces.contains("minecraft")) {
            return;
        }

        Predicate<ResourceLocation> filter = rl -> rl.getNamespace().equals("minecraft")
                && rl.getPath().startsWith("textures/gui")
                && rl.getPath().endsWith(".png")
                && looksLikeGameplayGui(rl.getPath());

        Map<ResourceLocation, Resource> resources = minecraft.getResourceManager().listResources("textures/gui", filter);
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation rl = entry.getKey();
            Resource resource = entry.getValue();
            Path target = packRoot.resolve("assets").resolve(rl.getNamespace()).resolve(rl.getPath());
            copyResource(resource, target);
            maybeCreateEditable(resource, editableRoot.resolve(rl.getNamespace()).resolve(fileBaseName(target) + "__x" + GuiWorkbenchConfig.EDITABLE_SCALE.get() + ".png"));
            manifest.add(manifestEntry(rl.getNamespace(), rl.getPath(), "resource_manager", target));
        }
    }

    private static void exportJarAssets(Path modsDir,
                                        Path packRoot,
                                        Path editableRoot,
                                        List<Map<String, Object>> manifest,
                                        ExportMode mode,
                                        Set<String> whitelist,
                                        Set<String> openedNamespaces) throws IOException {
        if (!Files.isDirectory(modsDir)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(modsDir, "*.jar")) {
            for (Path jar : stream) {
                exportJar(jar, packRoot, editableRoot, manifest, mode, whitelist, openedNamespaces);
            }
        }
    }

    private static void exportJar(Path jar,
                                  Path packRoot,
                                  Path editableRoot,
                                  List<Map<String, Object>> manifest,
                                  ExportMode mode,
                                  Set<String> whitelist,
                                  Set<String> openedNamespaces) {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (!name.startsWith("assets/") || !name.endsWith(".png")) {
                    continue;
                }

                String[] parts = name.split("/", 4);
                if (parts.length < 4) {
                    continue;
                }

                String namespace = parts[1].toLowerCase(Locale.ROOT);
                String resourcePath = parts[2] + "/" + parts[3];

                if (!looksLikeGameplayGui(resourcePath)) {
                    continue;
                }

                if (!whitelist.isEmpty() && !whitelist.contains(namespace)) {
                    continue;
                }

                if (mode == ExportMode.OPENED_ONLY && !openedNamespaces.contains(namespace)) {
                    continue;
                }

                Path target = packRoot.resolve("assets").resolve(namespace).resolve(resourcePath);
                Files.createDirectories(target.getParent());
                try (InputStream in = zip.getInputStream(entry)) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    maybeCreateEditable(in, editableRoot.resolve(namespace).resolve(fileBaseName(target) + "__x" + GuiWorkbenchConfig.EDITABLE_SCALE.get() + ".png"));
                }

                manifest.add(manifestEntry(namespace, resourcePath, jar.getFileName().toString(), target));
            }
        } catch (Exception e) {
            RetroGuiWorkbench.LOGGER.warn("Skipping jar {} due to export error: {}", jar, e.getMessage());
        }
    }

    private static boolean looksLikeGameplayGui(String path) {
        String lowered = path.toLowerCase(Locale.ROOT);
        if (!lowered.contains("gui") && !lowered.contains("screen") && !lowered.contains("container")
                && !lowered.contains("inventory") && !lowered.contains("slot") && !lowered.contains("terminal")
                && !lowered.contains("backpack") && !lowered.contains("storage") && !lowered.contains("tab")
                && !lowered.contains("button") && !lowered.contains("bar")) {
            return false;
        }

        return !lowered.contains("title")
                && !lowered.contains("main_menu")
                && !lowered.contains("realms")
                && !lowered.contains("logo")
                && !lowered.contains("accessibility")
                && !lowered.contains("options");
    }

    private static void copyResource(Resource resource, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream in = resource.open()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void maybeCreateEditable(Resource resource, Path editableTarget) throws IOException {
        if (!GuiWorkbenchConfig.GENERATE_EDITABLE_COPIES.get()) {
            return;
        }
        try (InputStream in = resource.open()) {
            EditableImageUtil.writeEditableCopies(in, editableTarget, GuiWorkbenchConfig.EDITABLE_SCALE.get(), GuiWorkbenchConfig.GENERATE_GUIDES.get());
        }
    }

    private static void maybeCreateEditable(InputStream source, Path editableTarget) throws IOException {
        if (!GuiWorkbenchConfig.GENERATE_EDITABLE_COPIES.get()) {
            return;
        }
        EditableImageUtil.writeEditableCopies(source, editableTarget, GuiWorkbenchConfig.EDITABLE_SCALE.get(), GuiWorkbenchConfig.GENERATE_GUIDES.get());
    }

    private static void writePackMcmeta(Path target) throws IOException {
        String json = """
                {
                  \"pack\": {
                    \"pack_format\": 34,
                    \"description\": \"Generated by Retro GUI Workbench\"
                  }
                }
                """;
        Files.writeString(target, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void writeManifest(Path target, List<Map<String, Object>> manifest) throws IOException {
        Files.createDirectories(target.getParent());
        try (Writer writer = Files.newBufferedWriter(target)) {
            GSON.toJson(manifest, writer);
        }
    }

    private static void writeSimpleReport(Path target,
                                          List<Map<String, Object>> manifest,
                                          ExportMode mode,
                                          Set<String> whitelist,
                                          Set<String> openedNamespaces) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("Retro GUI Workbench Export\n");
        out.append("Mode: ").append(mode).append('\n');
        out.append("Exported textures: ").append(manifest.size()).append('\n');
        out.append("Whitelist: ").append(whitelist.isEmpty() ? "<all namespaces>" : whitelist).append('\n');
        out.append("Opened namespaces: ").append(openedNamespaces.isEmpty() ? "<none logged>" : openedNamespaces).append('\n');
        out.append("Editable copies: ").append(GuiWorkbenchConfig.GENERATE_EDITABLE_COPIES.get()).append('\n');
        out.append("Editable scale: ").append(GuiWorkbenchConfig.EDITABLE_SCALE.get()).append('\n');
        out.append("Guides: ").append(GuiWorkbenchConfig.GENERATE_GUIDES.get()).append('\n');
        Files.writeString(target, out.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static Set<String> normalizedWhitelist() {
        Set<String> out = new LinkedHashSet<>();
        for (String value : GuiWorkbenchConfig.MOD_WHITELIST.get()) {
            if (value != null && !value.isBlank()) {
                out.add(value.toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static Map<String, Object> manifestEntry(String namespace, String path, String source, Path outputPath) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("namespace", namespace);
        record.put("path", path);
        record.put("source", source);
        record.put("output", outputPath.toString());
        return record;
    }

    private static String fileBaseName(Path target) {
        String fileName = target.getFileName().toString();
        return fileName.endsWith(".png") ? fileName.substring(0, fileName.length() - 4) : fileName;
    }
}
