# BOOP isolated Native Chat implementation review

Reviewed 2026-09-07 against plan `docs/superpowers/plans/2026-09-07-openai-conversation-relay.md`.
Base `7aa871f70d620093e1e2e176df13820b1fd51964`; six implementation commits end
at `b3bc1768f9563a4a149b9a04c3a096c271f748ba`.

## Review method and ownership

This chat has no subagent-launching tool. Tasks were implemented and reviewed
serially, with separate spec/security/diff passes and red/green tests. No independent
agent review is claimed. The per-task working ledger was kept in ignored local
`.superpowers/sdd/2026-09-07-openai-conversation-relay/`; this report publishes its
relevant findings without scratch files or private environment details.

Original Wall advanced concurrently to `bbd50a6`, then `1201678`, with another implementation of
the same plan. This series remains isolated on `boop-relay-reviewed-v34` and has
not been merged into `boop-wall-free-chat-wip`. Both are v34 candidates with
potentially different contents; a version label alone is not provenance. Read the
owning handoff and exact APK hash. No other session's commits were overwritten.

## Task-by-task findings

1. `b3510f7`: relay protocol/config/error handling. The actual OkHttp/JSONObject
   harness tests missing configuration with zero traffic, success, conversation
   continuity, auth/quota/rate/timeout/network/service errors, malformed replies,
   strict bounded types and client cleanup. Redirects/retries are disabled; relay
   lifecycle owns its transport and cannot close the house client's connection.
2. `67ec01e`: additive Native Chat mode. Router harness proves local is called
   once and every status except NO_MATCH stops there. Selected assistant and stale
   mode guards run after local processing. OpenCode and browser Free Chat remain.
   Existing persisted enum values are retained; no credential preference migration.
3. `c9c0acc`: short spoken error mapping. Inspection found existing LocalReply
   already owns outcome wording, so additive status messages belong there rather
   than duplicating new TTS handling in MainActivity. Existing assistant success
   uses the same speak-and-follow-up path. Relay auth cannot clear HA credentials.
4. `1c16e6e`: Worker authentication, bounded JSON, fixed upstream, true Responses
   output-array parsing, continuation, timeout and sanitized errors. Two additional
   red tests caught response IDs leaking credentials and reuse of the provider key
   as relay token; both were corrected before green. All provider calls are mocked.
5. `daefebf`: BuildConfig/CI integration. Empty/partial config produces empty fields;
   private-config values are safely escaped. The public-repository guard prevents
   publishing a token-configured APK: build-time injection does not make an APK's
   bearer token unextractable. Existing signer and permissions are unchanged.
   The legacy key-prefix scanner was narrowed only for the exact bare-prefix
   rejection predicate; mutation tests still reject full literal credentials.
6. `b3bc176`: native selection/persistence/revert added to the existing emulator
   menu flow without making a live provider request. Full normal verification,
   not the old user-specific build-only marker, is required for this request.

## Preserved boundaries

Raw MainActivity, face drawing, v33 183ms blink, readable Free Chat notice,
Android permissions, wake assets, original sleep deadline, local HA clients,
Shield/Launcher source and existing signing credentials are unchanged from the
approved v33 base. Materialization retains prior wake/eye patches. Protected
physical checkpoints are not moved or promoted by CI results.

## Explicit limits

The Worker is not deployed and no live provider conversation has been tested.
This is a single-owner private prototype: no per-device enrollment, independent
revocation, production multi-user isolation or hard spending cap is implemented.
Bearer authentication and request bounds alone are not spending controls. Stored
Responses continuation is disclosed in the setup README. Configured APKs must be
privately distributed. No physical phone installation or permission change occurred.

For final CI, APK receipt and remaining acceptance, use SESSION_HANDOFF.md and
the v34 build receipt; this review is not by itself a runtime pass.

## Execution completion checklist

- [x] Task 1: protocol, configuration and error tests red/green; commit b3510f7.
- [x] Task 2: local-first Native Chat selection/routing red/green; commit 67ec01e.
- [x] Task 3: existing LocalReply error speech red/green; commit c9c0acc.
- [x] Task 4: mock-tested Worker and credential boundaries; commit 1c16e6e.
- [x] Task 5: private BuildConfig, public-artifact guard, empty-config Android build; commit daefebf.
- [x] Task 6: scoped review, normal CI, signed setup APK, exact receipt and handoff.

Full run 34084002048 at 453bda9ec5bcd12f44fe50d63c7db5ce7a718295 passed all required gates, including actual natural blink
observation at the reduced-pixel, same-dp emulator profile. Failed earlier wake/
frame captures remain documented; no forced-blink change from the concurrent
branch was adopted. Physical and live Worker/OpenAI checks remain absent.
The approved plan remains unchanged; this checklist records execution and the
explicit isolation/method/private-distribution deviations.
