# Maximum whimsy — Shield246 / Wall213

## Current: doubled voice range installed on Shield246 / Pixel7 Wall213

Ryan physically accepted shared voice on both devices ("both work brilliantly") and requested double the pitch/cadence maxima for maximum whimsy. Pitch now reaches 2.90x and cadence 2.50x in both settings screens, Android/natural playback clamps and shared-profile validation. Existing minima/defaults and stored numeric settings are unchanged by installation. The existing HA helper marker is retained; both paired devices were upgraded before raising the sliders, since older builds reject values above their previous limits.

Final signed source `b697a00c55f2d2108889c92cc0d43b2da962de79`; successful run `35545251765`; artifact `10616267577` (`BOOP-Shield-v246-Wall-v213-Signed`). 132 focused local checks, 15 speech lifecycle scenarios and complete inherited/integration/HA/signed CI passed. Three new failing-before/passing-after regression paths cover preserved saved values on expanded sliders, immediate playback without clamping, and shared maximum serialization/state convergence with strict bounds. Independent review found no blocking issues.

Both permanent-signed APKs installed in place; on-device APK SHA256s match independently verified artifacts, and versions 246/213 read back. All voice/appearance preferences matched before/after installation. All 29 assets and 16 native libraries per APK match 245/212 byte-for-byte. APKs and receipts are retained in task outputs and Desktop/APKBOOP; 245/212 rollbacks remain.

Actual AudioTrack probes on both devices accepted pitch/rate pairs (2.90,2.50), (2.90,0.70), (0.75,2.50) and (1.45,1.25) with fallback mode FAIL, exact parameter readback and all 38144 sample frames consumed. Installed app tests then set both phone sliders to maximum, confirmed both device profiles/labels at 2.90/2.50, and verified a Shield DPAD pitch change travelled back to the phone before returning to maximum. TEST VOICE at maximum started in 18 ms on phone and 24 ms on Shield. Subsequent named-preview traces remained fast while Ryan actively tuned the phone. His live adjustments were preserved, so the final profile is user-controlled rather than forcibly reset to maximum. These are UI/profile/AudioTrack checks, not inferred acoustic acceptance of the new extremes. Temporary device probe files were removed. Details: `docs/handoffs/2026-09-21-maximum-whimsy-v246.md`.

## Delivery and verification notes

- Owner: `boop-shield-weather-focus-v221`; candidate: `astra-music-resolver-v242`.
- Signed build: https://github.com/ryankemble2006-web/boop/actions/runs/35545251765
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Before the interactive max test, both installations retained the prior Emma1.45/1.25 profile exactly. No preferences migration or forced default was introduced.
- A later phone UI dump exited137 during active user tuning, so that additional automated tap was not counted as a successful scripted action. Profile reads and current-process playback traces remained available; no AndroidRuntime error appeared in the filtered traces. Further screen interaction stopped to preserve the user's live tuning.
- The phone audio probe process was terminated after all four cases completed because Android retained its process; its temporary files were removed. No production code or settings were altered by that probe.
- Playback API reference for the diagnostic FAIL fallback mode: https://developer.android.com/reference/android/media/PlaybackParams

- `BOOP-Wall-v213.apk`: 161179785 bytes; SHA256 `a29ee9b4f74a9e4762803244e9558f10bbb44440a7bfc062a510951f4a03373f`.
- `BOOP-Shield-v246.apk`: 161179901 bytes; SHA256 `81df5a067b7e33c63ceec9325a97d87a19f27046299addd33743dd996d51f545`.
