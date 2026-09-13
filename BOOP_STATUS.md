# Current blocker: user-visible eye colour failure

2026-09-13: Ryan tested eye change and reports it is NOT working. Device/surface and whether the local display or cross-device delivery failed remain unspecified. Colour is not accepted. Pause further speed work/deployment; preserve the implemented speed candidate.

Source `d149cb509ec376779daf84c50f621d8adcbacd24`, v160, is on GitHub. Fresh checks: appearance `34760362361`, timing `34760362356`, full build `34760362417` all succeeded. Artifact `10318393790` exists. No v160 runtime/deployment acceptance is established. Last recorded v159 installs were on both emulators, Shield and Pixel 7; physical Pixel 10 remains excluded.

Read `docs/handoffs/2026-09-13-colour-failure-continuation.md` and SESSION_HANDOFF.md. First reproduce and trace the actual colour failure; numerical hue persistence and HA-ready status do not prove visible changes. This update changes documentation only.

---

# BOOP status: primary colour/speed continuation

GitHub owner `boop-unified-eye-sync-safe-v159`. Ryan confirmed this work is primary.
Shared-colour v159 was signed, emulator-tested, installed and hash-verified on Shield/Pixel 7 in the preceding continuation. Shield HA publish/rejoin worked; actual two-device delivery still pending because Pixel 7 was locked. No unlock bypass. Pixel 10 remains untouched.
Animation-speed v160 is now implemented in the GitHub candidate. Existing authored animation, master/shader/hue files are protected. Defaults to original 1x; options .5/1/1.5/2. Fresh timing CI, full signed build and local runtime checks are pending. Do not confuse implementation with acceptance. See SESSION_HANDOFF.md and existing plan.
