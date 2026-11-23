# Retro GUI Workbench

A small NeoForge 1.21.1 client mod for pulling vanilla and modded GUI textures into a resource pack folder so you can edit them without digging through jars by hand.

This is a client-side tool. Dedicated servers will load it fine, however exports and screen logging only run on the client because they need the active resource manager.

## How to use it

1. Launch Minecraft with the mod installed and join a world.
2. Press `F8` to turn screen logging on, then open the menus or inventories you want to reskin.
3. Press `F9` to export only the mods whose screens you opened, or `F10` to export everything allowed by your config.
4. Open `<gameDir>/retro_gui_workbench_export/GuiWorkbenchPack/` to find the generated resource pack.

## What gets exported

Inside `GuiWorkbenchPack/` you get a pack ready to drop straight into `resourcepacks/`, plus two helper folders for art work.

- `pack.mcmeta` and `assets/<namespace>/...` keeping the original texture paths (pack format 34)
- `_editable/<namespace>/*__x4.png` nearest-neighbor upscaled copies so pixel art stays sharp while editing
- `_editable/<namespace>/*__guide.png` translucent border, center lines, and rough title band overlays
- `_manifest/textures.json`, `_manifest/screens_opened.json`, and `_manifest/export_report.txt`

## Keybinds and commands

| Input | What it does |
|---|---|
| `F8` or `/guiwb log toggle` | Toggle runtime screen logging |
| `/guiwb log start` / `stop` | Turn runtime screen logging on or off |
| `F9` or `/guiwb export opened` | Export GUI textures only for mods you opened while logging |
| `F10` or `/guiwb export all` | Export all matching vanilla and modded GUI textures |
| `/guiwb scan` | Same as `/guiwb export all` |
| `/guiwb clearlog` | Clear the current session's logged screens |

## Config

Generated on first launch at `config/guiworkbench-common.toml`.

- `outputFolderName`: folder created inside your Minecraft directory (default `retro_gui_workbench_export`)
- `generateEditableCopies`, `editableScale`, `generateGuides`: toggle upscaled copies (default `4x`) and guide overlays
- `includeVanilla`, `includeMods`: choose whether to pull from Minecraft's resource manager, `mods/*.jar`, or both
- `modWhitelist`: limits jar scanning to specific namespaces when non-empty (clear the list to scan every mod jar)
- `exportOnStartup`: run a full export automatically once the client finishes loading

## Building

Requires Java 21.

```bash
./gradlew test build
```

The built jar lands in `build/libs/guiworkbench-1.0.0.jar`.

## License

MIT. See [LICENSE](LICENSE).
