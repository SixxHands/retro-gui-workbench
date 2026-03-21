package com.retro.guiworkbench.export;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EditableImageUtil {
    private EditableImageUtil() {}

    public static void writeEditableCopies(InputStream source, Path editableOutput, int scale, boolean guides) throws IOException {
        BufferedImage original = ImageIO.read(source);
        if (original == null) {
            return;
        }

        Files.createDirectories(editableOutput.getParent());
        ImageIO.write(scaleNearest(original, scale), "png", editableOutput.toFile());

        if (guides) {
            Path guidePath = editableOutput.resolveSibling(stripPng(editableOutput.getFileName().toString()) + "__guide.png");
            BufferedImage guide = createGuideOverlay(original.getWidth() * scale, original.getHeight() * scale);
            ImageIO.write(guide, "png", guidePath.toFile());
        }
    }

    public static BufferedImage scaleNearest(BufferedImage original, int scale) {
        if (scale <= 1) {
            return original;
        }

        BufferedImage out = new BufferedImage(original.getWidth() * scale, original.getHeight() * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(original, 0, 0, out.getWidth(), out.getHeight(), null);
        g.dispose();
        return out;
    }

    public static BufferedImage createGuideOverlay(int width, int height) {
        BufferedImage guide = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = guide.createGraphics();

        g.setColor(new Color(255, 255, 255, 40));
        g.drawRect(0, 0, width - 1, height - 1);
        g.drawRect(8, 8, Math.max(0, width - 17), Math.max(0, height - 17));

        g.setColor(new Color(255, 215, 0, 65));
        g.drawLine(width / 2, 0, width / 2, height);
        g.drawLine(0, height / 2, width, height / 2);

        g.setColor(new Color(100, 180, 255, 60));
        int titleBand = Math.max(12, height / 10);
        g.drawRect(4, 4, Math.max(0, width - 9), Math.max(0, titleBand));

        g.dispose();
        return guide;
    }

    private static String stripPng(String fileName) {
        return fileName.endsWith(".png") ? fileName.substring(0, fileName.length() - 4) : fileName;
    }
}
