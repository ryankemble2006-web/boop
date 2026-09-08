# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener: physical PASS
- v0.10.3 collision layout: physical **much better**
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: physical **PASS**
- v0.10.6 focus outline: CI/signer green, physical judgement pending
- Current candidate: same-version code 22 / `0.10.7-puppet-bay` **fill refresh**
- Build source: `85a3278d52a4c359a58716644b949924abaf961b`
- Workflow: `34290757484` SUCCESS
- Artifact ID: `10081272677`
- APK SHA-256: `f9509a3af47b4e50eac79fb96b173eba10216a625be1bf601a8bf148d4da7d21`
- Artifact ZIP SHA-256: `a01b88140192e4c6db534ab19cb4e54965c46f569152ab521fc27b43f34318eb`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; visual size/placement pending Ryan

## Fill refresh

Ryan reported v0.10.7 correctly put BOOP inside the Now Playing bay, but he remained too small. The old puppet renderer deliberately used only 88% of stage width and 75% of stage height.

Current visual-only change:
- puppet now occupies the full stage bounds;
- `FIT_CENTER` preserves artwork proportions and avoids cropping;
- stage size, clipping, controls, focus treatment, album-art path and animation policy remain unchanged;
- no GPU/OpenGL/skeletal renderer was added.

This refresh intentionally keeps version code 22 / `0.10.7-puppet-bay`. A temporary code-23 bump was reverted because the connected GitHub write safety layer blocked editing the existing signing-workflow verifier. The final branch therefore stays on the known-good signed code-22 verifier lane rather than leaving a mismatched release configuration.

## Fast CI rule

Ryan owns real-device visual acceptance. CI sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, signed assembly, exact package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## FTP handoff

Verified APKs should also be uploaded to Ryan's private FTP `/apk/` folder when the runtime can reach it. Credentials are private and must never enter this public repo.

The fill-refresh FTP attempt failed with connection refused on TCP/21 before authentication. **No FTP upload occurred.**

## Next gate

Install the v0.10.7 fill-refresh APK and physically judge BOOP's size/perch inside the right-hand Now Playing bay. Confirm clipping and remote navigation remain unchanged. Minor sizing/placement tweaks can continue after use. Defer full Tegra/GPU puppetry until the stage placement is worth locking.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
