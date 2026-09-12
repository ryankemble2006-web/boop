# v145 phone eye-entry crash repair

Date: 2026-09-12. Isolated branch: `boop-v144-devmenu-hue-experimental`.
Source: `055275a17ad0a0f485cc11f500edc0c258e63656`.
Package/version: `com.boop.alpha1`, `145 / 1.2.145-devmenu-hue-crashfix`.

## Reproduced defect and repair

v144 crashes when swiping from Launcher into the phone eyes. The user-triggered
fatal stack identifies `BoopCanonicalFaceView` constructor -> `setEyeHueDegrees`
-> `GLSurfaceView.requestRender`: GLThread does not exist before setRenderer.
Initialize the saved hue directly on renderer state; create the renderer thread
before any frame request. Runtime slider updates continue requesting frames.

The spoken/settings developer entry uses the in-place MainActivity menu, not
BoopDevMenuActivity. Final materialization now changes both in-place developer
face constructors and exposes EyeCatalogue.ALL there. The original standalone
menu, approved animation timelines, master artwork and hue shader are retained.

## Verification

- Regression assertions were observed failing on the old constructor/menu wiring.
- Fresh materialization and 10 focused contracts: PASS.
- Full local Unified unit suite: 277 tests, zero failures/errors/skips, 82 suites.
- Local complete Unified Java compile: PASS.
- GitHub run `34681294081`: SUCCESS for the exact source above.
- GitHub artifact `10293639800`, `BOOP-Unified`: available at verification time.
- CI receipt: 68 focused Shield + 217 focused Unified tests, zero failures/errors.
- Independent apksigner, aapt package/version, ZIP integrity and Git-source checks: PASS.
- APK SHA-256: `af10e94b40572904111278004be215dfa0fd93639e8babe1a8ecf68abbf0fddd`.
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Packaged eye-master hash is still the locked ffbd67af...; hand master unchanged.
- Packaged shader matches the exact signed Git object. Windows CRLF working-copy
  differences are not artifact corruption; compare text sources to Git bytes.

## Later successful phone deployment and runtime observation

A later continuation supersedes the earlier blocked-install result below.
The verified update returned Success on Pixel 10 Pro XL. Fresh Android package
queries report versionCode 145 and 1.2.145-devmenu-hue-crashfix. The installed
base APK hash independently matches af10e94b40572904111278004be215dfa0fd93639e8babe1a8ecf68abbf0fddd.
The prior v144 backup was hash-matched to the phone before replacement.
No uninstall, data clearing, permission grant, signer replacement or Shield
deployment was performed by this continuation.

MainActivity was observed resumed and the GLES2 canonical renderer reported
ready during the post-update observation. No new AndroidRuntime fatal exception
appeared in the collected post-update log window. The 10 focused contracts
were independently rerun successfully against the materialized source.
The phone subsequently locked; a UI-tree check showed the lock screen rather
than BOOP. Actual in-place menu interaction, iris appearance and complete
swipe/longer-session acceptance remain pending, not inferred from CI or logs.
Private raw observations and backups stay outside Git.

## Earlier blocked attempt (historical, superseded)


v145 is NOT installed. The tool safety gate blocked the installation request.
A subsequent read confirmed Pixel 10 Pro XL still runs v144. Do not call this
a physically working fix yet. No new permissions, defaults or Shield changes.
The current v144 APK was privately backed up and hash-matched before any write.
v144 is a diagnostic rollback, not the last-good animation build; v143 had user
positive animation feedback. Signed v145 was copied and hash-verified on the
laptop Desktop as BOOP-v145-DEVMENU-HUE-CRASHFIX.apk. Raw logs, device address and
private backups stay outside the repository.

Next safe step: obtain explicit user confirmation before retrying the blocked
phone-only installation. Then reproduce Launcher-to-eyes and the actual in-place
menu entry and inspect fresh crash/renderer logs. Visual/colour acceptance remains
user-owned. Do not weaken permissions or substitute a new signer to bypass gates.
