# BOOP - current notes only

Updated 2026-09-13. This file intentionally contains only the newest routing notes.
Old startup-rule/context stacks were removed because they had become stale and slowed down work.
Current user instructions always win.

## Unified app

Current combined device delivery is owned by `boop-unified-v146-integration`.
Version 146 / `1.2.146-phone-lyrics-startup`, package `com.boop.alpha1`, was installed on the Pixel 10 Pro XL and Nvidia Shield.
It combines the phone iris/menu/crash fixes, Lyrics availability preflight, and Startup Manager/defaults.
For new combined work, start from that live branch or a newer verified successor, not an older isolated v142/v145 line.
The standalone Animation Lab remains separate unless explicitly merged.

Latest user-accepted Shield successor in this continuation: `boop-unified-artist-link-v156`, source `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`. The launcher/Now Playing sweep, favourites selector and artist navigation are accepted; do not repeat those gates as unfinished work. Shared eye colour, then animation speed, are next. Local colour drafts are unfinished and are not a published replacement for this accepted source. Preserve the working Wall hue and authored animations. Pixel 10 remains on deployment hold until Ryan explicitly resumes it.

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

## GitHub resource recovery (2026-09-13)

Use the GitHub connector for repository resources and their live source of truth. Do not substitute laptop file inspection or ADB troubleshooting when Ryan reports a GitHub resource failure. Local emulators/device tools still have their separately authorized testing role; they do not prove GitHub access.

If a tool/resource lookup fails, rediscover the relevant GitHub actions and fetch the live branch reference, then fetch the exact repository path at the returned commit. Use only freshly returned response resources for pagination, and verify that their contents belong to the requested file. Historical `/response/...` identifiers are not durable GitHub file addresses or cross-chat handoffs.

For a failed read, make one controlled fresh fetch and inspect its actual result. A failed response lookup is not evidence that source files moved or permissions changed. Do not guess paths or widen permissions. For a write with an uncertain outcome, reread the live branch/file before retrying; never blindly repeat writes or overwrite concurrent work. Treat an explicit safety denial separately from a missing resource, and do not route around it.

Verified in this recovery: direct live-branch lookup, commit-pinned handoff retrieval and rereading a newly returned response resource succeeded. One historical response reference returned a different tool record than expected. This demonstrates that the historical reference is unsuitable here; it does not establish the cause of every reported error.

GitHub reported an API/authorization-related incident on 13 September, resolved at 10:44 UTC; OpenAI also reported related Codex GitHub failures as resolved. These are relevant upstream incidents, not proof of the cause of a particular missing-resource message. Sources: https://www.githubstatus.com/incidents/0rn90wk115q9 and https://status.openai.com/incidents/01M2D1CZG0862JB4JHVW56QF89 . This recovery note is not a claim that a platform-wide bug was fixed by a repository edit. Already-running chats must reread current instructions before relying on them.

## Laptop emulator-first testing

On Ryan's laptop, use the local Android emulator as the default BOOP development/test loop before real-device checks whenever the behaviour can be reproduced there.
For phone/Unified work, use `Pixel_10_Pro_XL_API_36`: official `pixel_10_pro_xl` hardware profile, Android 16 / API 36 Google APIs x86_64, 1344 x 2992.
For Android TV-specific behaviour, use `BOOP_Android_TV_API_36` when appropriate.

Build, install, launch, collect screenshots/logs, and run focused UI/animation/functional checks locally first. Avoid repeated physical-device deploy cycles for ordinary iteration unless the behaviour is genuinely device-specific or Ryan explicitly asks for it.
Real Pixel/Shield hardware remains the acceptance gate for remote focus/navigation, real notifications/media integrations, HDMI/audio/display behaviour, sensors, performance and other hardware-specific paths.
An emulator pass is `locally tested`; never relabel it as `physically tested` or `physically accepted`.

## Desktop Commander / ADB

On Ryan's laptop, the known-good Desktop Commander remote command is:
`npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`

Keep Desktop Commander pinned to `0.2.47` while it works. Do not proactively upgrade it or change this command merely because online documentation changes.
Only troubleshoot, change version, or change command after a real failure. Diagnose that failure from Ryan's supplied screenshots/evidence first, then make the smallest necessary change.
