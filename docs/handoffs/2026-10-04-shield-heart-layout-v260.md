# Shield260: track-mix heart recognition

Ryan approved repairing the Home and lyrics hearts while preserving the accepted
layout and playback. Base: live Shield259 owner boop-shield-next-up-heading-v259,
e4036bae9171d69e7615d853c122e3a64f1eace3. Installed versions were independently
verified: BOOP259, Deezer301000101. Main lacks the old startup/context files;
fallback BOOP startup guidance and the owning branch's current handoff were read.
A clean task-owned clone preserves the old checkout's deleted documentation.

## Diagnosis and implementation

Installed native read returned UNAVAILABLE at heart-read. A read-only native
probe showed track_mix_partner with the verified six-button finite player row,
one heart followed by shuffle/transport. Old policy accepts that row only for
album/playlist contexts and assumes two left controls for a track mix. Deezer's
Lyrics control was present. This proves a layout-recognition failure, not a
favourites outage. Ryan reported that native Deezer unfavourite/refavourite works.

Track mix now recognises either existing verified layout. Finite layout uses
heart index0 and rejects dislike; recommendation layout retains heart index1
and dislike index0. Other context rules, version pin, native track/session/context
checks, nonce ownership, state validation, confirmation, cancellation and
no-replay transport remain unchanged.

Requested failed status reads show an error after exhausted retries or watchdog.
Automatic reads remain quiet. Same-track confirmed state stays preserved and
no blind toggle follows a read. Home and lyrics use the same heart controller.
Timed lyrics retrieval is separate: installed259 returned27lines for Bob Dylan's
Blowin' in the Wind in247ms from actual current metadata. The user switched to
YouTube while the screen was being checked; further foreground input stopped.
Visual lyrics acceptance remains pending. No lyrics layout/provider edits.

## Verification

New geometry and requested-failure regressions failed before the fix. 27 focused
heart/state/receipt/confirmation/lyrics checks passed afterwards;9 additional
lyrics/package checks pass. A generated-build integration check cannot run until
materialization; signed CI performs it. Independent review found no actionable
issues but did not independently run tests. A read-only native layout prototype
returned OK saved0 for She's A Rainbow. No favourite/dislike mutation was sent.

Shield becomes260; Wall220 and permanent signing are preserved. Signed build,
artifact verification, installation and physical button acceptance are separate
pending steps. No installation is authorised merely by repository syncing.

## Signed release ready - 4 October 2026

Final code source41b910f3b7514685403f85ee126bf86b4eab7c84 passed the complete
signed workflow37173408925, including inherited checks, generated integration,
lyrics/audio/HA tests, compilation and both APK verifiers. The first build failed
only because the preserved weather release guard still expected259; that guard
now expects260 with its weather/permission checks preserved. All8weather tests
also passed locally. The production bridge compiled locally using CI's Java17;
the installed Android Studio JBR25 was unsuitable for this D8 version.

Independent downloaded-artifact verification confirms package/version260, source
commit, SHA25622530ad82f0ed083b88029a2d68ac55a003dc98fdb6b15adb8b202694ff88067,
permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde,
all29assets and16native libraries identical to the hash-verified accepted259APK.
The APK includes requested-read failure feedback. A Shield-only APK and a
sanitised verification receipt are retained in this chat's outputs.

No upgrade has been installed and no diagnostic favourite mutation was sent.
Physical Home/lyrics button acceptance and visual lyrics acceptance remain
pending. Build: https://github.com/ryankemble2006-web/boop/actions/runs/37173408925
