# BOOP Music Lab: signed side-by-side fork verification

Updated 2026-09-13. Ryan requested a complete separate installable fork while other operations continue, with no merge now. Task branch `boop-music-lab-side-by-side-v161` is based directly on `boop-unified-music-audio-prompt@174d492f36db80bc3da4036d9434ce1ec3c1582a`. No Unified, main or other lab branch was modified.

## Delivered candidate

- Name: BOOP Music Lab.
- Package and app namespace: `com.boop.musiclab`.
- Version: `1 / 0.1.1-v161-audio-prompt`.
- Source/build commit: `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`.
- GitHub run `34774532761`, job `103770090841`: completed SUCCESS.
- Artifact `BOOP-Music-Lab`, ID `10322813560`.
- File `BOOP-Music-Lab-v1.apk`, 155257454 bytes.
- APK SHA256 `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`.
- Artifact ZIP: 71261881 bytes, SHA256 `27e9e146108ebf06370150727e20b0fc1198217b9e08aaa771cfcd76bc61fa82`.
- Permanent certificate SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The APK was downloaded through the GitHub artifact connector. ZIP digest matched GitHub's artifact digest; extracted APK digest matched apk-sha256.txt; built-commit.txt matched source above. Package badging, public signer receipt and ZIP integrity were read. Artifact inspection/extraction in the sandbox did not build, install or execute application code.

## Isolation implementation

New `scripts/materialize-music-lab.py` copies the already prepared Unified tree to a separate fresh target. It repackages app namespace, internal app references, app label, version, task affinity and auth callback scheme while preserving all library code except required namespace-reference substitutions. Parent file-tree fingerprint is unchanged before/after the fork. Source Startup Manager protections for all com.boop packages remain intact. No device data or credentials are copied; no shared UID is used.

The fork manifest excludes HOME, ASSIST and BOOT_COMPLETED intent filters, RECEIVE_BOOT_COMPLETED permission, and Home override service registration. It therefore does not register as another automatic Home/assistant/boot owner. This does not claim every unrelated full-app feature was runtime-tested. Existing approved PNGs, WebP images, shaders and copied assets remain byte-identical. No visual generation or animation redesign occurred. Signing remains the existing BOOP permanent signer on GitHub; no replacement key or relay credential injection.

## Test-first and final results

RED: `fafcc351c373c0591edcbf93d0ed9c0d20b7a9de`, run `34774429117`, job `103769804737`. Four of five new fork tests failed because the fork output did not exist; parent preservation passed. Parent permission/timing/11 focused contracts passed. Failure log was read before implementing the materializer. Build/signing were skipped; no failed APK was distributed.

GREEN: run `34774532761` at `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`:

- Four music permission tests passed, including 18 executed pure-Java decisions.
- Six existing animation-speed functions passed. Logs include 160720 exact timing checks, 1157272 edge checks and 20920 Lab callback checks each for raw and materialized source. The 26 authored clips remain unchanged.
- Materialized speed/colour/master consistency passed.
- Eleven focused Now Playing/notification canonical-owner, exclusive face ownership and notification-manifest pytest tests passed.
- Five fork tests passed: independent identity/version; unchanged parent; private permission entry and byte-preserved assets; no competing Home/assistant/boot registrations or shared UID; no original application ID left in fork source/configuration.
- Full signed fork assembly succeeded, 104 tasks executed, including Java compilation and duplicate-class checks.
- Packaged application ID/version/label/entry activity, permission activity presence, forbidden role/original-ID absence, permanent signature and archive integrity verified.
- Artifact upload and temporary signer removal completed successfully.

Existing Android/Gradle/action deprecation and JKS-format warnings were nonfatal and are not claimed fixed. This was focused build/logic verification; the entire historical test suite was not rerun. Source diff and namespace/callback/isolation changes were reviewed in-session; no independent reviewer is claimed.

## Scope and acceptance boundary

No installation or launch on Shield/Pixel/emulator occurred. No runtime permission prompt, visual acceptance or side-by-side physical operation is claimed. No permission was granted, data cleared, HOME choice changed, laptop source modified or local checkout synchronized. The original Unified acceptance record remains historical evidence for that app, not this lab.

The fork includes v161 plus the already implemented conditional permission entry only. **No Visualizer audio-level sampler or music-driven bounce is implemented.** Music should later dictate bounce height while saved animation speed independently dictates blinks, with no physical-microphone fallback. Usable Deezer audio data remains to be tested jointly with Ryan after implementation is authorized.

Do not merge or install over Unified. Requested future installation targets only com.boop.musiclab with package/signature/hash checks; Ryan operates the OS permission prompt. Any later integration must reconcile against the live then-current Unified successor and preserve other operations. This receipt and the final handoff/status/memory update are documentation only; the signed source commit above remains unchanged.
