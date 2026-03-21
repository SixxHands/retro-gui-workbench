package com.retro.guiworkbench.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.retro.guiworkbench.RetroGuiWorkbench;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class OpenedScreenRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, LoggedScreenRecord> RECORDS = new LinkedHashMap<>();

    private OpenedScreenRegistry() {}

    public static synchronized void record(String namespace, String screenClass, String title) {
        String key = namespace + "::" + screenClass;
        LoggedScreenRecord record = RECORDS.computeIfAbsent(key, ignored ->
                new LoggedScreenRecord(namespace, screenClass, title, Instant.now().toString(), 0));
        record.openCount++;
        record.lastOpenedUtc = Instant.now().toString();
        if (record.title == null || record.title.isBlank()) {
            record.title = title;
        }
    }

    public static synchronized Set<String> namespaces() {
        Set<String> out = new TreeSet<>();
        for (LoggedScreenRecord record : RECORDS.values()) {
            out.add(record.namespace.toLowerCase(Locale.ROOT));
        }
        return out;
    }

    public static synchronized Collection<LoggedScreenRecord> records() {
        return RECORDS.values();
    }

    public static synchronized void clear() {
        RECORDS.clear();
    }

    public static synchronized void write(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(RECORDS.values(), writer);
            }
        } catch (IOException e) {
            RetroGuiWorkbench.LOGGER.error("Failed to write screen registry to {}", file, e);
        }
    }

    public static synchronized void readIfPresent(Path file) {
        if (!Files.exists(file)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            LoggedScreenRecord[] records = GSON.fromJson(reader, LoggedScreenRecord[].class);
            RECORDS.clear();
            if (records != null) {
                for (LoggedScreenRecord record : records) {
                    RECORDS.put(record.namespace + "::" + record.screenClass, record);
                }
            }
        } catch (IOException e) {
            RetroGuiWorkbench.LOGGER.error("Failed to load screen registry from {}", file, e);
        }
    }
}
