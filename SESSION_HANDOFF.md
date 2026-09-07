# BOOP Wall Native Chat v34: isolated relay handoff

Updated 2026-09-07. Owning branch: **boop-relay-reviewed-v34**.
Status: **signed setup candidate; full CI green; live deployment and physical acceptance absent**.
App/build commit `453bda9ec5bcd12f44fe50d63c7db5ce7a718295`. Full receipt: `docs/BOOP-WALL-V34-BUILD-RECEIPT.md`.
Shared map verified on main at `85d08eb7733febc6c976afaa36755be3fa35ec36`.

## Ownership and method

Started from approved relay plan on `boop-wall-free-chat-wip@7aa871f70d620093e1e2e176df13820b1fd51964`.
Another session independently implemented that same plan and advanced the original
Wall branch to bbd50a6 then 1201678 when inspected. Its commits were read and
preserved. THIS variant is isolated; no application merge or force push occurred.
Both variants say v34; use exact branch/commit/APK receipts, not version labels.
Never mix one variant's Worker source with the other's APK. Reconcile explicitly
before any promotion. Main is a context hub, not a combined application branch.

This chat had no subagent launcher. Implementation and spec/security/diff reviews
were serial with red/green tests; no independent agent review is claimed. The six
implementation task commits and findings are in `docs/BOOP-RELAY-V34-REVIEW.md`.
The approved plan is preserved verbatim; execution results are recorded here.

## Implemented contract

Hold eyes for three seconds: **OpenCode / Free Chat / Native Chat / Cancel**.
OpenCode stays default, existing preference values remain valid, and the choice
persists/reverts through the same menu. All modes run local processing exactly
once first. Only NO_MATCH can invoke the selected conversational assistant; local
auth/offline/missing-target/device failures never become relay questions.
Native success uses existing TTS/follow-up/puppetry. Short conversation errors use
existing LocalReply, not a second TTS engine or credential-clearing HA auth path.
The relay client owns/cancels its transport and retains only a response ID in RAM.
Browser Free Chat still copies the utterance and needs manual paste/send.

The Worker authenticates before reading the utterance, bounds input/output,
fixes the upstream/model server-side, parses real Responses output arrays, supports
opaque continuation, and emits sanitized short error codes. There are no tools,
automatic paid retries, prompt logs or provider keys in Android. Only the Worker
holds OPENAI_API_KEY; Android receives a separate private-prototype relay token.
Read relay/cloudflare/README.md for request bounds, stored conversation privacy,
billing, missing multi-user/cost protections and deployment steps.

## Verified build and tests

App: com.boop.alpha1, versionCode 34, 0.4.14-wall-native-chat, stable-signed debug.
CI run `34084002048`, job `101624466469`, artifact `10004727824`: **all normal required gates PASS**.
177 local source/Python/JVM tests and 13 mock Worker tests PASS. CI also passed
bridge tests, Android units, materialization, signature/package/archive validation,
real wake-microphone activation, actual three-mode menu/persist/revert/cancellation,
notice/portrait+landscape natural blink/sleep/background tests and pairing return.
Downloaded APK `1bb448d6f458cf4b527a69f7137f65ef3dd5a1a1da7dfab958b5cfe8efaeba97`, 139485306 bytes; full provenance in the receipt.

The final UI run uses half pixel dimensions and half density (720x1560 @280),
preserving dp/aspect and animation time. Live View renders are captured at observed natural OnPreDraw blink phases;
toast and sleep images use UiAutomation screenshots. There is no forced blink,
synthetic time, paused animator or altered closed-frame threshold. No physical phone or live provider test is claimed.

## CI history retained, not hidden

- Run 34081435081 at 59554c1: compile/units/signing passed; 30-second wake-marker
  assertion failed without focused runtime diagnostics. Root cause remains unknown.
- Run 34082040808 at 0bfe1b5: same production code, added read-only diagnostics;
  wake and menu passed. Natural portrait blink/reopen/sleep passed; landscape
  closed-frame capture failed while face was awake, foreground and eligible.
- Run 34082686491 at a0db9bf added observation-only frame diagnostics; wake/menu
  passed. Portrait natural blink ran, but min drawn openness was 0.19216101 over 10
  observed frames, never the unchanged test threshold <0.15. Queue/eligibility
  stayed true at animation scale 1.0. Artifact 10004315430 preserves that report.
- ddf4e2b reduces only disposable-emulator pixel workload. Five tests protect exact
  proportional scaling, physical-device rejection and observed override checks.
  No production blink code, duration, sleep deadline or assertion was weakened.
- Run 34083364637 at ddf4e2b: every actual visual assertion passed, including
  both natural blink orientations, notice, sleep and background cancellation.
  The wrapper still failed because `am instrument -w` decoded the stream result
  rather than printing the raw completion code that its grep required.
- 453bda9 requests raw `am instrument -w -r` output and retains the exact
  `INSTRUMENTATION_CODE: -1` check. A regression test catches the previous mismatch;
  another catches pipeline masking of the render-profile command. Both were red
  before their focused fixes. No production animation or threshold changed.
- Final successful run is above. Earlier high-resolution frame-capture/wake failures
  are not proof of broken physical blinking, nor a proven full-resolution fix.

## Configuration still required

The downloaded APK is **setup-required**: no Worker deployment or live OpenAI call.
Do not call Native Chat operational from installing this APK alone. This route uses
separate API billing, not consumer ChatGPT/Codex credits. OpenCode and Free Chat
remain available while Native Chat is unconfigured.

Configured APK tokens are extractable: never publish them through public builds.
This variant rejects nonempty relay credentials in public-repository CI. No repo
visibility or credentials were changed. Use a private build/distribution route,
then configure the Worker with OPENAI_API_KEY and a separate random BOOP_RELAY_TOKEN;
set BOOP_RELAY_URL and that matching token privately in Actions, rebuild/sign,
and test a real response. Do not paste secrets into chat or committed files.
Per-device enrollment, revocation, multi-user isolation and hard cost limits are
not implemented. This is a single-owner prototype, not a public service.

## Preservation and next acceptance

Accepted physical Wall stays 595e1daa43393882a0e5de43967545ac526b8b66; its tag must not move.
Preserved Wall 3a702f8, Shield Home/Routines checkpoints, Launcher and other app
lineages remain separate. Existing permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` is unchanged.
Raw MainActivity, face artwork/drawing, v33 183ms blink, bigger paste notice,
permissions, wake assets, local HA clients and Shield/Launcher sources are unchanged
from 7aa871f. No phone installs, settings/permission changes or signer changes occurred.

Ryan's earlier observations: v31 query-copy/new-chat/paste prompt worked; v32 blink
worked on his tested phone. They do not establish v34 native or physical acceptance.
v31/v32 receipts remain preserved; v33 history is docs/BOOP-WALL-V33-HANDOFF.md.

Next: choose this matching implementation for private relay setup, then verify
native speech and follow-up, offline/quota/auth messages, local media with relay
unavailable, and physical hold/revert/Back/sleep. Record actual device evidence
before promoting. Already-open Work sessions must reread main and this branch.
"Update memory" stays documentation-only unless Ryan separately authorizes code,
permissions, installs or signing. GitHub sync does not claim a laptop/LAN connection.
