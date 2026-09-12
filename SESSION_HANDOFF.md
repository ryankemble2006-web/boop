# BOOP Rally Shield handoff

Updated 2026-09-12. Explicit adjacent-game task, not Unified or HOME work.
Branch: boop-rally-shield. Project: rally-shield/. Package: com.boop.rally.
Base: live main af0db837bf9dde26d16c632fe75d8b5bca1f7fc0.

The user requested one launcher for the two existing portable Network Q rally games, a Shield APK, installation and testing, with source/builds on a separate BOOP GitHub branch.

Recovered source inputs: original RAC Rally RALLY folder and Rally Championship's accepted Browser-Test folder. Preserve both originals. Championship's original RALLY copy is not the accepted desktop launch target.

Architecture: native ARM64 DOSBox Pure 1.0-preview6 at a4a0bab7f8931433588f2fcad9045c85b277373d inside an offline two-game Android TV launcher. No separate RetroArch install or PC streaming. Separate game process, controller/remote mapping, pause/return menu and per-game persistent saves.

Public repository must contain only original launcher/host/build/test code and permitted open-source dependencies/notices. Keep proprietary game files, device addresses, raw logs/screenshots and private source receipts local. Local bundle preparation/provisioning supplies the user's existing games after installation. GitHub APK contains no games.

Use the existing permanent BOOP development signer in GitHub Actions, with fingerprint f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde. Do not export a key or replace any existing app.

State: implementation in progress; no APK has yet been built or installed. Source preparation tests have been written and observed failing before implementation. Hardware performance, sound, race entry and controller behavior are unverified.

Next: implement and behavior-test bundle/control layers, build native core and APK on GitHub, then provision and test on the positively identified Shield. Record actual build/device results before declaring completion.
