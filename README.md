# Retro GUI Workbench (NeoForge 1.21.1)

Client-driven NeoForge 1.21.1 tool mod that exports gameplay GUI textures from vanilla and installed mod jars into a ready-to-edit resource pack folder. It can also write upscaled nearest-neighbor work copies, alignment guide overlays, and a log of screens you opened while playing.

It only runs exports on the client; dedicated servers cannot run GUI exports.

## What it exports

Running an export writes `<gameDir>/<outputFolderName>/GuiWorkbenchPack/` with:

- `pack.mcmeta` (pack format 34) and `assets/<namespace>/...` matching the original texture paths
- `_editable/<namespace>/*__x<scale>.png` nearest-neighbor upscaled copies (when `generateEditableCopies` is on)
- `_editable/<namespace>/*__guide.png` border, center-line, and title-band overlays (when `generateGuides` is on)
- `_manifest/textures.json`, `_manifest/screens_opened.json`, and `_manifest/export_report.txt`

## Keybinds

- `F8` - toggle runtime screen logging
- `F9` - export GUI textures for namespaces seen while logging
- `F10` - export all configured GUI textures

## Commands

- `/guiwb log start` - start logging opened gameplay screens
- `/guiwb log stop` - stop logging opened gameplay screens
- `/guiwb log toggle` - toggle runtime screen logging
- `/guiwb export all` - export all configured GUI textures
- `/guiwb export opened` - export only namespaces logged during play
- `/guiwb scan` - alias for `/guiwb export all`
- `/guiwb clearlog` - clear the in-memory logged screen list

## Config

Config file is generated at `config/guiworkbench-common.toml` with sections for `[passes]`, `[export]`, `[output]`, and `[filters]`. When `modWhitelist` is non-empty, jar scanning only exports textures from those namespaces; leave it empty to scan every mod jar.

## Build

Requires Java 21.

```bash
./gradlew test build
```

Output jar is written to `build/libs/guiworkbench-1.0.0.jar`.

## License

MIT. See [LICENSE](LICENSE).
