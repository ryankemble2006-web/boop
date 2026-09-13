# Unified memory: v164 installed for Ryan's music test

Updated 2026-09-13. Task owner: `boop-unified-v164-music-bounce`.

## Latest physical delivery

Ryan explicitly requested installation on the now-free Shield. Exact signed v164 was installed in place with ordinary ADB install -r, Success/exit0. Independent readback verified the installed package/version/hash and unchanged HOME, app UID and first-install timestamp. RECORD_AUDIO was already granted and the Unified media listener already enabled; both remained unchanged. No grants, resets, data clears, uninstalls, explicit launch/playback commands, phones or emulators were involved. No app source edits/builds or branch merges occurred in this continuation.

Preflight actually found `163 / 1.2.163-music-audio-access`, despite the earlier rollback report. This is an observation, not a diagnosis of why163 remained. Do not misreport a162-to164 physical install. The APK source lineage remains accepted v162 directly, not163.

Installed: `com.boop.alpha1`, `164 / 1.2.164-music-bounce`.
Source: `f9f65569250b9dc02602101ef4d56195824e0380`; signed run `34778178916`; artifact `10323694771`.
APK SHA256: `d7ae61fc956dddc064211b7e5b3c5197b1ce4e55800acd558cee4b9e622eb5c8`.
Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Staged hash/source/package/signature and native Lyrics Activity were checked before installation. Installed base.apk matched the staged candidate afterwards. Installation receipt: `docs/handoffs/2026-09-13-v164-shield-install.md`.

## Preserve the confirmed work

Ryan had reported no movement and a broken new Lyrics button on163, then asked to start again from confirmed162. v164 starts directly at `boop-unified-native-lyrics-v162@112d09b5b446d6582954a6d89b3700fe16298ecb`. Do not reuse the v161 Music Lab or treat163 as this source baseline. The164 branch remains unmerged; no accepted-owner/main/lab advance is authorized merely by installation.

v162 native Lyrics and Skip were user-accepted with "perfection". Preserve internal ShieldLyricsActivity, matching button route, track observer, main lyric size and passive half-size bottom-right licence footer. The retired Lyrics Lab must not become a dependency or be reinstalled. v161 speed-on-Shield-and-Pixel7 and automatic two-way colour remain inherited acceptance. Speed is device-local; colour is shared. These are not fresh acceptance of164.

The164 source preserves all existing lyrics files/callback, media manager, launcher route, startup/voice features, dependencies/materializers, artwork/shaders and shared animation engine. Existing input changes were confined to four adapters: version fields, one private Activity line, six settings lines and the Now Playing puppet host. Detailed provenance and the prior full root memory are retained at `677009e53c52df3ec6c92ea75214df5e7c4dba0a` and `docs/handoffs/2026-09-13-v164-music-bounce.md`.

## Locked music behavior

Actual Android Visualizer output-mix loudness drives vertical bounce; saved animation speed independently controls blinks/expressions. No BPM analysis, synthetic beat, per-hit clip restart or physical-microphone fallback. Missing/stale/constant samples must not fabricate music response. MusicBounceSource polls transient levels on a short-lived background worker while visible/playing/permitted; it releases when inactive without changing audio focus, volume or playback. MusicBounceEnvelope removes DC and applies bounded fast-rise/soft-fall motion. MusicBounceRenderer shifts the GL viewport around the unchanged eye renderer, not the eye pose/artwork/shader.

The retained MusicAudioPermissionActivity/Flow and foreground-only once-off offer use explicit Continue/Not now and Android's permission UI. No ADB auto-grants, denial loops or voice callback reuse. Manual entry remains Launcher Settings > Now Playing > Music audio access. The installed Shield already had the necessary grant at this readback.

## Verification boundary and workflow

v164 is INSTALLED, not yet physically accepted. Useful real Deezer levels, visible bounce, runtime permission behavior and runtime lyrics remain for Ryan's joint test. Prior nonvisual checks passed with332 envelope assertions, lyrics data/timing/transport/entry/lifecycle checks, six timing functions, materialized source/art checks,10 owner/bay checks, signed assembly and packaged identity/signature/classes. Two inherited v162-only freezes skip future versions; the new164 baseline guard passed. No full historical-suite rerun, independent reviewer or hosted visual test is claimed, and these tests were not rerun for this installation-only continuation.

Keep development/build/signing/handoffs on GitHub and test behavior with Ryan. No automatic emulator gate or main-phone operation. Physical Pixel10 remains excluded. Keep the working Desktop Commander0.2.47 command unchanged. No local app checkout was modified or claimed synchronized; private staging/readback files remain under laptop Downloads. Preserve concurrent work and user settings; publish sanitized task evidence only. Fetch LIVE task HEAD before continuation and update handoff/status/memory after material results.
