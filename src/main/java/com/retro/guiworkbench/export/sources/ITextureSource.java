package com.retro.guiworkbench.export.sources;

import com.retro.guiworkbench.export.model.ExportBlueprint;
import com.retro.guiworkbench.export.model.ExportedTextureData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ITextureSource
{
    void collect(
            ExportBlueprint blueprint,
            Path packRoot,
            Path editableRoot,
            List<ExportedTextureData> manifest) throws IOException;
}
