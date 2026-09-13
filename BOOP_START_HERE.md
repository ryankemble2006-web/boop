# BOOP - current notes only

Updated 2026-09-13. This file intentionally contains only the newest routing notes.
Old startup-rule/context stacks were removed because they had become stale and slowed down work.
Current user instructions always win.

## Unified app

Current combined device delivery is owned by `boop-unified-v146-integration`.
Version 146 / `1.2.146-phone-lyrics-startup`, package `com.boop.alpha1`, was installed on the Pixel 10 Pro XL and Nvidia Shield.
It combines the phone iris/menu/crash fixes, Lyrics availability preflight, and Startup Manager/defaults.
For new combined work, start from that live branch or a newer verified successor, not an older isolated v142/v145 line.
The standalone Animation Lab remains separate unless explicitly merged.

## Standalone Shield Launcher

The separate launcher-and-advanced-tools build is owned by `boop-shield-launcher-standalone`.
Project: `shield-launcher/`. Package: `com.boop.shieldlauncher`.
It is intentionally separate from Unified and should not silently absorb unrelated Unified changes.

## Network Q Rally Shield

The separate Shield rally project is owned by `boop-rally-shield`.
Project: `rally-shield/`. Package: `com.boop.rally`.
Keep it isolated from Unified and preserve original game folders/private packs.

## Working rule

For any BOOP task, fetch the intended live branch, preserve dirty/concurrent work, and read only that branch's newest task-specific handoff or verification note when needed.
Do not require the deleted historical root rule stack before planning or editing.
