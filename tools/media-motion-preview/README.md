# BOOP H1 / P1 motion preview

Current status,2026-09-06: this remains an offline preview. The shared motion
sampler is now used by the separately integrated Deezer H1 runtime on this
branch; P1 popcorn is still preview-only. The original preview-stage notes below
describe that earlier milestone, not the current shipped H1 capability. Read
SESSION_HANDOFF.md at the repository root for live evidence and deferred work.

Open `index.html` directly in a browser; it is self-contained and makes no network calls. The enlarged renders and TV-corner scale studies use the same motion. Pause is available; reduced-motion preferences pause by default.

Speed feedback, 2026-09-06: Ryan asked for BOOP to be a wee bit quicker. Preview playback is now 1.2x: a 3-second music cycle and a 10-second popcorn cycle. The Java-exported base curves remain unchanged; carry the 1.2x playback multiplier into any later Android integration. Verify preview timing with `node tools/media-motion-preview/test-preview-clock.cjs`.

**Preview only. Nothing is connected to the installed Shield, Deezer, Kodi, Forki, Home Assistant, the microphone or authentication.** The seven Java tests verify motion and export, not device behavior. No APK was created in this increment.

## Approved direction

Ryan chose H1 Soft groove for its 3D depth and P1 Quiet nibble on 2026-09-06. H1 gently sways and bobs. P1 rests at the tub, lifts a kernel to the implied mouth, makes the kernel disappear, lowers the empty hand and collects another. No drawn mouth, body or extra gag was introduced. Yellow articulated hands are eating pantomime, not validated ASL/BSL signs.

## Files

- `assets/`: cleaned RGBA music, cinema base, hand and kernel layers.
- `source/`: untouched originals used for cleanup. H1 is the exact approved original render; the other layers were separated from the approved P1 concept with image generation.
- `ART_PROVENANCE.md`: generation and cleanup provenance.
- `asset-alpha-proof.png`: light/dark inspection sheet.
- `motion.json`: exported pose values, not a hand-written second animation implementation.
- `preview.fragment.html`: editable preview template.
- `qa-rest.html`, `qa-lift.html`, `qa-nibble.html`, `qa-return.html`: deterministic stills of the same renderer for inspection.

## Rebuild

The Java source is `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MediaPuppetMotion.java`. The H1 Android renderer now uses it too; P1 remains preview-only. Use Java 17 and JUnit 4.13.2 / Hamcrest 1.3. From the repository root:

```powershell
./tools/media-motion-preview/test-motion.ps1 -JunitPath <junit-jar> -HamcrestPath <hamcrest-jar>
java -cp tools/media-motion-preview/build ExportMotion > tools/media-motion-preview/motion.json
python tools/media-motion-preview/build_preview.py --motion tools/media-motion-preview/motion.json --fragment <inline-output.html> --standalone tools/media-motion-preview/index.html
python tools/media-motion-preview/audit_assets.py
```

For art cleanup, use Pillow, NumPy and OpenCV; `clean_layers.py --source source --output assets` preserves source RGB and changes only alpha. User explicitly approved local background cleanup after generated layers contained painted checkerboards. No paid image API fallback was used.

## Historical Android integration plan (written before H1 integration)

The H1 integration and play/pause sofa checks below subsequently happened.
The remaining foreground-app, P1 and broader device checks are recorded in the
root SESSION_HANDOFF.md; this original plan is not a claim they all shipped.

Wire a lifecycle-aware renderer only after the motion is accepted. Retain nonfocusable/nontouchable overlay behavior, no overlay microphone or HA connection, hide-on-Home and display-mode recovery. Stop animation while hidden or detached. Do not replace the protected build/checkpoints without regression evidence.

Automatic reaction needs verified on-device signals: Deezer actually PLAYING, and Kodi/Forki foreground. Android media sessions and app-usage access are candidates, not enabled or physically verified here. Discover installed packages rather than inventing a Forki package. Missing access must fall back to ordinary eyes. The next Shield sofa test must include pause/stop, app switches, remote focus, Home hiding, HDR changes and resource use.
