## 2026-09-10 Deezer compatibility patch, v23 candidate

During approved native Deezer control work, Android UI hierarchy reads reconnected
ShieldHomeOverrideService. Its unconditional onServiceConnected launch brought
Home over Deezer even though Ryan was not using the remote. Logs and source agree.
The patch rearms Home once per device boot, using Android boot count and private
preferences. Real stock-Home window events still use the existing override path.
No permission, screen-content access, default Home, artwork or media changes.

Version 23 / 0.10.8-service-reconnect is a compatibility candidate on the existing
standalone lineage, not a new app. Focused policy tests cover same-boot reconnect,
new boot, first activation and unavailable boot count. GitHub performs only the
focused functional test, build, signer and integrity checks; no visual tests.
Signed source: ccb10658bded07dbf2a91ee8234509999f960a49. GitHub run
34446163444 SUCCESS; artifact 10139755652. ZIP SHA256
9643be98a41ccb6c4173bdf8e52994fbc46239029ebac0189aa8deb05d75537d.
APK SHA256 6bcc46633c38f61f4c7c5b4b0a49a4d3eec7b05818a25ef29abd4d36e82f411e.
Permanent signer independently verified after download. Two focused policy tests pass.
Ryan explicitly approved installing this exact v23 update; install succeeded after
preserving the actual installed v22 APK privately for rollback. Repeated hierarchy
reads now leave Deezer in front. Native track, Flow and artist switching component
tests proceeded successfully after the fix, with agent-inspected playing indicators.
This is scoped device evidence; Ryan's acoustic/end-to-end acceptance remains separate.
Reboot and single/double Home physical acceptance remain pending.

# BOOP Shield clean launcher status

Updated 2026-09-09. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener: physical PASS
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: physical **PASS**
- v0.10.6 focus outline: CI/signer green, physical judgement pending
- +10% puppet size retained
- Playback dance: physically reported **"looks awesome"**
- Paused upset/sulk: implemented; explicit physical acceptance not separately recorded
- Exact approved eye master: permanently locked, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`
- Latest physical report: layered blink otherwise good, but duplicate static upper eyelid visible from old headphones artwork
- Current candidate: code 22 / `0.10.7-puppet-bay`, legacy headphone-lid arcs masked, approved master + top-only puppet blink unchanged
- Exact APK source: `71828a49239fd41f2b7dd81fdfb6a758c4839d22`
- Workflow: `34299712206` SUCCESS
- Artifact ID: `10084479659`
- APK SHA-256: `62886af8b7bac55bebb019555aba3f3b08bc0f060ae53780133b15b8a20f4bf0`
- Artifact ZIP SHA-256: `be286e1161df93705d065b0ff8cd790b0997c1662effa79b7b3b6f8e963190d4`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package: GREEN
- Visual checks: DISABLED by design; single-eyelid physical acceptance pending Ryan

## Permanent eye lock

Ryan re-confirmed the exact 1774 x 887 approved PNG and locked it as BOOP forever, infinitely poseable. The build hash-checks the master before functional tests. The pair is rendered as one undistorted bitmap and its source spacing/offset must never be normalised or redrawn.

## Blink baseline

- Layer order: headphones -> exact approved eye master -> animated eyelid.
- Top black curved animated lid only.
- No animated bottom lid.
- Existing 183 ms blink curve retained.
- Random interval remains 3-7 seconds.
- Double-blink chance remains 18% with 110 ms gap.
- Dance, paused upset motion, acknowledgement hop, +10% size, clipped bay, focus behavior and media plumbing unchanged.
- Lifecycle/animator/Power Saver safeguards unchanged.

## Duplicate legacy eyelid correction

Ryan's Shield video showed the second upper lid even when the blink was open. Root cause: the old `boop_headphones.png` raster already had upper eyelid/brow arcs baked into it.

Latest repair:
- legacy headphones bytes retained unchanged as `boop_headphones_legacy.png`;
- `boop_headphones` is now a wrapper drawable that black-masks only the two obsolete upper-lid arcs;
- direct unmasked `boop_headphones.png` is no longer selectable;
- approved eye master, animated top lid and all blink timing remain unchanged;
- no launcher/media layout or behavior code changed.

TDD RED workflow `34299420096`: 82 tests, exactly one expected failure for the missing mask resource; build/sign/upload skipped.

GREEN source `71828a49239fd41f2b7dd81fdfb6a758c4839d22`, workflow `34299712206`: focused Shield HOME tests green with `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`, exact master hash gate green, signed assembly green, package/version/manifest/service/resource/signer/archive checks green, artifact uploaded.

Scope compare from pre-correction `c5a1bc620484238536a22fbdae8b304d23566b52` to green source shows only the headphones PNG rename, new masking drawable and one focused regression test.

## Next gate

Install the current signed candidate on the Shield and physically confirm exactly one approved upper lid per eye at rest/open, then confirm the existing top-only blink closes/reopens cleanly. Everything else was intentionally left alone.

Real Shield behavior is visual authority. Do not merge into unified or expand into full Tegra/GPU puppetry until Ryan explicitly approves.
