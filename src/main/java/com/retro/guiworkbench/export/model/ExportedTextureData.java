package com.retro.guiworkbench.export.model;

import java.util.Objects;

public record ExportedTextureData(
        String namespace,
        String path,
        String source,
        String output)
{
    public ExportedTextureData
    {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(output, "output");
    }
}
