# BOOP canonical voice baseline and active rebuild

Updated 2026-09-10. Canonical application branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged. This is a documentation-only acceptance update; app source remains v91 `11650313221ae5bf997dbb93b6a905bfdc7da1ed`. Resolve and verify the current documentation HEAD against live GitHub before continuing.

## Physically accepted Natural Voices

Ryan confirmed that natural voices are installed, selectable, demonstrated successfully, and that a normal BOOP reply used the selected natural voice. The Natural Voices gate is now satisfied for the selected canonical rebuild. This is voice-specific acceptance, not blanket acceptance of eyes, HA room switching, remote microphone behaviour or future builds.

Protected checkpoint: `checkpoint-boop-unified-v91-natural-voices-accepted` -> `11650313221ae5bf997dbb93b6a905bfdc7da1ed`.

- versionCode 91; versionName `1.2.91-unified-static-track-state-fix`
- package `com.boop.alpha1`; entry `com.boop.alpha1.UnifiedEntryActivity`
- permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`
- artifact ZIP SHA256 `e4f2fb47e2a3b5db2d338512b44ebdbfa840a42b03acc99a5bce02243f8c820c`
- Unified workflow `34433115316`: success; artifact `10135283428`; 157 Unified and 58 Shield focused tests passed
- Shield HOME workflow `34433115318`: success

The exact APK was independently downloaded and checksum/receipt-verified, supplied as local `BOOP-v91.apk` and Desktop `TEST.apk`, and uploaded to the user-requested `/apk/TEST.apk` location through FileZilla. Pixel emulator launch was inspected separately; Ryan's device confirmation is the physical voice evidence.

## Active selected rebuild

Ryan selected overhaul items **1, 3, 6, 7, 8 and 10**. Development belongs to **`boop-canonical-rebuild`**, based on `99474d141e7affad17cdbe854e94dd3986076980`, worktree `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-canonical-rebuild`. Read that branch's live handoff, status, memory, plan and source provenance for candidate details. Initial candidate source `a3eb768641e339ed59a6e2e86cb74f64bccf5979`; build `34436377162` was running when this pointer was recorded. Do not infer its final result from this snapshot.

Scope: shared state; dynamic configured room and exposed generic HA discovery; one media corner/Home Now Playing owner; Deezer/local transport; HA Back escape; device profiles. Eyes are handled in another task for later transplant; no eyes/blink or Turbo redesign belongs to this selection. Existing Shield Home/Now Playing is reused as an internal library in the rebuild under this new explicit scope; its standalone branch/package stays preserved. This supersedes the older separation restriction only for that integration candidate, not by merging or replacing canonical without verification.

Use GitHub for nonvisual compilation, functional checks and signing. No GitHub visual/appearance tests. Inspect UI locally in emulators and ask Ryan for device/provider/HA acceptance. No new microphone pipeline, no provider secrets, and no automatic OS HOME/accessibility/notification-access changes.

## Historical rollback and failure provenance

Keep v88 checkpoint `checkpoint-boop-unified-v88-android-voice-restored` at `f5f086fc4f67712b5746be067aff852331299bb0` unchanged. v88 restored ordinary Android TTS and isolated failed natural attempts. v89 failed with E890 because a preflight required two absent Kokoro files. v90 removed those requirements but exposed E893 Android audio-output failure. v91 fixed static-track playback state and is now the accepted natural-voice baseline. Detailed historical evidence remains in git history; do not resurrect its former acceptance gate.
