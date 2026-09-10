# BOOP // BLOCK PARTY

A little order. A lot of falling things.

A standalone, offline Kodi falling-block game for the sofa. Neon extruded cubes, the real approved BOOP eyes, landing guide, three-piece preview, seven-bag randomisation, increasing speed, combo bonuses and a local personal best.

Classic 10 x 20 gameplay with pre-rendered 3D cube faces (2.5D). This is not a volumetric 3D well and does not require a browser or game emulator.

## Install in Kodi

1. Copy `script.boop.blockparty-1.0.0.zip` somewhere Kodi can browse (Downloads, USB or your usual file source).
2. Open **Add-ons > Install from zip file** and choose it. If Kodi asks, enable **Unknown sources** for this local add-on installation.
3. Open **Add-ons > Program add-ons > BOOP - Block Party**.
4. Press **Enter / OK** to begin.

Kodi 19+ with Python 3 is required. Kodi 21 is the intended target. Stock-style Kodi directional/Select actions are used; a custom skin or remote keymap may remap physical buttons.

To put it directly in a skin menu, use:

```xml
<onclick>RunScript(script.boop.blockparty)</onclick>
```

You can also open its context menu and **Add to favourites**.

## Controls

| Button | Action |
| --- | --- |
| Enter / remote OK | Rotate clockwise; start / replay / resume |
| Left / Right | Move |
| Down | Fall faster; one row per action/repeat |
| Up | Drop to the landing guide immediately |
| Back / Escape | Pause; press Back again to leave |
| Play/Pause | Pause / resume |

The game starts on a title card and never needs a mouse. It pauses if another Kodi window takes focus. It does not install global keymaps, change your skin, control media playback, or contact a server. Tiny local sound effects play through Kodi's UI-sound API; existing media is not paused automatically.

## Rules

Fill horizontal rows. Single/double/triple/four-line clears score 100/300/500/800 times the current level. Consecutive clearing pieces add a combo bonus. Soft drops earn one point per row; hard drops earn two. Every ten cleared rows raises the level. Pieces have a short lock delay and simple wall/floor kicks; this is not an implementation of competitive Guideline SRS or T-spin scoring.

Best score is saved in this add-on's private profile as `best.json`. No account, telemetry or runtime download.

## Source and verification

`script.boop.blockparty/` is the complete add-on. `tests/` covers the game engine and controls without needing Kodi. Run with Python 3:

```text
python -m unittest discover -s tests -v
```

`make_assets.py` generates the cabinet, cube textures, icon and original short sound effects with Pillow and Windows Segoe UI fonts. It reuses the shipped exact approved eye master, or accepts `--eyes PATH`. The shipped PNGs need no generation at installation/runtime.

The approved eye image is shipped byte-for-byte unchanged. Its SHA-256 is `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Runtime scaling/blinking does not edit it. BOOP artwork remains under its owner's rights; code is MIT-licensed. This is an independent falling-block game, not an official Tetris product.

See `VERIFICATION.md` for the exact checks performed. Target-device/remote and appearance acceptance belong to Ryan.
