# BOOP Music Lab status: signed APK staged, Shield installation blocked

Updated 2026-09-13. Owner: `boop-music-lab-side-by-side-v161`. No merge into any other branch is authorized. Other operations continue independently.

## Latest requested installation

Ryan explicitly requested installation on Shield only. The exact signed GitHub artifact was downloaded into a task-specific laptop Downloads directory. Read-only APK SHA256 and actual apksigner verification passed against the existing permanent certificate. No local app build, source edit or checkout synchronization occurred.

**NOT INSTALLED:** the tool blocked the combined verification/install command before execution because it could not determine the request's safety status. No alternate installation route was attempted. This was not an Android installer result and is not evidence of an unsafe APK.

Read-only before/after package checks confirmed Music Lab absent and Unified still `162 / 1.2.162-native-lyrics`, with identical Unified APK hash. Resolved HOME was unchanged. No phone/emulator was operated, no app was launched, no permission was granted and no data was cleared. See SESSION_HANDOFF.md for the exact scope and read-only helper correction.

## Verified build retained

BOOP Music Lab: `com.boop.musiclab`, `1 / 0.1.1-v161-audio-prompt`. Full v161-derived app plus conditional music permission entry, with separate application identity, storage and permissions. HOME/ASSIST/boot registration is excluded in the fork. Approved artwork and animation timing are retained. No physical-device acceptance is claimed.

Source `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`; signed GitHub run `34774532761`, SUCCESS, rechecked; artifact `10322813560` / `BOOP-Music-Lab`. APK SHA256 `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb`. Existing permanent signer verified, no replacement key. Prior downloaded APK/ZIP matched the build receipt; current laptop-staged APK hash/signature were checked directly.

Preserved build tests: five fork isolation checks; four permission tests with 18 Java decisions; six existing animation timing functions; materialized speed/colour/master checks; 11 canonical-owner/notification-manifest checks; signed assembly and packaged identity/signature/integrity checks passed. These were focused non-visual build checks, not execution of the complete historical test suite or device UI. No app source or new build was produced for the installation request.

**Still not done:** installation, device launch, OS permission grant, emulator use, microphone capture, Visualizer sampling or VU bounce. Nothing was merged or installed over working Unified. User settings, data, artwork and laptop app sources remain untouched by this task.

Read `SESSION_HANDOFF.md` for current installation status and `docs/handoffs/2026-09-13-music-lab-fork.md` for historical build provenance. Permission entry after a future installation: Launcher Settings > Now Playing > Music audio access. Actual bounce remains the agreed future design: music determines height; animation speed independently controls blinks. Keep the tool-blocked attempt distinct from a successful installation and do not route around an explicit tool denial.
