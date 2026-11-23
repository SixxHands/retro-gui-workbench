package com.retro.guiworkbench.runtime;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

public interface IScreenRegistry
{
    void record(String namespace, String screenClass, String title);

    Set<String> namespaces();

    Collection<LoggedScreenData> records();

    void clear();

    void save(Path file);

    void load(Path file);
}
