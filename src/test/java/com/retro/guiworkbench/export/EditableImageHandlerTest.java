package com.retro.guiworkbench.export;

import com.retro.guiworkbench.export.output.EditableImageHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EditableImageHandlerTest
{
    @Test
    void scalesImageUsingNearestNeighborWithoutBlending()
    {
        BufferedImage source = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        source.setRGB(0, 0, Color.RED.getRGB());
        source.setRGB(1, 0, Color.BLUE.getRGB());
        source.setRGB(0, 1, Color.GREEN.getRGB());
        source.setRGB(1, 1, Color.WHITE.getRGB());

        BufferedImage scaled = EditableImageHandler.scaleNearest(source, 4);

        assertEquals(8, scaled.getWidth());
        assertEquals(8, scaled.getHeight());
        assertEquals(Color.RED.getRGB(), scaled.getRGB(3, 3));
        assertEquals(Color.BLUE.getRGB(), scaled.getRGB(4, 0));
    }

    @Test
    void writesScaledCopyAndGuideOverlayWhenEnabled(@TempDir Path tempDirectory) throws IOException
    {
        byte[] pngBytes = createPngBytes(16, 16);
        Path outputFile = tempDirectory.resolve("minecraft/chest__x2.png");

        EditableImageHandler.writeEditableCopies(new ByteArrayInputStream(pngBytes), outputFile, 2, true);

        Path guideFile = tempDirectory.resolve("minecraft/chest__x2__guide.png");
        assertTrue(Files.exists(outputFile));
        assertTrue(Files.exists(guideFile));

        BufferedImage scaled = ImageIO.read(outputFile.toFile());
        BufferedImage guide = ImageIO.read(guideFile.toFile());
        assertEquals(32, scaled.getWidth());
        assertEquals(32, guide.getHeight());
    }

    @Test
    void omitsGuideOverlayWhenDisabled(@TempDir Path tempDirectory) throws IOException
    {
        byte[] pngBytes = createPngBytes(8, 8);
        Path outputFile = tempDirectory.resolve("minecraft/bar__x2.png");

        EditableImageHandler.writeEditableCopies(new ByteArrayInputStream(pngBytes), outputFile, 2, false);

        assertTrue(Files.exists(outputFile));
        assertFalse(Files.exists(tempDirectory.resolve("minecraft/bar__x2__guide.png")));
    }

    @Test
    void rejectsInvalidScaleAndNonPositiveGuideDimensions()
    {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        assertThrows(IllegalArgumentException.class, () -> EditableImageHandler.scaleNearest(image, 0));
        assertThrows(IllegalArgumentException.class, () -> EditableImageHandler.createGuideOverlay(0, 16));
        assertThrows(IllegalArgumentException.class, () -> EditableImageHandler.createGuideOverlay(16, -4));
    }

    private static byte[] createPngBytes(int width, int height) throws IOException
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
