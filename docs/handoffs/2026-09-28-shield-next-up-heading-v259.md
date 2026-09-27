# Shield259: remove the Next Up heading

Ryan requested removal of the launcher's “Seren Next Up” text. The fixed title view is removed; the existing poster positions, focused episode detail, row sizing and controls remain unchanged. Shield version increases from258 to259. The audio switching source is unchanged.

During the same task, physical Shield logs showed Kodi and Deezer repeatedly invalidating/recreating competing direct AudioTracks (float PCM versus 16-bit PCM) while Kodi was in the Android background. Kodi retained the default one-minute idle audio keepalive. The live setting audiooutput.streamsilence was changed from1 to0 (Off) using Kodi JSON-RPC, with readback verified. Other Kodi audio settings and BOOP's native music/video rate switching remain unchanged; Kodi stays alive for Next Up. Logs/settings rollback evidence is retained in the calling task's work/audio-handoff directory. Repeated transition and physical acoustic acceptance are pending; do not treat the setting readback as proof of a resolved stutter.

Source is based on the installed Shield258 owner commit14e832fc. Build/install results will be recorded after verification.
