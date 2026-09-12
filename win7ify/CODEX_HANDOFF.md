# BOOP Win7ify — Codex handoff

## Start here

Branch: `boop-win7ify-v01`

This is an adjacent BOOP Windows utility, not part of the Android Unified APK. Keep `boop-unified`, `boop-canonical-rebuild`, Launcher/Wall/Shield/Turbo lineages, Android signing and protected checkpoints untouched.

Read the branch design and implementation plan first:

- `docs/superpowers/specs/2026-09-12-boop-win7ify-v01-design.md`
- `docs/superpowers/plans/2026-09-12-boop-win7ify-v01.md`

## Current repair and physical result

The initial 0.1 EXE launched but the first physical Apply failed. Do not treat its
old green CI result as physical acceptance. The protected Widgets setting
`TaskbarDa` rejected even an identical-value write; `TaskbarAl` did not. The existing
user backup remains compatible and must be preserved.

Version 0.1.1 addresses this without elevation or permission bypass. Apply and
Restore now return `IReadOnlyList<ChangeResult>` with `Changed`, `AlreadyCorrect`,
`Blocked`, and `Failed` outcomes. They skip matching data, read back writes, continue
past per-setting denials, and preserve the original backup after incomplete restore.
Backup reads validate version/type/duplicates/production allowlist, and updates are
atomic. Restore leaves empty key structures alone: schema v1 recorded values, not
whether entire keys originally existed.

The UI opens maximised, keeps actions outside the scrollable choices, uses legible
secondary buttons, and shows failures in the log after buttons are re-enabled.
Apply/Restore no longer automatically kill Explorer. A separate confirmed action
restarts only the current session's desktop shell. Startup is read-only.

The focused RED proof is commit `810324265acddfa030b44a846ca93b1ba3ef1163`,
run `34670905369`: five new failure cases failed as intended and the old nine passed.
Read `../SESSION_HANDOFF.md` and `STATUS.md` for the latest repair verification.

## Historical v0.1 build evidence

The core is deliberately separated from WinForms behind `IRegistryStore`.

Automated contracts cover:

1. first Apply saves the real original before writing;
2. repeated Apply never replaces that original baseline;
3. Restore removes values that did not originally exist;
4. Restore preserves original registry kind/data;
5. the v0.1 tweak catalogue stays scoped to the approved set;
6. the default preset excludes Experimental tweaks;
7. Windows DWORD conversion round-trips;
8. ExpandString preserves its registry kind;
9. Binary conversion preserves bytes.

The approved BOOP eyes are reused as the exact binary from the BOOP asset lineage. Locked SHA-256: `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. The build must fail if this changes.

First complete green build observed while creating v0.1:

- workflow: `BOOP Win7ify`;
- run: `34670164392`;
- source commit: `0ba351eccfa138b92f34ba9fb7045c351af6c4c9`;
- artifact: `10290356901`, `BOOP-Win7ify-v0.1-win-x64`;
- artifact ZIP digest: `sha256:f44c475676aa29ee9426114305396654e11401caa9bfe7c94dbebcad455a73c8`;
- `Win7ify.exe` SHA-256: `781a446060b13477762ae1ed3be286bb472b96d907c283029552197f2f30a0d9`;
- independent artifact inspection confirmed a PE32+ x86-64 Windows GUI executable and confirmed the receipt hash matches the EXE bytes.

This is CI/build evidence, not physical acceptance of the visual result or of every Windows 11 shell tweak.

## Protected v0.1 behaviour

Preserve these unless new evidence justifies a deliberate redesign:

- `PUT WINDOWS 11 BACK` remains prominent and simple;
- original registry values are captured before first touch and survive repeated Apply operations;
- a failed restore retains the backup;
- normal preset remains HKCU-only with no elevation request; protected values may still be blocked;
- Experimental classic-context-menu support remains opt-in;
- no protected Windows binary replacement;
- no silent third-party downloads/installers;
- no security/update/activation tampering;
- no claim that CI proves appearance;
- do not regenerate or restyle the approved BOOP eyes.

## Advanced phase for Codey

The next phase can investigate, in roughly this order:

1. **Real Windows 7 Start experience.** Prefer a clean integration boundary around a proven open-source implementation such as Open-Shell rather than patching Explorer. Any download/install must be explicit, consent-driven and licence-compliant.
2. **Taskbar fidelity.** Research the current Windows 11 shell version before choosing between supported APIs, companion shell UI, or third-party integration. Keep recovery possible without the Win7ify process running.
3. **Explorer chrome.** Restore useful Windows 7-style navigation/command affordances without replacing protected Explorer binaries.
4. **Aero/glass.** Treat this as presentation only. Do not let visual effects become a dependency for restore/recovery.
5. **Windows build adaptation.** Detect build/version and mark each tweak supported, best-effort or unavailable rather than blindly applying stale registry folklore.
6. **Windows Sandbox regression coverage.** Automate apply/restore registry-state checks on representative Windows builds where CI infrastructure permits it.
7. **Release hardening.** Consider Authenticode signing separately from BOOP Android signing, plus versioned release notes and durable artifact retention.
8. **Better UX diagnostics.** Keep BOOP language plain for users, but give Codey/diagnostic views exact build, tweak, registry path and failure-stage evidence.

Do not solve these by sacrificing the v0.1 rollback model.

## Physical acceptance still needed

On a real Windows 11 machine, check:

- app launches and BOOP eyes look correct;
- Apply makes the requested visible changes that the installed build supports;
- the separately confirmed current-desktop Explorer refresh restarts cleanly;
- repeated Apply does not corrupt the original backup;
- Restore returns the original visible state;
- Experimental context-menu toggle can be applied and restored independently;
- no unrelated user settings move.

If a Windows update ignores a legacy value, label that tweak unsupported/best-effort for that build instead of escalating immediately to binary patching.