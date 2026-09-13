# Local real-session regression fixture

This is TEST-ONLY source, not part of BOOP Lyrics Lab or Unified. It uses a real
Android MediaSession published from a clearly labelled emulator fixture with the
`deezer.android.app` test identity. This is necessary to exercise the unchanged
production package filter. It contains no Deezer code, credentials, network or
audio playback. Every executable entry refuses non-emulator hardware.

Install only on the dedicated `BOOP_Lyrics_157_8186447` AVD after confirming no real
Deezer app is present. Never install this APK on a Shield or phone. Existing
`com.boop.lyricslab` is the separately built, unchanged production artifact.

The instrumentation runs inside that production lab process under the existing
signer. It seeds short invented documents into the existing memory cache using
test-only reflection, then exercises real Binder metadata/state callbacks, the
actual activity, controls, document replacement and timing. This isolates the
refresh failure from external catalogue/network variability. It is not a test of
Deezer audio, the real API catalogue or real Shield firmware.

GitHub builds/signs this helper only. It never launches an emulator, captures an
image or runs this instrumentation. Execution and any inspection remain local.

## Local run procedure

1. Verify the serial resolves to the named dedicated emulator, and verify both
   APK source receipts, package names, signer and hashes. Never use an untargeted
   `adb install` or overwrite an existing real Deezer package.
2. Install the desired production lab APK and this emulator-only fixture on that
   AVD. Enable the lab's own notification access through Android Settings there.
3. Close the consent screen. Start `deezer.android.app/com.boop.lyricstest.PlayerActivity`
   to establish the synthetic session. It requests no audio focus or credentials.
4. Ensure the lab, not Settings, is the front activity. On this disposable local
   test task only, the following establishes a clean task and remote input mode:

```text
adb -s emulator-5574 shell am start -W -n com.boop.lyricslab/com.boop.shieldhome.LyricsLabActivity -f 0x10008000
adb -s emulator-5574 shell input keyevent 20
adb -s emulator-5574 shell am instrument -w -r deezer.android.app/com.boop.lyricstest.LyricsRuntimeTest
```

The first command does not clear app data. It prevents `startActivitySync` waiting
on a task with the setup Settings screen still on top. The D-pad event exits touch
mode left by tap-based consent, so remote-focus checks exercise remote behavior.
Use an external 90-second timeout when automating these commands; if setup stalls,
stop only the owned emulator lab process, preserve the logs and diagnose it.

Require `checksPassed=38`, `INSTRUMENTATION_CODE: -1`, and no failure result. Shell
exit 0 alone is not a pass. Do not relax assertions to accommodate a failure.
The old v157 reproduces the refresh loss after Previous; fixed v158 passed all
38 checks twice. See `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md`.

After testing, verify the fixture's installed test version/hash again and remove
ONLY this temporary fixture from the named AVD. Preserve the production lab and
its data. Do not leave a synthetic provider masquerading as a real Deezer install.
