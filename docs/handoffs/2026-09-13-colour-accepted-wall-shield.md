# Colour runtime evidence and Wall-to-Shield acceptance

2026-09-13. This is the current acceptance addendum to `2026-09-13-colour-failure-continuation.md`. Preserve that original handover in full for its research, exact commits and scope, but its failure/unsigned-off status is superseded by the evidence below and Ryan's latest feedback.

## User acceptance

Ryan: "it works btw, i just tested eye colour :) from wall to shield".

Record Wall -> Shield as user-confirmed physical success. Do not manufacture approval of speed, every notification/Lab surface or every reconnect scenario. Do not repeatedly seek reapproval for this accepted route or the older accepted v156 Shield polish.

## Verified source and builds

Live feature branch before this documentation update: `boop-unified-eye-sync-safe-v159` at `0492fc57431474d9c89c9d536ebc12f935bd8238`. Underlying app source remains `d149cb509ec376779daf84c50f621d8adcbacd24`, version `160 / 1.2.160-colour-animation-speed`.

The v160 appearance run `34760362361`, timing run `34760362356` and full build `34760362417` were rechecked and successful, including existing preservation and permanent-signing gates. Candidate artifact is `10318393790` / `BOOP-Unified`. No new source, tests, build, signer or deployment was introduced by this diagnostic continuation.

Physical Shield and Pixel 7 were verified on `159 / 1.2.159-shared-eye-colour`, source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, build `34757337845`, artifact `10317198102`. Both installed APK hashes matched `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`.

The existing main phone and TV emulators were found already running v160, both with APK SHA256 `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`. They were not upgraded by this continuation. The phone emulator was restarted after the remote session interruption, using the existing AVD without wiping it. Do not attribute an unknown earlier emulator installation to this diagnostic work.

## Actual visible tests

1. Phone emulator, Wall: the visible settings control changed hue 225 to 122. Opening/waking the existing Wall showed green irises instead of blue/violet. Restoring 225 through the UI restored both the saved value and the blue/violet irises. This was a real rendered result, not just a label.
2. Shield, Now Playing local isolation: sharing was temporarily disabled. Hue 80 -> 260 -> 80 produced yellow-green -> purple -> yellow-green eyes in the existing mascot bay. Sharing was then restored using the normal UI. No artwork replacement or second face was added.
3. Pixel 7, Wall: initially rendered its saved hue 48. Its sharing preference was initially absent/off. Explicit normal-UI opt-in joined the existing HA value 80. The Wall visibly rendered yellow-green irises. No credentials were copied, lock bypassed or permissions granted.
4. Shield -> Pixel 7: with sharing enabled on both, changing Shield to 260 updated the Pixel 7 preference and visible Wall irises to purple. Captures show the sending Shield Now Playing eyes and receiving Pixel 7 eyes. Pixel 7 was awake during the recorded receipt.
5. Pixel 7 -> Shield: changing Pixel 7 to 122 updated the Shield preference and visible Now Playing irises to green. Captures show both physical surfaces.
6. Ryan independently reported his successful Wall -> Shield test. A subsequent read-only package/preference receipt showed v159, sharing enabled and hue 2 on both physical devices. That is newer user state, not a value to overwrite with earlier test fixtures.

The controlled reverse-direction captures were inspected when saving this acceptance. Preserve the distinction between these inspected physical captures and Ryan's own acceptance statement.

## What was and was not diagnosed

The tested same-device renderer paths and live cross-device delivery worked without a source repair. An initially off Pixel 7 sharing setting was observed and enabled with explicit app UI confirmation. Do not assert that this definitively explains the earlier unspecific failure report. No reproduced code defect or code fix is claimed.

Some early screenshots were black because Wall was idle; a tap woke the canonical eyes. Later Pixel 7 UI inspection encountered a dozing display. A wake key followed by lock/BOOP-foreground checks allowed normal app navigation without unlocking or changing device security. Do not mislabel dozing as a confirmed lock failure.

The preceding v159 handover already recorded Shield HA publish/read/rejoin, including accepting stored shared state over a disconnected local edit. This continuation verified both live delivery directions but did not complete a deliberate two-device offline/reconnect cycle. Retain that as outstanding coverage, not an excuse to call accepted live colour delivery broken.

## Current device state and preservation

Phone-emulator original hue 225 was restored. Shield's isolated local test was restored before the two-device tests. The later shared colour was changed again during Ryan's own testing; last read-only receipt is hue 2 on both physical devices, sharing on. Preserve that newer state, and rediscover it before any future fixture test. Do not blindly restore old 48/80/122/260 values or switch Pixel 7 sharing off.

No physical Pixel 10 commands, app changes, permission changes, data clears, uninstalls, signing changes, global physical animation-scale changes or unrelated app installs were made. No BOOP code or approved artwork was edited. Runtime terminal input sequences were ended after the user's confirmation; no background test sequence is queued.

The local owning worktree remains dirty with historical recovery work; it was left untouched and is not live source authority. Private captures/receipts are under `.worktrees/boop-unified-eye-sync-safe-v159/work/colour-failure-20260913-primary/`. Useful capture stems: `phone-wall-change1`, `phone-wall-h225-returned`, `shield-local-h260-visible`, `shield-restored-h80-visible`, `p7-joined-wall-h80`, `shield-shared-h260`, `p7-received-shield-h260`, `p7-sent-shared-h122`, `shield-received-p7-h122`. Some earlier filenames optimistically said restored before readback succeeded; use the actual inspected results above, not filenames alone. Never publish the private screenshots, raw logs or device addresses.

## Next work

The user's reported colour blocker is superseded by acceptance. Retain the small outstanding reconnect coverage separately. Resume from the existing v160 speed candidate for runtime validation, not a new implementation or old recovery draft. Check .5/1/1.5/2, exact original 1x, mid-clip changes, lifecycle pause/resume, sleep-hide timing and synchronized notification hands/eyes on local emulators first. Keep physical speed deployment held until those gates pass.

Existing authored animation, approved masters/shaders, Wall hue controls and accepted Shield polish remain protected. Current-session instructions remain primary; no unrelated native-lyrics/Johnny merges, permission changes, lock bypass, new signing keys or regenerated artwork.
