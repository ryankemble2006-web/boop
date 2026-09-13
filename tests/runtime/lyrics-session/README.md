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
