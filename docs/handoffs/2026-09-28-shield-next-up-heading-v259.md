# Shield259: native Deezer resume and Next Up heading removal

Ryan requested removal of the launcher's “Seren Next Up” text. The fixed title view is removed; the existing poster positions, focused episode detail, row sizing and controls remain unchanged. Shield version increases from258 to259.

Physical Shield logs showed Kodi and Deezer repeatedly invalidating/recreating competing direct AudioTracks (float PCM versus 16-bit PCM) while Kodi was in the Android background. Kodi retained the default one-minute idle audio keepalive. The live setting audiooutput.streamsilence was changed from1 to0 (Off) using Kodi JSON-RPC, with readback verified. The same single setting was persisted in guisettings.xml after a fresh backup; all other saved setting values matched. Kodi stays alive for Next Up. Logs/settings rollback evidence is retained in the calling task's work/audio-handoff directory.

Ryan then physically tested Kodi Liquid -> Home -> manual Deezer unpause and reported clean playback without a skip. This establishes one clean cycle, not repeated acoustic acceptance. His amp still showed48kHz, matching Android's48000Hz mixer with a44100Hz Deezer source. This is a separate regression from the previous native44.1kHz result.

The launcher previously enabled native mode only on Deezer app launch, and its live media observer recognised only Cast. Home resume now prepares native mode before the selected Deezer play command; selected playing Deezer callbacks also reclaim native mode after Home. Paused sessions do not alter the mode, foreground video priority remains, and Cast retains its existing content classification. Home re-evaluates the live session rather than replaying a cached mode from before Kodi. No auto-resume, track skip, Kodi force-stop or other audio preference change is added.

The controller regression test failed on native mode missing before play, then passed after the fix.12 focused tests pass, including legacy Cast/launch policy, ordered native resume, paused/unknown session protection, platform rejection, rate re-read, and Seren/weather checks. Signed CI includes the new audio tests. Installation and renewed physical44.1kHz acceptance are pending.

Source is based on the installed Shield258 owner commit14e832fc. Build/install results will be recorded after verification.
