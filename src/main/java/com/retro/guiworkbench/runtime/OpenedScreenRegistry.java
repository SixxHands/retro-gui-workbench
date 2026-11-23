package com.retro.guiworkbench.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.retro.guiworkbench.export.model.GuiWorkbenchException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class OpenedScreenRegistry implements IScreenRegistry
{
    private static final Gson Json = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, LoggedScreenData> _records = new LinkedHashMap<>();

    OpenedScreenRegistry()
    {
    }

    @Override
    public synchronized void record(String namespace, String screenClass, String title)
    {
        String key = namespace + "::" + screenClass;
        String now = Instant.now().toString();
        LoggedScreenData existing = _records.get(key);
        if (existing == null)
        {
            _records.put(key, new LoggedScreenData(namespace, screenClass, title, now, 1));
            return;
        }

        _records.put(key, existing.recordOpen(title, now));
    }

    @Override
    public synchronized Set<String> namespaces()
    {
        Set<String> out = new TreeSet<>();
        for (LoggedScreenData entry : _records.values())
        {
            out.add(entry.namespace().toLowerCase(Locale.ROOT));
        }

        return Set.copyOf(out);
    }

    @Override
    public synchronized Collection<LoggedScreenData> records()
    {
        return List.copyOf(_records.values());
    }

    @Override
    public synchronized void clear()
    {
        _records.clear();
    }

    @Override
    public synchronized void save(Path file)
    {
        try
        {
            Path parent = file.getParent();
            if (parent != null)
            {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(file))
            {
                Json.toJson(_records.values(), writer);
            }
        }
        catch (IOException exception)
        {
            throw new GuiWorkbenchException("Failed to write screen registry to " + file, exception);
        }
    }

    @Override
    public synchronized void load(Path file)
    {
        if (!Files.exists(file))
        {
            return;
        }

        try (Reader reader = Files.newBufferedReader(file))
        {
            LoggedScreenData[] loaded = Json.fromJson(reader, LoggedScreenData[].class);
            _records.clear();
            if (loaded != null)
            {
                for (LoggedScreenData entry : loaded)
                {
                    _records.put(entry.namespace() + "::" + entry.screenClass(), entry);
                }
            }
        }
        catch (IOException | JsonParseException exception)
        {
            throw new GuiWorkbenchException("Failed to load screen registry from " + file, exception);
        }
    }
}
