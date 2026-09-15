# BOOP - current notes only

Updated 2026-09-15. This file intentionally contains only the newest routing notes.
Old startup-rule/context stacks were removed because they had become stale and slowed down work.
Current user instructions always win.

## Current workflow: GitHub development, joint device testing

Ryan explicitly requested on 2026-09-13: install the signed candidate so he can test it, and from now keep development on GitHub and test together with him.

Keep source edits, non-visual functional/logic tests, builds, permanent signing and durable handoffs on GitHub. After the existing code/package/signer checks pass, provide the signed APK and perform the installation Ryan requests. Do not add an emulator-first gate, automatic emulator run, autonomous visual acceptance sweep or repeated testing hurdles before delivering the candidate. This replaces earlier emulator-first rules, including the former distinction between cosmetic and substantive changes.

Ryan and the assistant test device behaviour together. Follow his observations and agreed next checks; do not claim visible behaviour works merely because CI passes, a slider value changes or a connection says ready. Installation verification means package/version/hash checks, not physical acceptance of animation, navigation, notifications or media behaviour. No GitHub visual tests or hosted Android acceptance runs.

Desktop Commander/ADB may stage the GitHub-built APK, perform an explicitly requested installation on authorized devices, and read back installation identity. Local runtime diagnosis, captures and inputs should be part of the testing agreed with Ryan, not an automatic development loop. Do not edit or build app source locally. Preserve dirty/concurrent checkout work. Emulators are optional only if Ryan explicitly asks for them; do not delete or reconfigure existing AVDs.

Current authorized physical BOOP targets are Shield and Pixel 7. Leave physical Pixel 10 alone unless Ryan explicitly changes that boundary. No permission changes, lock bypass, data clear, signing-key substitution or reset of his colour/settings choices. Keep the approved artwork, coded animations, exact original 1x timing and accepted Shield polish. Tool denials must not be bypassed; they are not evidence that animation controls themselves are dangerous. Distinguish a denied operation from an independently authorized later request.

## Accepted default character: photographic felt Boop

Ryan permanently selected the accepted v189 felt Boop as the default on 2026-09-15. Preserve his rounded single moving eyelids, whole-cap motion, photographic fibres and lighting. The free eye, felt and independent hand colour controls and saved choices remain available. Hand hue zero restores the exact original yellow; custom hand colours do not change anatomy or source artwork. Do not regenerate or replace this character during unrelated work. Only Ryan's explicit new direction can revise the accepted default, retaining this checkpoint.

Frozen reference: tag `boop-felt-default-v189`, built source `470bb4b471e7e452b29b6a3a437bea0fa4a0904e`. Signed v189 was installed and physically accepted before the default lock. Current hand-colour work continues from the accepted v190 felt signs on `boop-hand-colour-v191`; consult that branch's latest receipt for candidate build/install status. The new build lock protects exact default files plus materialized renderer routes; default means the shared photographic renderer, not forcing everyone's saved colour to charcoal.

Every hand has exactly five digits: four fingers and one opposing thumb. The approved notification grip shows four curled fingers in front and hides the existing thumb behind the sign. Keep connected palms, short cuffs and the original yellow felt default; Ryan explicitly authorized runtime hand recolouring in Build a Boop. Hiding a digit does not remove it from the model. Reuse one coherent grip across notification styles; the pose is not validated ASL/BSL.

## Unified app

Current combined app work is owned by `boop-hand-colour-v191`. Fetch its LIVE HEAD and read its current `SESSION_HANDOFF.md`, `BOOP_STATUS.md` and `BOOP_UNIFIED_MEMORY.md` for exact build, installation and user-acceptance state. The branch name does not determine the APK version. Main is the shared context hub, not the latest app source.

Preserve the earlier `boop-unified-v146-integration` combined lineage and the accepted v156 Shield polish at `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. Do not restart from isolated v142/v145 work or unpublished local colour drafts. Standalone Animation Lab and unrelated projects remain separate unless Ryan explicitly requests integration.

## Standalone Shield Launcher

The separate launcher-and-advanced-tools build is owned by `boop-shield-launcher-standalone`.
Project: `shield-launcher/`. Package: `com.boop.shieldlauncher`.
It is intentionally separate from Unified and should not silently absorb unrelated Unified changes.

## Network Q Rally Shield

The separate Shield rally project is owned by `boop-rally-shield`.
Project: `rally-shield/`. Package: `com.boop.rally`.
Keep it isolated from Unified and preserve original game folders/private packs.

## Working rule

For any BOOP task, fetch the intended live branch, preserve dirty/concurrent work, and read only that branch's newest task-specific handoff or verification note when needed.
Do not require the deleted historical root rule stack before planning or editing.
Update the owning branch's handoff/status/memory after material work and verify the LIVE GitHub branch after publishing. Keep installation evidence separate from Ryan's acceptance. Never publish private addresses, raw device dumps, private screenshots, credentials or signing keys.

## GitHub resource recovery (2026-09-13)

Use the GitHub connector for repository resources and their live source of truth. Do not substitute laptop file inspection or ADB troubleshooting when Ryan reports a GitHub resource failure. Device tools have their separately authorized installation/joint-testing role; they do not prove GitHub access.

If a tool/resource lookup fails, rediscover the relevant GitHub actions and fetch the live branch reference, then fetch the exact repository path at the returned commit. Use only freshly returned response resources for pagination, and verify that their contents belong to the requested file. Historical `/response/...` identifiers are not durable GitHub file addresses or cross-chat handoffs.

For a failed read, make one controlled fresh fetch and inspect its actual result. A failed response lookup is not evidence that source files moved or permissions changed. Do not guess paths or widen permissions. For a write with an uncertain outcome, reread the live branch/file before retrying; never blindly repeat writes or overwrite concurrent work. Treat an explicit safety denial separately from a missing resource, and do not route around it.

Verified in this recovery: direct live-branch lookup, commit-pinned handoff retrieval and rereading a newly returned response resource succeeded. One historical response reference returned a different tool record than expected. This demonstrates that the historical reference is unsuitable here; it does not establish the cause of every reported error.

GitHub reported an API/authorization-related incident on 13 September, resolved at 10:44 UTC; OpenAI also reported related Codex GitHub failures as resolved. These are relevant upstream incidents, not proof of the cause of a particular missing-resource message. Sources: https://www.githubstatus.com/incidents/0rn90wk115q9 and https://status.openai.com/incidents/01M2D1CZG0862JB4JHVW56QF89 . This recovery note is not a claim that a platform-wide bug was fixed by a repository edit. Already-running chats must reread current instructions before relying on them.

## Existing emulators: optional inventory, not a testing requirement

Previously configured phone AVD: `Pixel_10_Pro_XL_API_36`, official `pixel_10_pro_xl` profile, Android 16 / API 36 Google APIs x86_64, 1344 x 2992.
Previously configured TV AVD: `BOOP_Android_TV_API_36`.
Do not start, stop, wipe or use them automatically. Their presence does not authorize a local test run. The current workflow above supersedes the earlier default emulator-first policy.

## Desktop Commander / ADB

On Ryan's laptop, the known-good Desktop Commander remote command is:
`npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`

Keep Desktop Commander pinned to `0.2.47` while it works. Do not proactively upgrade it or change this command merely because online documentation changes.
Only troubleshoot, change version, or change command after a real failure. Diagnose that failure from Ryan's supplied screenshots/evidence first, then make the smallest necessary change.
