package com.retro.guiworkbench.export.output;

import com.retro.guiworkbench.export.model.ExportOptionsData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TextureFileWriter
{
    private TextureFileWriter()
    {
    }

    public static Path writeTexture(
            byte[] pngBytes,
            String namespace,
            String resourcePath,
            Path packRoot,
            Path editableRoot,
            ExportOptionsData options) throws IOException
    {
        Path target = packRoot.resolve("assets").resolve(namespace).resolve(resourcePath);
        Path parent = target.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }

        Files.write(target, pngBytes);

        if (options.generateEditableCopies())
        {
            String stem = EditableImageHandler.removePngExtension(target.getFileName().toString());
            Path editableTarget = editableRoot
                    .resolve(namespace)
                    .resolve(stem + "__x" + options.editableScale() + ".png");
            EditableImageHandler.writeEditableCopies(
                    new ByteArrayInputStream(pngBytes),
                    editableTarget,
                    options.editableScale(),
                    options.generateGuides());
        }

        return target;
    }
}
