# Current phone animation experiment: signed v145, installation blocked

Updated 2026-09-12. Owner: `boop-v144-devmenu-hue-experimental`,
worktree `.worktrees/boop-v144-devmenu-hue-experimental`.
This is phone-animation work, NOT the active Shield/Startup Manager task.

Source `055275a17ad0a0f485cc11f500edc0c258e63656` fixes the reproduced
pre-renderer hue initialization crash and the still-legacy IN-PLACE phone
developer-menu path. All 26 canonical clips are exposed by that actual menu.
277 local unit tests + 10 focused contracts pass; signed run `34681294081` passes.
See [exact verification/deployment receipt](docs/verification/v145-phone-crashfix.md).

**Pixel still runs v144. v145 was NOT installed: the installation tool blocked
the request.** No phone runtime/visual success is claimed for v145.
The verified signed APK is on the laptop Desktop. The next action is explicit
user confirmation before retrying the phone-only install, followed by the real
Launcher-to-eyes crash-path and in-place menu check. Keep the Shield untouched.
Do not bypass tool/Android permission gates or change the permanent signer.

The inherited notes below are historical context, not this task's current scope.

---
## Inherited historical notes

# BOOP Unified continuation memory

Updated 2026-09-12. This is the current scoped supplement. The complete preceding memory is preserved byte-for-byte at [docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md](docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md). Read it for protected checkpoints, wider app history and earlier decisions; current user instructions and the owning handoff take precedence over its dated progress entries.

## Current explicit scope

Ryan said: lyrics is WIP; leave it out; Startup Manager only; finish more quickly. Do not merge, debug or deploy lyrics as part of this delivery. Preserve that separate branch/worktree. The previous proposal to preserve lyrics inside v133 is superseded.

The corrected candidate is v134, retaining the Startup Manager-only repair from `42fe4be8ddfb196a34be73c713d434b235663415` and the already-established approved music artwork without editing or regenerating it. No additional feature work is authorized by this correction.

## Protected engineering facts

One Unified package remains `com.boop.alpha1`, using the permanent GitHub signer. Local-first controls, established profile routing, approved eyes/hands, audio behavior and rollback checkpoints remain protected. GitHub performs nonvisual functional/build/integrity checks; visual acceptance belongs to Ryan.

Startup Manager must preserve exact original enabled/background/boot state before a persistent change. Undo must repair a partial write even when successful-action flags were never recorded. Reject unknown or mismatched package state, preserve damaged receipts, bind commands to the verified Android user, cancel stale callbacks and retain the minimal recovery floor. The stock Google TV launcher is manageable, not blanket-protected by vendor prefix.

Java 17 compilation alone did not prove Android 11 runtime compatibility: v128 crashed on String.lines. The repaired parsers and synthetic Startup Manager suites were subsequently exercised on the actual Android 11 runtime without changing installed packages. CI success, signed artifact, agent device checks and Ryan's physical acceptance remain separate evidence levels.

## Current delivery boundary

The new no-lyrics source still requires its exact signed build, install and Package Control check. Last observed installed Shield version was v132. Re-read it and preserve the actual installed APK before deployment. Ryan authorized one music-track-change completion signal only after the requested delivery is genuinely installed. Do not send it for a source-only update.

The Yoga bridge currently rejects filesystem/terminal calls. Publishing via GitHub does not synchronize the local checkout automatically. On reconnection, fetch and reconcile without overwriting dirty or concurrent work. See [SESSION_HANDOFF.md](SESSION_HANDOFF.md).

## 2026-09-12 v143 phone animation experiment
- Branch: `boop-v143-phone-animation-experimental`.
- Source commit: `955a2d14484776bc2bd7a645034d66a306bc0115`.
- Version: `143 / 1.2.143-phone-animation-experimental`.
- Purpose: wire the finished canonical Animation Lab engine into the phone/Wall production face, not only the embedded lab and Shield media path.
- Production phone face now materializes as `BoopCanonicalFaceView`, backed by `ProductionAnimationController` + `CanonicalEyeRenderer`, while keeping existing semantic hooks for wake/sleep/listening/thinking/Berry/shake and notification presentation.
- GitHub signed run `34677505803`: SUCCESS. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. APK SHA-256 `51b879797ab9f9a84da53a7b80c3b7ce0b7d1500ba92fa9270f7de1b134c43cf`.
- Pixel 10 Pro XL install: upgraded from v142 to v143 with `adb install -r`; post-install dumpsys confirmed versionCode 143 and versionName 1.2.143-phone-animation-experimental; launcher intent injected successfully. v142 rollback APK preserved privately before replacement.
- Physical animation appearance remains Ryan-owned manual acceptance. Shield was not modified by this install.
