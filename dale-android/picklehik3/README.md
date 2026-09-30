# DALE launcher for PickleHik3

This integration keeps PickleHik3's native Termux terminal, PTY/session engine, panes, keyboard, and rendering intact.

It adds a standalone `DaleLauncherActivity` as the Android `MAIN/LAUNCHER` entry point. The Terminal and NetHunter cards open the existing `TermuxActivity`.

The original NetHunter SVG is kept in:

```
app/src/main/assets/dale/nethunter-original.svg
```

The launcher renders that asset through the AndroidSVG dependency already present in PickleHik3.

## Apply locally

```bash
bash /path/to/Termux_Claude-Codex/dale-android/picklehik3/integrate-picklehik3.sh \
  /root/PickleHik3-termux-launcher \
  /path/to/nethunter-original.svg
```

The manifest is backed up as:

```
app/src/main/AndroidManifest.xml.dale-prelauncher.bak
```

The SVG is intentionally supplied as an external argument so the exact original artwork is preserved byte-for-byte instead of reconstructing a truncated pasted copy.
