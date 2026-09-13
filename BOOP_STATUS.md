# BOOP status: Wall-to-Shield colour physically accepted

Updated 2026-09-13 after Ryan reported: "it works btw, i just tested eye colour :) from wall to shield".

Wall -> Shield is user-accepted. Controlled physical captures also verified visible Shield -> Pixel 7 delivery at hue 260 and Pixel 7 -> Shield delivery at hue 122. This supersedes the earlier reported-failure blocker; do not keep presenting colour as untested or restart a speculative repair.

Current evidence and remaining coverage: `docs/handoffs/2026-09-13-colour-accepted-wall-shield.md` and `SESSION_HANDOFF.md`. A deliberate two-device offline/reconnect cycle is still separate outstanding coverage. No claim of exhaustive lifecycle or all-surface acceptance.

Physical Shield and Pixel 7 remain on verified v159. Both had sharing enabled and hue 2 in the read-only post-acceptance receipt. Preserve the user's newer colour; do not reset it to test fixtures. Physical Pixel 10 was not touched. No app code changes or deployments were made in this diagnostic continuation.

Owner remains `boop-unified-eye-sync-safe-v159`; app source is `d149cb509ec376779daf84c50f621d8adcbacd24`, v160. The existing main phone/TV emulators were found already running v160. Published appearance `34760362361`, timing `34760362356` and full build `34760362417` succeeded; artifact `10318393790` remains the candidate. Speed is implemented and CI-green, not physically accepted.

Next: validate that existing speed candidate locally, preserving exact 1x and authored animation. Keep physical speed deployment on hold until its runtime gates pass. Accepted v156 Shield polish stays accepted. This update changes documentation only.
