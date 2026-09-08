# BOOP unified status

Updated 2026-09-08. Branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Latest physical retest: partial pass, remaining failures

Ryan tested the repair candidate below and confirmed **HA device names are fixed and Home buttons actually control his devices**. Preserve this physically working HA path during subsequent eye/assistant work.

The eye rendering is **physically rejected**: his private photo shows comb-like horizontal tearing at the inner upper eyelid edges, and he reports no visible blink. Android still reports **Assistant choice was not changed**. Assistant takeover, remote-button activation and audio from that remote are not accepted. Phone acoustic wake was not updated in the latest report; its earlier failure remains unresolved. Room switching and repeated-open scale have no new physical acceptance from this message.

Overall release status: **NOT physically accepted**. Do not reissue the same candidate as an all-fixed APK.

## Signed artifact and historical non-visual evidence

Code commit `949f1085328a3e815d9bc57747425f1f930c48db`, version 45 / `1.1.2-unified-assist-repair`. Run `34201200463` succeeded; artifact `BOOP-Unified` ID `10045928699`. Extracted APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

That build passed 58 Shield focused tests + 66 unified wake/routing/assistant tests, zero failures/errors/skips, Launcher lint, compilation, package identity, manifest component presence, permanent signature and ZIP integrity. These checks did not certify appearance, blink or Android's real assistant selection. No visual, screenshot/golden, aesthetic source-string, emulator launch/install or physical-device acceptance ran in GitHub.

## Current source findings and next safe step

See `SESSION_HANDOFF.md` for exact findings and primary Android references. The brightness-per-row transparency conversion is not an approved silhouette and can cut dark eyelid edges. The declared VoiceInteractionService omits required recognitionService metadata; this must not be mistaken for a confirmed Shield firmware limitation. The blink code has runtime animation-scale and power-save vetoes, whose actual device state is not yet known.

This investigation updates documentation only. No new app code, APK, device settings, permissions or signing changes. Pause speculative rebuilds, establish the active blink gate and replace the invalid assistant integration with a valid supported route without another microphone stack. Do not regenerate locked art or tune another arbitrary alpha threshold. Keep HA, iris tint and headphones/puppetry unchanged.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic installation/grants, package/signer change, Windows synchronization or unattended monitoring.
