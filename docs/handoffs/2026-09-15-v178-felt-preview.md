# v178 loose felt fibres and live colour preview

Updated 2026-09-15. Owner `boop-felt-preview-v178`.
Base `af9ad31ff32869ee02a30a64680d1f217d84c03e`; built source `4f873eadaf65c1fb954aa95e68b5da966263a94d`.

## User direction and change

Ryan physically liked v177 and its stage lighting, but the tiny loose fibres from the approved study were missing. That was an implementation gap: the old shader retained a smooth alpha silhouette. He approved restoring loose lit fibres while keeping the lighting, and adding BOOP to the shared eye-colour menu as a live preview.

- `FeltFringeMesh` derives sparse tapered ribbons from the original upper-lid alpha contour and rig. Neutral fibres extend outside the smooth outline, render behind the original eyes and remain stable between frames. Mesh generation occurs once per GL context.
- The accepted v177 `eyes.frag` and original eye/hand artwork are byte-identical. Fringe uses the same placement scale and premultiplied blend. Motion, voice, media and colour-sharing transport are unchanged.
- `BoopAppearanceActivity` now has one fixed BOOP preview above its scrolling controls. Existing hue binding updates it from local edits and incoming shared colour. It renders on change, pauses/resumes with the activity and does not take keyboard focus. The preview is open-eyed; it does not introduce a second animation loop.

## Checks and review

Test-first run34929866191 failed for missing mesh/shaders/preview. Run34929959566 passed initial new checks and was superseded by the final workflow check additions.

Final [GitHub run34930044029](https://github.com/ryankemble2006-web/boop/actions/runs/34930044029) SUCCESS:
- Five new geometry/shader/preview/isolation tests and five retained material checks passed.
- Real master generated6072 triangle vertices,3801 outside the original alpha; checks confirm deterministic, bounded geometry on both lids and no distant floating specks.
- Five existing colour-sharing/state/menu tests passed. Reviewed preview/mesh/shader files match the materialized app, and both new shader files plus mesh bytecode are in the actual APK.
- Inherited voice/music/HA/lyrics/timing/face checks passed. Two historical one-time source-freeze tests remain skipped.
- Independent source review found no critical/important issue. Final follow-up changes only added workflow checks. No local build, emulator or hosted visual testing.

Source tests are not a physical judgement of fibre visibility, softness or menu usability.

## Signed package and installation

Artifact `10381481974`, `BOOP-Unified-v178-Felt-Preview`.
Package `com.boop.alpha1`, version `178 / 1.2.178-felt-preview`.
APK SHA256 `fe9bd733d5dce497adebd5254e9e1b6c4d28f4e7e1b020b5b9457f1adb62a523`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` verified after download.

Updated Shield and Pixel7Pro from177to178. Both installed APK hashes/version match the signed artifact. Opened existing BOOP entry on both for joint review. Shield renderer-ready was observed. Shield16of16 preference hashes unchanged; phone10of11 unchanged, including eye/appearance/voice. The phone notification bookkeeping file changed as in the prior install; no manual preference/permission reset was performed. Pixel10ProXL was not updated.

v177 signed rollback is retained locally: SHA256 `e8d082480e63fd02f6b391e6a362a1b71fb529061614c36ed821770347a68c8c`.

## Next joint check

Ryan to judge loose fibres and crown light at normal viewing distance, then the live preview while adjusting eye colour in Eyes and animation. Existing sharing opt-in remains unchanged; no real colour change was sent by the installer. v178 physical verdict is pending.

The coherent approved five-digit sign grip and fake-notification diagnostic remain following work. No Wall-notification repair or natural-voice feature is included in v178.
