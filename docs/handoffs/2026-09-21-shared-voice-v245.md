# Shared voice in Voice Settings — Shield245 / Wall212

## Current: shared voice enabled on Shield245 / Pixel7 Wall212

Ryan accepted the immediate natural previews, then requested sharing voice across devices like colours, with the phone's current voice as the starting point. Both devices now share Emma, pitch1.45 and cadence1.25. Voice Settings exposes the same sharing flag/runtime as Build a Boop, beneath the natural voice buttons. The open sliders, effective profile and Selected label follow remote updates; active touch drags and transient preview messages are preserved. Sharing uses the existing paired Home Assistant, works while BOOP is open, retains the last profile offline and catches up on reopening. Natural voice downloads remain local.

Final signed source `3a795bb334734aff81c497e6e320f6566513c0f4`; successful run `35544738329`; artifact `10616216941` (`BOOP-Shield-v245-Wall-v212-Signed`).131 focused local checks passed, with the final label correction covered by a failing-then-passing regression and15 focused checks;15 speech lifecycle scenarios passed. The complete final inherited/integration/HA/signed CI passed. Independent review found no remaining blocking issues.

Both final APKs installed with data preserved; their on-device APK SHA256s match the independently verified artifacts. All voice/appearance preferences survived both installs. All29 assets and16 native libraries per APK are byte-identical to244/211. Final APKs and receipt are retained in task outputs and Desktop/APKBOOP;244/211 rollbacks remain.

Live tests verified phone pitch -> Shield, Shield DPAD cadence -> phone, voice selection in both directions, and background/reopen catch-up. The initial candidate revealed a stale secondary Selected label; the final installed correction passed actual bidirectional label checks. Final device traces show Shield preview starts17-23ms and phone23-79ms, including TEST VOICE using a voice selected on the other device, with no synthesis or AndroidRuntime errors in those traces. Both final labels/profiles read back Emma1.45/1.25 with sharing connected. These are installed-device/UI/AudioTrack checks; Ryan's physical acceptance of sharing is not inferred. Temporary device XML was removed. Details: `docs/handoffs/2026-09-21-shared-voice-v245.md`.

## Delivery

- Owner branch: `boop-shield-weather-focus-v221`; candidate: `astra-music-resolver-v242`.
- Signed build: https://github.com/ryankemble2006-web/boop/actions/runs/35544738329
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Selected backend, natural speaker, pitch and rate use the existing shared helper. Wake name, Android voice identity, local pack verification and voice model files are not transferred.
- The phone's current profile was retained as the initial shared profile. No private device addresses, auth material, raw logs or screenshots are published.

- `BOOP-Wall-v212.apk`: 161179785 bytes, SHA256 `c19d7a7d525db0edf14c310c0465322cdcb95dade8cc43d9184dc58a9abe23e4`.
- `BOOP-Shield-v245.apk`: 161179901 bytes, SHA256 `c8f7fc46ee76f571a8687a238e41d8ca7ab80446c697a6d14c6fef2168500bdb`.
