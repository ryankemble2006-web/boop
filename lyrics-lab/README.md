# BOOP Lyrics Lab

Side-by-side real-Deezer lyrics package requested by Ryan after concurrent Unified work collided.
Package: `com.boop.lyricslab`. Launcher name: **BOOP Lyrics Lab**.
Branch: `boop-lyrics-lab-side-by-side-v157`, forked from `9bb64285d3a3fb8d3cd1f4890931d7afdf74dfb9`.

The shared production renderer, timed-lyrics client/parser/loader and artwork resolver are copied byte-for-byte during materialization. Do not redesign the emulator-approved presentation. The lab adds only its independent application identity, foreground native-Deezer session observer and an explicit Android music-access setup action.

This is not a replacement launcher or a full second Unified install. There is no HOME filter, BOOP authentication callback, overlay, microphone, startup cleanup, Home Assistant connection or shared UID. Existing BOOP app data and permissions are not imported or modified. The lab needs its own Android notification-listener access to observe media sessions; the app only opens Android's consent screen and never grants that access itself. It does not read notification bodies. Do not grant access silently during physical-device testing.

Build on GitHub with `.github/workflows/lyrics-lab.yml`. The permanent BOOP signer is unchanged. The normal Unified signing/build workflow is not modified or dispatched for this package. Runtime test only on the dedicated lyrics TV emulator, then install only this package on Shield. Never install `com.boop.alpha1` or the synthetic preview package in this task.

No merge is authorized yet. Keep lab-specific files separate from the later merge of the production native-lyrics feature. API catalogue availability, synchronization on actual playback and physical acceptance remain distinct from packaging tests.
