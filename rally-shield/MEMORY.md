# Rally project memory

The user explicitly requested the two existing desktop Network Q games combined into one Shield launcher, installed and tested, on a separate BOOP GitHub branch. This is an adjacent game project, not Unified/HOME integration.

Use branch/worktree `boop-rally-shield`, project `rally-shield/`, package `com.boop.rally`, app label BOOP Rally. Preserve original desktop folders. Championship's accepted payload is Browser-Test, not RALLY or Installed. Native ARM64 DOSBox Pure 1.0-preview6 is embedded; no separate RetroArch install or PC streaming.

Game content stays private. Public Actions APK contains the engine/launcher only; local preparation and provisioning supply the user's existing files. Ordinary DOS game-file persistence is implemented separately from immutable packs; do not call it RAM save-state emulation or claim it passed persistence testing yet. Existing permanent BOOP signing remains in Actions.

Installed candidate: version 1 / 1.0, source `71c71157fd9b6931b3c7d320409fa32d94f72197`, successful run `34688081241`, artifact `10296216335`. APK SHA-256 `cb3b37efd9dfe4b5ea1850c4fe3ff9155e3c319ecefa1855ee1ae6f408e192d8`, 1,602,630 bytes. Permanent certificate `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Installation and both private pack hashes were verified on the Shield.

Desktop folder `Network Q Rally Shield` holds release/BOOP-Rally.apk, source/notices/receipts, games/rac93.zip, games/rac96.zip, the preparation receipt and private evidence. Originals verified unchanged. Source already exists on GitHub: do not begin another game search, emulator port or signer setup.

Twelve behavioral tests passed, including 22 Java controls assertions and native queue tests; rerun successfully from the artifact's corresponding source. Both games booted with advancing video and nonzero PCM. Championship menus responded. Actual racing, physical controller feel, audible sound, save/return/reopen and repeated-switch/resume tests remain incomplete. CI/package/signature evidence is not physical acceptance.

A separate package `local.networkq.rally` was also installed and sometimes foreground. Its text-entry and accelerator-toggle menus do not belong to this code. Leave that package and Johnny Castaway untouched. Confirm resumed package and selected game around each future device test; restored Android tasks can reopen the previously selected game.

Late remote commands, file reads and a ping timed out. Final docs were committed through GitHub directly. Fetch live intended branch before resuming; do not assume the laptop worktree includes these documentation commits. No app code changed after the installed source above. Read SESSION_HANDOFF.md, BOOP_STATUS.md and VERIFICATION.md for the exact remaining tests, source-archive rebuild caveat and review limitations.
