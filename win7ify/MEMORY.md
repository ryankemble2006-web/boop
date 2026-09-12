# Win7ify durable decisions

2026-09-12. Win7ify is a separate Windows utility within the public BOOP repository,
explicitly requested on `boop-win7ify-v01`. Source: `win7ify/`. Do not fold it into
an Android package or change an Android app branch, permission or signer.

The user requested an EXE built by GitHub now and the advanced shell work recorded
for a later Codex session. BOOP styling uses the existing approved eyes unchanged,
black/cyan colours, large readable controls, and a conspicuous undo action.

The first v0.1 physical Apply test failed. This is NOT a physically accepted build.
The Widgets value TaskbarDa rejected an identical-value rewrite on the test laptop;
the equivalent TaskbarAl write succeeded. The existing user's original backup was
inspected and kept intact. Do not describe this as a generic missing-admin problem.

Required repair principles:
- Leave settings already correct alone, including during Restore.
- A denied setting is named and skipped, not forced by changing permissions.
- Continue the remaining independent settings and show truthful per-setting outcomes.
- Keep the complete original backup after partial restore; validate before writing.
- Preserve schema-v1 compatibility and first-original values across repeated Apply.
- Do not claim saved registry data proves that Windows drew a Windows 7 interface.
- Opening the app is not permission to Apply. Refreshing Explorer is separately confirmed.
- No automated appearance/layout/screenshot acceptance. Functional tests and process smoke
  are separate from the user's real-device visual judgement.

Advanced Start/taskbar replacement, Aero/glass and third-party integration remain
future work, not features quietly included in this repair. Read CODEX_HANDOFF.md.

Verification update: v0.1.1 source bb554f6 built in run 34671365939. All 21
functional tests passed in CI and on the laptop; the exact GUI opened/closed
without settings changes. The original backup stayed byte-identical. This does
not replace the pending real desktop Apply/Restore and visual acceptance.
