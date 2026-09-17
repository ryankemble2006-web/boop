# Manual APK delivery and Ryan-owned visual acceptance

Date: 2026-09-17. Shared workflow/context handoff, not a new application release.

## User decision

Ryan now owns installation and all visual/physical tests. The assistant and GitHub own source changes, code review, non-visual tests, CI builds, existing permanent signing and downloadable signed APK delivery. Supply clearly labelled actual `.apk` links/buttons for Wall and Shield, with build/version identity. Ryan drags each APK into the appropriate scrcpy device window and reports results.

No automatic assistant/CI screenshots, visual sweeps, emulator runs or device-driving loops. Interpret screenshots, errors and feedback that Ryan supplies. Non-visual regression/logic tests, lint/static checks, builds and package/signer/hash checks remain enabled. A green build never substitutes for Ryan's visual acceptance.

Routine development/delivery no longer requires RDC, an assistant-controlled laptop or Work-mode switching. Do not automatically install candidates or prompt Ryan to restart a paid bridge merely to deliver them. Later explicitly requested diagnostics/installations are scoped exceptions, not a reversal of this default. The authoritative policy is the current `BOOP_START_HERE.md` on main; older joint-testing/install defaults are superseded.

## scrcpy setup and verification

The existing scrcpy 4.1 installation and Pixel 7 Pro launcher were reused. Added a separate local desktop shortcut `scrcpy - Shield` and its launcher. It discovers the saved Shield's network service when available, verifies its hardware identity before opening it, selects that target explicitly and keeps audio forwarding off. The Pixel 7 launcher remains separate; physical Pixel 10 was not targeted.

Observed local process metadata after launching: both `Pixel 7 Pro` and `Shield` scrcpy windows existed with nonzero window handles and `Responding=True`. This is process/connection evidence only. No screenshot was captured or inspected, no BOOP candidate was installed, and no visual or functional BOOP acceptance was performed. Ryan owns the next screen/navigation/install checks.

Private addresses and hardware identifiers remain only in the user's local launcher. Do not copy those into this public repository. The local shortcut files are workstation configuration, not BOOP source and not a new remote assistant bridge.

## Application continuity

The live application owner read for this task was `boop-wall-shield-split-v207` at `1b6816f611f88db67abf548eebe67057c01f5bab`. Its implementation and root handoff/status/memory were not rewritten by this shared-workflow update. Follow its newest live notes for v207 identities and the pending real-remote microphone test; do not infer new acceptance from scrcpy running.

Voice remains frozen. No application source, CI configuration, signing key, permission, app data, model, accepted artwork or application branch was changed. Existing dirty/concurrent local work was preserved. Repository publication for this task is documentation-only on main with CI-skip commit messages; no app test/build rerun was needed for these policy documents.

## Next task

Read the latest live main policy and the relevant application owner's newest handoff. Develop and verify the requested code on GitHub, deliver its signed APK download, then wait for Ryan's installation/visual report rather than starting a device-testing loop.
