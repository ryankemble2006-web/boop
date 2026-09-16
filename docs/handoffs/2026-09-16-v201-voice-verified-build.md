# v201 Voice surface correction - verified build receipt

Date: 2026-09-16. Owner: `boop-hand-colour-v191`.

## Source and test evidence

Built source: `eff4b004176747bfa489f566a34357b52e8c0557`.
Production correction: `a90fbaa7c8e52f4d015934caafd924a9cf28f694` (five added lines in BoopCanonicalFaceView only).
RED: source `1759fd76516bca357472d6af82cccf37ee47bcff`, focused run `35079882308`, job `104741067108`: one intended failure, four existing passes; exact assertion `voice settings surface: expected 8 but was 0` after Java compilation succeeded.
GREEN: focused run `35080668363` and voice-profile run `35080668438` on `478372242171d9df77e00897a64bad0e3de240e4` succeeded. The first full run `35080668407` stopped only because its changed-file allowlist omitted BoopCanonicalFaceView, after 1,444 music worker/curve checks passed. The explicit two-line allowlist update at built source above changed no music behavior or music assertions.
Final full signed run `35080910395`, job `104744423886`: SUCCESS. Checks include the actual source and materialized visibility regression, exact source/materialized comparison, inherited character/lyrics/music/voice/sharing checks, Android compilation, permanent signer, APK identity/bytecode and artifact upload. No duplicate build or rerun was started by the reviewing session.

## Artifact and independent read-only verification

Artifact `10439409429`: `BOOP-Unified-v201-Voice-Surface-Ownership`.
Artifact ZIP digest: `sha256:57a6b8036c9c2a1570e0cf71fa83aed6d7d92887822d513724f2f20e6a99a7c9`.
APK: `BOOP-Unified-v201-Voice-Surface-Ownership.apk`.
APK SHA-256: `527907407d60787860cf9e1c1cd13b464db91f015f657eec92590f995d735c8e`.
Package/version: `com.boop.alpha1`, 201 / `1.2.201-voice-surface-ownership`.
Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The reviewing recovery downloaded the signed artifact to a separate private temporary folder, recomputed its APK hash, matched built-commit.txt, read actual aapt package/version data, ran apksigner verification, matched the signer receipt, and compared actual permission declarations against the verified v200 APK. All comparisons passed; no permission declarations changed. This is artifact inspection, not a local app build, signing operation or installation.

The installed v200 APK was also independently matched to signed run `35076396821`, source `572e8d6cb4726951df0604e18a9a8e614d93a543`, SHA-256 `bd0b80ab96e858ed12bcaa1c43b132f4b55a43c078630a7cebb80a9db34d5bf5`. That closes the historical source-provenance gap without relying on version labels alone.

## Runtime boundary and next step

Last read-only package check in this recovery still reported v200. This session has not installed v201, pressed TEST VOICE, changed sliders, touched phones or modified permissions/app data. Private device captures, raw dumps and local paths are not published. The independent logic review is `docs/handoffs/2026-09-16-v201-voice-surface-review.md`.

The collaborating session's already-authorized install continuation is `docs/handoffs/2026-09-16-v201-voice-surface-install.md`. Its earlier in-progress build reference is superseded by the successful build above. Coordinate rather than double-install: first re-read current device identity, then follow that handoff and capture the actual Voice page. Do not declare the visual blocker fixed until the Shield display proves it. Preserve the approved artwork and existing voice controls. Audio pitch/rate, readable selectors and phone/Shield profile propagation remain joint human checks.
