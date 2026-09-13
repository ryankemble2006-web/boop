# BOOP Lyrics Lab v159 status

Updated 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; `159 / 0.1.159-lyrics-footer`.

**Requested UI:** removed the Back to Now Playing footer action. The existing
licence/copyright attribution is now passive, bottom-right in that area and half
its former font size. Actual lyrics, artwork, controls and Skip-refresh code are
unchanged. Existing remote Back handling is not intercepted.

**Build and delivery:** GitHub `34772352902` SUCCESS, source
`e6f9bbb736ac90287815bea3a9c48496505675ed`, artifact `BOOP-Lyrics-Lab` / `10322900224`.
APK SHA256 `fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.
Permanent signature and package/source/digest verified after download. Shield-only
lab update succeeded; installed version and base APK hash match. Unified v161's
APK hash was identical before/after. No launch, playback input or permission change.
No phone/emulator operations, local source edits/builds, or Unified/main merge.

**Checks:** new non-visual footer-wiring assertion red then green. Existing session,
recording-identity, timing/ownership, transport, entry and packaging checks passed.
No new visual/runtime pass is claimed. Ryan inspects the delivered UI together with
us under current main's GitHub-development/joint-testing workflow.

**Preserved acceptance:** Ryan confirmed v158 Skip reloads lyrics inside the lab on
Shield, without exit/reopen. ADB is fine per Ryan. Neither is an unresolved gate.
Do not enlarge that confirmation into every natural-completion/offline/long-session
case. The v158 local 38x2 runtime evidence remains distinct and recorded.

**Next:** keep polishing this separate lab while the other branch finishes; merge
only when agreed, into the then-current Unified successor, followed by joint tests.
See SESSION_HANDOFF.md and `docs/handoffs/2026-09-13-lyrics-footer-v159.md`.
