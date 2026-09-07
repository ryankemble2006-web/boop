# BOOP — start here on either device

Updated 2026-09-07. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

**One repository, separate app branches.** The branches do not need the same
commit ID. Each task must match the current GitHub HEAD of its own app branch
and read the other branches when cross-project context is needed.

| Work | Authoritative branch | Source | Owner / state |
| --- | --- | --- | --- |
| Shield Home + Deezer puppet | [boop-shield-media-puppetry](https://github.com/ryankemble2006-web/boop/tree/boop-shield-media-puppetry) | shield-overlay/ | Laptop; H1 play/pause and lower placement user-tested |
| BOOP Wall voice/eyes | [boop-wall-resurrection](https://github.com/ryankemble2006-web/boop/tree/boop-wall-resurrection) | source/ plus materialization scripts | Working voice/control baseline; preserve it |
| Wall Native Chat + eye hue + sleepy close candidate | [boop-wall-native-chat-eye-hue](https://github.com/ryankemble2006-web/boop/tree/boop-wall-native-chat-eye-hue) | Native Chat lineage + isolated hue/sleep helpers/materialization patches | v39 sleepy-close candidate; v38 eye-colour UX physically accepted, v39 sleep CI/signer green, physical sleep acceptance pending; read SESSION_HANDOFF.md and BOOP_WALL_NATIVE_CHAT_EYE_HUE_MEMORY.md |
| Historical Wall eye-hue experiment | [boop-wall-eye-hue-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-eye-hue-wip) | source/, scripts/patch-wall-eye-hue.py, focused tests/workflow | v31 experiment layered on Wall v30. Do NOT install it over newer Native Chat builds |
| Wall Free Chat / Native Chat line | [boop-wall-free-chat-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-free-chat-wip) | source/, chat/relay/materialization patches | Native Chat family. A concurrent hue implementation also landed here at `36e3199`; preserve and reconcile rather than duplicating it |
| Isolated Native Chat relay candidate | [boop-relay-reviewed-v34](https://github.com/ryankemble2006-web/boop/tree/boop-relay-reviewed-v34) | source/, relay/cloudflare/, focused tests | Separate reviewed relay implementation; read its handoff/receipt; do not assume it is interchangeable with the Free Chat branch solely because both say v34 |
| BOOP Launcher | [boop-launcher-alpha1](https://github.com/ryankemble2006-web/boop/tree/boop-launcher-alpha1) | launcher/ | Android-led development; read launcher/README.md |
| Wall-to-Launcher swipe draft | [boop-wall-launcher-handoff-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-launcher-handoff-wip) | source/ and focused tests | Historical preserved draft; Wall v30+ owns the reviewed swipe, with physical acceptance still lineage-specific |
| Routine-authoring research | [boop-routine-authoring-v1](https://github.com/ryankemble2006-web/boop/tree/boop-routine-authoring-v1) | docs/superpowers/ and tests/ | Capability evidence/design; do not infer full authoring implementation |
| Older Shield Home lineage | [boop-shield-home-implementation](https://github.com/ryankemble2006-web/boop/tree/boop-shield-home-implementation) | shield-overlay/ | Historical/reference; new Shield work uses puppetry branch |
| Cross-project context | main | AGENTS.md, BOOP_CONTEXT.md, this map | Context hub; app files here can be historical |

## Native Chat relay ownership (2026-09-07)

The relay plan was executed from `boop-wall-free-chat-wip@7aa871f`. Another
session independently implemented the same plan on that branch while a separate
review task worked. Its commits were preserved. The reviewed variant remains
isolated on **boop-relay-reviewed-v34**. These implementations are not
interchangeable copies just because both say v34. Use the APK, Worker source and
build receipt from the SAME branch and explicitly reconcile before promotion.

Native Chat is conversation-only: local NO_MATCH -> authenticated Worker -> OpenAI
Responses -> existing BOOP voice/eyes. OpenCode and browser Free Chat stay available.
Empty configuration is a setup candidate, NOT live chat. No provider key belongs
in Android. Configured bearer-token APKs require private build/distribution;
public-repository CI rejects token-bearing builds. Deployment and private account
configuration must be verified separately from a signed APK or mock tests.

## Wall eye-hue and sleep ownership (2026-09-07)

There are multiple hue histories and they must not be confused:

1. `boop-wall-eye-hue-wip` is the older v31 experiment based on Wall v30. It
   proved the hue-only rendering approach but is not an update for newer Native Chat installs.
2. `boop-wall-native-chat-eye-hue` is the current combined lineage. The v36
   two-eye one-second summon physically failed and is superseded. The first v37
   voice matcher was too strict, so `BOOP change eye colour` could fall through
   to Free Chat/assistant routing. v38 fixed this by treating eye-colour requests
   exactly like Voice Settings: a tolerant local intent checked before HA/OpenCode/
   Native Chat/Free Chat fallback. Ryan physically tested that v38 UX and called it
   **literally perfect**; preserve that routing and slider behavior.

Current candidate v39 uses versionCode 39 / `0.4.19-wall-sleepy-close` and the
existing permanent BOOP signer. Green build commit: `240a1286`; workflow run
`34094925623`. v39 changes only sleep puppetry: it begins with the exact accepted
183 ms idle-blink geometry, fully reopens, pauses, droops to a half-lidded rest,
then closes slowly over a total ~1.24 seconds before a late fade to black. Wake
animation remains unchanged and cancels an in-progress sleepy close safely. The
old 300 ms whole-face squash/fade sleep path is superseded in the v39 materialized
app. Do not change the accepted v38 eye-colour route while tweaking sleep.

Default cyan/blue at 190 degrees deliberately applies no colour filter; non-default
hues tint the existing shared `boop_eyes` Paint. Do not add a mouth, replacement
artwork, brightness/saturation/opacity controls, themes or effects.

A concurrent implementation of the hue concept landed on
`boop-wall-free-chat-wip@36e3199`. Preserve that commit. The combined branch
records/reconciles it rather than blindly stacking duplicate hue logic.

## Start a new Work task

Attach this repository and choose the branch for the app you are developing.
Read AGENTS.md and that branch's SESSION_HANDOFF.md before making changes.
Fetch and compare the live branch heads: a downloaded folder is not a live sync.
Read the fetched main versions of this map, BOOP_CONTEXT.md and AGENTS.md.
Main owns shared decisions/contracts; the app branch's SESSION_HANDOFF.md owns
its current implementation and verification state. Branch-local shared files
are fallback copies when offline, and must not hide newer main decisions.
Root README files inherited from Alpha 1 do not override the current app map.

For current Wall eye-colour or sleep-animation work on a device already running
Native Chat, read `boop-wall-native-chat-eye-hue`, its `SESSION_HANDOFF.md`,
`BOOP_STATUS.md`, and `BOOP_WALL_NATIVE_CHAT_EYE_HUE_MEMORY.md`. Read
`boop-wall-free-chat-wip` as concurrent lineage context. The historical
`boop-wall-eye-hue-wip` is reference only for this install path. Never infer
install compatibility from feature names or versionName text alone; compare
package, signer and integer versionCode.

A Work request to "update memory" remains documentation-only: fetch current
handoffs, reconcile, commit/push documentation, and verify the live branch. Do
not change app code, permissions, installs or signing without a new explicit
request. An already-open Work task must reread these GitHub records.

Cross-project reference does not require merging the apps. For example, a
Launcher task can read Wall's current manifest/source from the Wall branch and
Shield's notes from the Shield branch. Use explicit refs in shallow/single-branch
environments, for example:

    git fetch --no-tags origin refs/heads/boop-wall-resurrection:refs/remotes/origin/boop-wall-resurrection
    git show origin/boop-wall-resurrection:SESSION_HANDOFF.md

Do not check out another branch over dirty work. If connectivity or permissions
prevent synchronization, state that immediately and do not claim up-to-date.

## End a session / hand over

Update SESSION_HANDOFF.md, branch memory/status, and relevant decisions; run
appropriate checks; commit reviewed files; push the owning branch; verify local
HEAD equals the live GitHub branch HEAD. Ryan has requested this as the default
publishing workflow.

## Cross-app contract

- Wall: com.boop.alpha1. Launcher: com.boop.launcher. Shield: com.boop.shieldoverlay.
- Wall and Launcher stay independently launchable. Do not merge packages.
- Existing tap/hold/voice/HA/Native Chat behavior must survive eye or gesture work.
- The current v39 Native Chat + eye-hue + sleepy-close candidate is isolated on
  `boop-wall-native-chat-eye-hue`; do not promote it to a protected physical
  checkpoint until Ryan physically accepts the v39 sleep animation.
- The v38 eye-colour local-intent/slider UX is physically accepted behavior and
  should not be regressed while tuning v39 sleep.
- The old v31 hue candidate is historical and is not an update for newer devices.
- Launcher may open Wall by its package; permissions/return-strip behaviour
  require their own consent and device testing.
- Stable update signing remains in the existing GitHub workflows.

Read BOOP_CONTEXT.md for durable product decisions and each branch's handoff
for current evidence. Do not publish private third-party inspection artifacts.
