# BOOP - current notes only

Updated 2026-09-17. This file intentionally contains only the newest routing notes.
Old startup-rule/context stacks were removed because they had become stale and slowed down work.
Current user instructions always win.

## Current workflow: downloadable APKs; Ryan owns visual testing

Ryan explicitly changed the default on 2026-09-17 to conserve tool/AI usage: the assistant and GitHub handle code and non-visual CI; Ryan installs candidates himself and is the eyes. This supersedes the previous joint-device-testing and assistant-install defaults. Routine BOOP development and delivery must not depend on Desktop Commander or on a computer-control connection to the laptop.

Keep source edits, non-visual functional/logic/regression tests, builds, permanent signing and durable handoffs on GitHub. After the existing code/package/signer checks pass, deliver a clearly labelled download link/button for each actual signed `.apk`, distinguishing Wall from Shield and identifying its version/build. Prefer the APK itself rather than making Ryan unpack an Actions artifact ZIP. Never claim a download exists or that a package was installed without evidence.

Ryan downloads the APK and drags it onto the corresponding scrcpy device window. He performs installation and all visual/physical acceptance. Do not automatically install an APK, drive the device, check its screen or use RDC to deliver it. A later explicit request for a specific diagnostic or installation is scoped permission for that task only, not a return to the old default.

Assistant-run and GitHub-run visual tests remain OFF: no automatic screenshots/captures, visual acceptance sweeps, emulator launches, hosted Android visual acceptance or repeated device-testing hurdles before delivering a candidate. Ryan may provide screenshots, errors and observations for the assistant to interpret. Do not initiate an independent visual-testing loop. Non-visual code tests, lint/static checks, compilation, package/version/signer/hash checks and CI remain ON. CI success is not evidence that animation, navigation, media, onboarding or visible behaviour has been accepted; record Ryan's reported acceptance separately.

Current authorized physical BOOP targets are Shield and Pixel 7. Leave physical Pixel 10 alone unless Ryan explicitly changes that boundary. No permission changes, lock bypass, data clear, signing-key substitution or reset of his colour/settings choices. Keep the approved artwork, coded animations, exact original 1x timing and accepted Shield polish. Tool denials must not be bypassed; they are not evidence that animation controls themselves are dangerous. Distinguish a denied operation from an independently authorized later request.

Local scrcpy is Ryan's install/control surface, not a new assistant-to-laptop bridge. The Pixel 7 Pro and Shield desktop launchers select their own device and leave audio forwarding off. Keep device addresses/identifiers and private laptop connection details out of this public repository. No new app build, installation or physical BOOP acceptance is implied by configuring those launchers.

## GitHub-first efficiency and test order

Use GitHub as BOOP's shared source of truth across Chat, Work and Codex so a mode change does not require Ryan to reconstruct project state. Start with the intended LIVE branch HEAD, then read that branch's newest task-specific handoff/status/memory and only the source files relevant to the current change. Prefer exact line/range reads, targeted searches and commit/diff comparisons over rereading whole files or broad historical context. Do not rerun expensive checks or reread unchanged context merely because the session or mode changed.

For GitHub Actions, inspect the run/job/step summaries first and fetch full logs only for the failing or otherwise relevant job. For code changes, use the smallest relevant automated regression/logic test first where the repository already supports one, then use the full GitHub Actions build/test as the authoritative clean-environment code/build evidence. Keep source edits and builds on GitHub; do not substitute a local checkout build for the published branch result.

Device testing and installation are Ryan-owned by default. Use his supplied evidence, not routine Desktop Commander/ADB calls, for visual feedback. A separately requested non-visual runtime diagnostic does not authorize screen inspection or an autonomous test loop. Keep these evidence levels distinct: targeted test result, GitHub CI/build result, installation verification and Ryan's physical acceptance are not interchangeable. Preserve the existing concurrency, signing, privacy and no-local-source-edit/build rules while following this efficient path.

## Accepted default character: photographic felt Boop

Ryan permanently selected the accepted v189 felt Boop as the default on 2026-09-15. Preserve his rounded single moving eyelids, whole-cap motion, photographic fibres and lighting. The free eye, felt and independent hand colour controls and saved choices remain available. Hand hue zero restores the exact original yellow; custom hand colours do not change anatomy or source artwork. Do not regenerate or replace this character during unrelated work. Only Ryan's explicit new direction can revise the accepted default, retaining this checkpoint.

Frozen reference: tag `boop-felt-default-v189`, built source `470bb4b471e7e452b29b6a3a437bea0fa4a0904e`. Signed v189 was installed and physically accepted before the default lock. Current hand-colour work continues from the accepted v190 felt signs on `boop-hand-colour-v191`; consult that branch's latest receipt for candidate build/install status. The new build lock protects exact default files plus materialized renderer routes; default means the shared photographic renderer, not forcing everyone's saved colour to charcoal.

Every hand has exactly five digits: four fingers and one opposing thumb. The approved notification grip shows four curled fingers in front and hides the existing thumb behind the sign. Keep connected palms, short cuffs and the original yellow felt default; Ryan explicitly authorized runtime hand recolouring in Build a Boop. Hiding a digit does not remove it from the model. Reuse one coherent grip across notification styles; the pose is not validated ASL/BSL.

## Puppet identity and deferred personality work

Ryan defines Boop as a genderless, raceless felt puppet that can belong in any household. Custom colours express felt and character styling. Preserve five digits per hand, connected anatomy and the accepted material; future ASL/BSL needs its own implementation and validation.

Historical direction, now deferred for Astra rather than an active next task: playful, deliberately silly voice/personality controls in Build a Boop, with voice choices carried to the Shield like colour choices. Preserve optional natural-language/voice download direction and the pause/scrub lab for shared inspection. This direction is historical and does not reopen the frozen Voice work; consult the owning branch's [recorded next direction](https://github.com/ryankemble2006-web/boop/blob/805a4e6ed88eb9dd221d8fc421c839c3e79c8489/docs/handoffs/2026-09-15-puppet-personality-next.md).

## Standalone EastEnders and Casualty shortcuts

Current owner: `iplayer-shortcuts-home-20260916`, with `eastenders-shortcut/` and `casualty-shortcut/` as separate standalone packages. EastEnders 1.8 and Casualty 1.2 are installed on Shield: obsolete shortcut menus removed, identical exact trailer skipping retained, and post-playback programme return now requests Home. Read the owner's `docs/handoffs/2026-09-16-iplayer-shortcuts-home.md` for successful CI, original-key signing, installation verification and pending joint playback acceptance. This does not modify the Unified app or supersede its owner.

## Current Wall and Shield apps

Current application owner: `boop-wall-shield-split-v207`. This is the accepted v206 combined lineage separated into one shared assistant implementation with Wall and Shield application shells, not older standalone code. Wall `com.boop.alpha1` version 207 / `1.2.207-wall` was clean-installed on Pixel 7 Pro; Shield `com.boop.shieldoverlay` version 207 / `1.2.207-shield` was clean-installed on Nvidia Shield. Fetch the LIVE owner and its SESSION_HANDOFF.md, BOOP_STATUS.md and BOOP_UNIFIED_MEMORY.md before work. Main remains the context hub, not the current app source.

Installed build source: `aa8fd9f6d79f28b441a48df31138a75d38420118`; signed run `35110823569`; artifact `10451993779`, `BOOP-Wall-Shield-v207-Signed`. Later verification/docs commits are not an instruction to reinstall. Both apps were initially verified at genuine first setup without this sequence pressing Continue. Latest readback: Pixel 7 remains at first setup; Shield subsequently has its setup-complete flag true and YouTube foreground. Do not reset or overwrite that subsequent progress. Read `docs/handoffs/2026-09-16-v207-signed-clean-install.md` on the owner.

Voice is frozen in BOTH apps. Ryan deferred the roughly 12-second latency investigation, alternative providers/voices and further pitch/cadence work for Astra. Preserve current natural voices, local model/download path, existing tuning and sharing. No extra Voice prerequisite is implied. All 16 packaged native libraries matched accepted v206 byte-for-byte.

The clean installs were explicitly authorized for this newcomer test. No BOOP data, models, credentials or setup flags were restored; no permission was pre-granted and no Android default Home was forced. Three BOOP Shield listener grants and its overlay access were found enabled later and cleared for the fresh-access brief, preserving unrelated listeners. The origin of those intervening grants was not established. Do not silently change current access or rewind setup now. Broader onboarding, HA/media/sharing/Johnny behavior remain human acceptance checks.

Previous combined owner `boop-hand-colour-v191` at `841458b8bbc53773d16a18bb0359de1c0550f5e2` is preserved, as is its dirty historical local v203 documentation. Do not start from that stale worktree or historical standalone code. Preserve all earlier combined/accepted checkpoints and keep standalone Animation Lab, Johnny and unrelated projects separate.

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

## Desktop Commander / ADB: optional, not the delivery path

Routine builds, APK downloads and Ryan's scrcpy installations do not require RDC. Do not ask him to restart/pay for a bridge or switch modes merely to deliver a candidate. Scrcpy itself does not give the assistant independent access to his laptop.

For a separately requested task that genuinely needs remote laptop/device access, check the tools actually available and respect the current authorization. On Ryan's laptop, the known-good Desktop Commander remote command is:
`npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`

Do not substitute `npx`, a different package command, a newer Desktop Commander release, or commands copied from current online documentation. The exact `npx.cmd ...@0.2.47 remote` command is the proven laptop path. Do not claim ADB itself is unavailable or force Work mode merely because a bridge is absent; distinguish local ADB from the assistant's remote-control transport. The manual-delivery and no-visual-testing rules above remain the default.

Keep Desktop Commander pinned to `0.2.47` while it works. Do not proactively upgrade it or change this command merely because online documentation changes.
Only troubleshoot, change version, or change command after a real failure. Diagnose that failure from Ryan's supplied screenshots/evidence first, then make the smallest necessary change.
