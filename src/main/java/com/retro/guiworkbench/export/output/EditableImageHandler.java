package com.retro.guiworkbench.export.output;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class EditableImageHandler
{
    private static final int OuterBorderAlpha = 40;
    private static final int CenterLineAlpha = 65;
    private static final int TitleBandAlpha = 60;
    private static final int InnerInsetPixels = 8;
    private static final int MinTitleBandPixels = 12;

    private EditableImageHandler()
    {
    }

    public static void writeEditableCopies(
            InputStream source,
            Path editableOutput,
            int scale,
            boolean guides) throws IOException
    {
        BufferedImage original = ImageIO.read(source);
        if (original == null)
        {
            return;
        }

        Path parent = editableOutput.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }

        ImageIO.write(scaleNearest(original, scale), "png", editableOutput.toFile());

        if (guides)
        {
            String stem = removePngExtension(editableOutput.getFileName().toString());
            Path guidePath = editableOutput.resolveSibling(stem + "__guide.png");
            BufferedImage guide = createGuideOverlay(original.getWidth() * scale, original.getHeight() * scale);
            ImageIO.write(guide, "png", guidePath.toFile());
        }
    }

    // Nearest-neighbor only. Bilinear turns 16x16 pixel art into soup.
    public static BufferedImage scaleNearest(BufferedImage original, int scale)
    {
        Objects.requireNonNull(original, "original");
        if (scale < 1)
        {
            throw new IllegalArgumentException("scale must be at least 1.");
        }

        if (scale == 1)
        {
            return original;
        }

        int scaledWidth = original.getWidth() * scale;
        int scaledHeight = original.getHeight() * scale;
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        graphics.dispose();
        return scaled;
    }

    // Translucent border, lines, rought title to line up new gui art withough guessing
    public static BufferedImage createGuideOverlay(int width, int height)
    {
        if (width <= 0 || height <= 0)
        {
            throw new IllegalArgumentException("Guide dimensions must be positive.");
        }
        BufferedImage guide = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = guide.createGraphics();

        graphics.setColor(new Color(255, 255, 255, OuterBorderAlpha));
        graphics.drawRect(0, 0, width - 1, height - 1);
        graphics.drawRect(
                InnerInsetPixels,
                InnerInsetPixels,
                Math.max(0, width - (InnerInsetPixels * 2 + 1)),
                Math.max(0, height - (InnerInsetPixels * 2 + 1)));

        graphics.setColor(new Color(255, 215, 0, CenterLineAlpha));
        graphics.drawLine(width / 2, 0, width / 2, height);
        graphics.drawLine(0, height / 2, width, height / 2);

        graphics.setColor(new Color(100, 180, 255, TitleBandAlpha));
        int titleBand = Math.max(MinTitleBandPixels, height / 10);
        graphics.drawRect(4, 4, Math.max(0, width - 9), Math.max(0, titleBand));

        graphics.dispose();
        return guide;
    }

    public static String removePngExtension(String fileName)
    {
        if (fileName.endsWith(".png"))
        {
            return fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }
}
