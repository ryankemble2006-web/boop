# Natural music selection: Shield242 / Wall209

## Scope and implementation

Continued from bd803312 on boop-shield-weather-focus-v221. Ryan approved autonomous laptop/ADB implementation and installation on Shield and Pixel7 Pro, and explicitly chose asking when artist and song collide. No RCD. Work isolated on astra-music-resolver-v242; owning branch fast-forward only after verification.

The shared catalogue handles artist/title in either order, possessives, explicit by, and explicit artist/band/song/track prefixes. Original titles remain matchable; omitted remaster/Ultimate Mix suffixes are handled conservatively. Live/demo/karaoke/arbitrary remixes remain distinct. Composite matches take precedence over misleading literal titles. Performer IDs are checked against name-specific artist results. Bare duplicate artist names require agreement across multiple distinct songs. No performer-versus-title majority suppresses a song collision.

Ambiguity returns a local question before HA target discovery/playback. One retained client carries choices for60seconds, scoped to server, stable HA registration and room. Answers choose artist/song or a named choice. Cancellation, unrelated input and activity pause clear choices; each answer is consumed once. Network work runs outside the UI-state lock; generations reject stale results. Normal access-token refresh preserves the stable connection identity. LOCAL_QUESTION uses the existing spoken follow-up window. Diagnostics contain stage labels only. Native target/hardware/room guards, nonce receipts and no-replay behavior are preserved.

## Verification and review

The initial production-source regression reproduced10 failures. Review caught UI monitor blocking, duplicate-name collision ordering, explicit remaster matching and query-dependent cache suppression; each gained a failing regression and correction. A further actual-auth review caught token-refresh invalidation and gained a regression. Final local probes pass97 music-selection assertions and22 native receipt-race assertions.116 focused checks passed before the final connection-scope fixture extension; that extension passed separately.

Signed workflow35541894553 passed the full inherited checks, speech lifecycle tests, split/materialized integration, HA room tests, both Android builds and package verification. Earlier35541651932 failed because its extracted speech lifecycle harness lacked the new cancellation collaborator; the stub and pause assertion were fixed. Superseded35541608057 and35541787954 were cancelled. No GitHub visual acceptance was used.

Desktop and phone public catalogue responses differ. Old installed Wall208 actually returned Done for play John Lennon Imagine on Deezer, while the Shield MediaSession reported John Lennon Imagine by Crazy Chauffeur. Updated209 returned Done and the Shield reported Imagine (Remastered2010), artist John Lennon, playing state3. Replaying the exact phone catalogue also selected track7193834/artist226.

Installed phone tests: Queen -> artist/song question -> the artist -> Don't Stop Me Now by Queen; Britney Spears -> question -> the artist -> ...Baby One More Time by Britney Spears. Bare John Lennon -> question; cancel left the existing Lennon track unchanged. The artist answer succeeded across actual HA token refresh.

Installed Shield tests: John Lennon Imagine -> Imagine (Remastered2010) by John Lennon; Queen -> question -> Queen playback; Britney Spears -> question -> Britney playback. Every playback result was checked against Deezer's actual MediaSession metadata and playing state, not just Done.

Tests used a temporary private app-process probe invoking the installed HomeAssistantClient with existing encrypted pairing. Its context required an application-context wrapper; this was diagnostic tooling only. No credentials left the apps. Temporary helpers were removed after testing. Device tests changed playback. The final test left Britney Spears playing.

## Build and install receipt

Source:54c8a2e14f59bc25be918fa2bedcadaae7170d63.
Run:https://github.com/ryankemble2006-web/boop/actions/runs/35541894553
Artifact10615640493: BOOP-Shield-v242-Wall-v209-Signed.
Permanent signer:f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

- BOOP-Wall-v209.apk: 160583929 bytes; SHA256 8fcd20cdb9fa498c2dff432f4ae5a6aeb998366ffc3b9c494616b258e1851763.
- BOOP-Shield-v242.apk: 160584045 bytes; SHA256 14735d84137b23711e3106efbdafc95686232cd934c29c9363c3e77f9ed1b897.

Independent cryptographic signature, ZIP CRC, hash and size checks passed. All18 assets and16 native libraries in each APK exactly match the prior241/208 rollback. Both installed with adb install -r; data retained and package versions242/1.2.242-shield and209/1.2.209-wall read back. Existing pairing/registration remained usable. Copies saved to task outputs and Desktop/APKBOOP;241/208 rollback retained. Pixel10 untouched.

## Acceptance limits

These tests inject text into installed handlers. They do not prove microphone transcription, audibility, TTS timing or physical spoken-answer recognition. LOCAL_QUESTION is wired to the existing follow-up window and tested at source/lifecycle boundaries; Ryan's real spoken test remains the acoustic acceptance authority. Try play John Lennon Imagine, then an artist-name collision and answer the artist. No album-intent expansion, heart/layout/artwork/settings/voice-model/permission changes. Current catalogue results can change; resolution asks/fails conservatively. Raw diagnostics/screenshots stay private.
