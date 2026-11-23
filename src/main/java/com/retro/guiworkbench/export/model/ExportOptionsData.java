package com.retro.guiworkbench.export.model;

public record ExportOptionsData(
        boolean includeVanilla,
        boolean includeMods,
        boolean generateEditableCopies,
        int editableScale,
        boolean generateGuides)
{
}
