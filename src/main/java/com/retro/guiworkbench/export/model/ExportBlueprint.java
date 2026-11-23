package com.retro.guiworkbench.export.model;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public final class ExportBlueprint
{
    private static final int MinEditableScale = 1;
    private static final int MaxEditableScale = 16;

    private final Path _gameDirectory;
    private final String _outputFolderName;
    private final ExportMode _mode;
    private final ExportOptionsData _options;
    private final Set<String> _whitelist;
    private final Set<String> _openedNamespaces;

    private ExportBlueprint(
            Path gameDirectory,
            String outputFolderName,
            ExportMode mode,
            ExportOptionsData options,
            Set<String> whitelist,
            Set<String> openedNamespaces)
    {
        _gameDirectory = gameDirectory;
        _outputFolderName = outputFolderName;
        _mode = mode;
        _options = options;
        _whitelist = Set.copyOf(whitelist);
        _openedNamespaces = Set.copyOf(openedNamespaces);
    }

    public static ExportBlueprint create(
            Path gameDirectory,
            String outputFolderName,
            ExportMode mode,
            ExportOptionsData options,
            Set<String> whitelist,
            Set<String> openedNamespaces)
    {
        Objects.requireNonNull(gameDirectory, "gameDirectory");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(whitelist, "whitelist");
        Objects.requireNonNull(openedNamespaces, "openedNamespaces");

        if (outputFolderName == null || outputFolderName.isBlank())
        {
            throw new IllegalArgumentException("outputFolderName must not be blank.");
        }

        if (options.editableScale() < MinEditableScale || options.editableScale() > MaxEditableScale)
        {
            throw new IllegalArgumentException("editableScale must be between 1 and 16.");
        }

        return new ExportBlueprint(
                gameDirectory,
                outputFolderName.trim(),
                mode,
                options,
                whitelist,
                openedNamespaces);
    }

    public Path gameDirectory()
    {
        return _gameDirectory;
    }

    public String outputFolderName()
    {
        return _outputFolderName;
    }

    public ExportMode mode()
    {
        return _mode;
    }

    public ExportOptionsData options()
    {
        return _options;
    }

    public Set<String> whitelist()
    {
        return _whitelist;
    }

    public Set<String> openedNamespaces()
    {
        return _openedNamespaces;
    }
}
