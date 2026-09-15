# v179 stage fibres — 15 September 2026

Owner: `boop-stage-fibres-v179`. Built source: `e8b7ed4045c0ed7c1eb06b9f2a02da0a5ddf8c16`.

## Change
v178 fibres became subpixel at display scale and their roots were hidden behind the eyes. v179 uses soft screen-width ribbons, longer loose ends and visible crown wisps, masked away from exposed whites and irises. The accepted core stage-lighting shader and Appearance live colour preview are byte-identical to v178. Original artwork and shared hue behavior are preserved.

## Verification
GitHub run [34931180380](https://github.com/ryankemble2006-web/boop/actions/runs/34931180380) passed. Four new v179 checks, five v178 checks, five v177 checks and inherited shared-colour and application checks passed; two historical freeze checks remain skipped. Real-master geometry: 10584 vertices, including 2931 outside-alpha and 5538 surface vertices, with width checks across four scales. Independent review caught an incorrect ink stride before installation; final source fixes all seven-float records to 28-byte stride and adds regression checks. Reviewer confirmed no remaining source blocker. No emulator or GitHub visual tests.

## Signed artifact and installation
Artifact 10382145686, BOOP-Unified-v179-Stage-Fibres.
Package `com.boop.alpha1`, version179 / `1.2.179-stage-fibres`.
APK SHA256: `13a94cebd7c69af6c51c052e04b99e70cf012191d07871f89465d93eb6df53e3`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Installed on Shield and Pixel7Pro; installed package/version/hash match the signed artifact on both. Shield preference hashes16/16 unchanged; phone10/11 unchanged, with only notification bookkeeping changed. No manual settings or permissions change. v178 signed rollback retained.

## Visual status and next work
Private Shield capture shows more visible lit rim/crown fibres; this is an assistant observation, not user physical acceptance. Pixel MainActivity starts in its existing idle-black state; renderer-ready logged, but a visible v179 phone-face capture was not obtained. Direct opening of the internal Appearance activity was refused because it is not exported; no permission workaround attempted. Ryan's v179 visual verdict remains pending. v178 preview placement and immediate hue response remain accepted; v177 lighting remains user-liked. Five-digit sign grip and fake notification diagnostic remain subsequent work, not part of v179.
