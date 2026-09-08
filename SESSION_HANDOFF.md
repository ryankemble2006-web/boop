# BOOP unified handoff

Updated 2026-09-08. Owner: `boop-unified`; package `com.boop.alpha1`; existing permanent signer only. Fresh main owns shared contracts. This handoff owns implementation and verification evidence.

## Current candidate

This change continues from live `848a99725b6d64d4fb80d74fc466bdff807dd41e`, preserving its preceding Shield and wake-name repairs. Build and physical acceptance are PENDING at this source checkpoint. No new signed APK is claimed yet.

The last delivered APK was `6cd9c67a03c639a20acde892e2d57186652e13d5`, successful run `34125882296`, artifact `10020439707`. APK SHA-256 `603e72b6f3a83eca429e90a11559454ca2d9c140eee69bff9bcd537d9a276a4e`; ZIP SHA-256 `455cd406beb111ac6d5d1d974b20bd54735a65222aa702612476929efd4ad285`. Version code 43 is reused by this lineage; distinguish candidates by exact built commit and checksum. The physically accepted rollback `e746affbb82b577cef2f1cf6e731dff186c8f881` remains protected.

## Latest physical evidence from Ryan

- The newer cyan Shield Settings appearance is visible and liked. HA rows below Room were not selectable and belong on Home, not Settings. Home had legacy focus treatment and labels moved on focus.
- Approved replacement is Room -> controllable devices on Home, configuration only in Settings, no Favourites, no helpers/diagnostic clutter. HA room membership must be read-only and fail-closed.
- Pixel 7 survived Ryan's Android 17 upgrade; media control, blink and eye-colour controls still work. After choosing a spoken name, neither the custom name nor the BOOP fallback woke it. Exact dock state and acoustic cause have not been independently verified. Do not claim Android 17 compatibility or wake success from these partial observations.
- Ryan explicitly requests iris-only colour correction in THIS build. He owns visual checks; GitHub must not run visual acceptance checks again. See BOOP_RULES.md.

## Included changes

Existing source already contains the real UNIGRAM tokenizer repair and fallback stream creation around custom-name setup. Those were absent from the delivered APK. They remain pending acoustic verification and are not proof of the sole cause of the reported failure.

Home now has an actionable room picker and stable per-device rows. Updates do not recreate or disable the focused row while waiting for HA confirmation. Up/Down follows the actual control list; Left returns to the navigation rail. Shared cards and settings retain fixed padding and no focus scaling; visibility uses Android's descendant rectangle request. No decorative settings animation was added.

The actual HA compact category lookup is a keyed object, whereas old code attempted to read an array. `HaEntityCategory` accepts keyed objects, arrays and literal categories, and unknown category metadata fails closed. `RoomDeviceControls` admits supported on/off lights, switches and fans only, excluding helpers, sensors, hidden/config/diagnostic/unknown-category and unavailable items. Existing HA target expansion handles inherited room membership; local same-room filtering remains. No HA membership changes or rename/config writes are introduced.

`BoopIrisTint` and `BoopIrisTintMath` constrain tinting to the original blue/cyan iris ring and preserve original pixels elsewhere. The original atlas is retained, default blue returns the original bitmap, and a single reusable tinted bitmap plus a small pixel tile avoids full-bitmap allocation on every slider event. The existing materialized hue setter is replaced by `patch-unified-iris-cache.py`; all animation methods stay unchanged. Local checks are numeric fixtures, not visual acceptance.

`patch-unified-shield-dashboard.py` invokes the small room-control adapters for both source and copied library trees, and the cached-iris adapter for Wall. These are materialization integration checks, not aesthetic tests.

## Verification at this checkpoint

Python adapter syntax and workflow YAML parsing passed locally. Four synthetic colour/ring fixture methods passed with a plain-JDK assertion harness, not an Android runtime. The preceding run `34190471954` failed two functional dashboard tests after Favourites filtering was removed; `848a997` restored supported-control filtering. Do not omit real functional failures as 'visual tests'.

The unified workflow now runs focused non-visual integration/control/wake tests, Launcher lint, assembly, package/signature/archive checks, and uploads the signed APK immediately. Non-visual process/entry-activity smoke runs after upload. No screenshot, golden-image, artwork-appearance, geometry or animation-judging gate runs. Legacy visual tests remain historical source but are not selected by this workflow. Raw emulator logs are not included in the downloadable artifact.

Next: inspect this exact CI result, fix any functional build failure, verify artifact digest/built commit/signer, and deliver the APK. Then record exact result here and reconcile status/memory/context. Physical tests remain Ryan's: all Shield D-pad rows/room selection/actions, renamed and BOOP fallback wake while wirelessly docked, undocked tap mode, and iris-only colour appearance.

## Preserved boundaries

No app/package/HA/pairing/signing identity, microphone policy, Android target, permissions, installation, launcher behaviour or unrelated animation is changed. Undocked Wall is intentionally tap-to-talk; continuous wake is a foreground wireless-dock feature. Do not restore always-on handheld microphone capture as a workaround. Original detailed history remains in `docs/history/unified-v43/` and `docs/BOOP-UNIFIED-WAKE-NAME-RECHECK.md`.

The Windows checkout was not mounted in this session. Connected GitHub was checked live; no laptop checkout synchronization or user-device deployment is claimed. No unattended background polling is established.
