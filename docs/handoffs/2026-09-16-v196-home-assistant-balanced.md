# BOOP v196 Home assistant balanced placement

- Owner branch: `boop-hand-colour-v191`
- Source/build commit: `07f3d122fcd2beca910ef73814871a6654b1b753`
- GitHub Actions run: `35066116181` SUCCESS
- Artifact: `BOOP-Unified-v196-Balanced-Home-Assistant`
- Package: `com.boop.alpha1` version 196 / `1.2.196-home-assistant-balanced`
- APK SHA-256: `A90F4167D59F2DC799692C3617DFDC28B813F828F75D1C82A8CC95DA086D16B7`
- Shield install: SUCCESS, pulled installed APK hash matched artifact; accessibility services unchanged

## Scope

v194 established the larger 360x220dp idle Home BOOP above Shield Settings. Ryan then asked to move that same assistant upward so the black space above and below was balanced. v195 raised the assistant bottom margin from 8dp to 24dp; a fresh real-Shield frame showed improvement but it remained slightly low. v196 raises that margin to 32dp. Size, right alignment, Shield Settings position, Close media behavior, media ownership, artwork and all accepted character/media features remain unchanged.

## Verification

The inherited non-visual preservation suite, materialization checks, Android build, permanent signer check, APK identity check and artifact upload all passed in run 35066116181. v196 was installed on Shield only and the installed APK was pulled back and hash-compared to the artifact. A fresh Shield Home screenshot was captured privately for joint review. Per BOOP rules, screenshot appearance is not an automated acceptance test and is not published to the public repository. Ryan's visual verdict remains pending.
