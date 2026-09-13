# Unified v164: signed music-bounce candidate ready for Ryan

Updated 2026-09-13. Task owner `boop-unified-v164-music-bounce`. Base is accepted v162 at `112d09b5b446d6582954a6d89b3700fe16298ecb`, not v161 Music Lab or v163. The accepted v162 branch was rechecked unchanged. No merge or installation occurred in this task.

**Implemented:** real Android Visualizer output-mix waveform sampling, loudness-to-height envelope and whole-puppet GL offset in Now Playing. Blink speed is still controlled by the unchanged canonical animation-speed binding. Sampling has visibility/playing/permission gates and releases off the UI thread. No physical-microphone recording, beat inference, synthetic bounce, audio focus or playback changes. Conditional permission entry is included and requests only through Android's user-operated flow.

**Preserved in source:** native Lyrics screen, Lyrics-button callback, track/Skip observer, accepted footer, media manager, launcher controls, startup features, voice, approved artwork, shared renderer/shaders and existing animation engine. Manifest change adds one private permission Activity without losing ShieldLyricsActivity. Version/config change is limited to the two version fields.

**Signed candidate:** `com.boop.alpha1`, `164 / 1.2.164-music-bounce`; source `f9f65569250b9dc02602101ef4d56195824e0380`; run `34778178916`, SUCCESS; artifact `10323694771`. APK SHA256 `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`. Existing permanent signer verified. Downloaded ZIP/APK and packaged lyrics/bounce classes verified in the artifact sandbox.

**Checks:** four new test groups, including 332 executed envelope assertions; native lyrics data/transport/entry/lifecycle checks; six existing animation-speed functions; materialized source/art checks; 10 owner/bay contracts; full signed assembly and actual APK identity/signature/integrity checks passed. Two inherited one-time source freezes skip later versions; the separate new v164 preservation gate passed. Existing nonfatal warnings remain. These are nonvisual checks, not a real Deezer test.

**Not claimed:** installation, physical motion/permission/lyrics acceptance of v164, usable Shield audio data, emulator testing, independent review or full historical test-suite coverage. Ryan reported his own rollback after v163; this task did not touch the installed app. Give him the signed APK to test, not an assertion of hosted visual success. Detailed receipt and continuation boundaries are in SESSION_HANDOFF.md and docs/handoffs/2026-09-13-v164-music-bounce.md.
