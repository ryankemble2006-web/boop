# Shared-context hub handoff — 2026-09-06

## Continuity refresh — 2026-09-06

Ryan explicitly requested an "update your memory" refresh. No new product
decision, implementation result, physical test, or app-branch task was supplied.
The shared context, rules, status, and this handoff were reread; live `main` was
fetched and verified at `7c0cf6b` before this documentation-only update. This
does not promote any app state or substitute for fetching an owning app branch
before its next change.

Main is the cross-project entry point, NOT a consolidated latest build of all apps.
This change is documentation only. Application source/build configuration on main
is left at its previous8fcd6da baseline; do not build current Wall from it.
Use BOOP_START_HERE.md to select each app's development branch and read that
branch's SESSION_HANDOFF.md for fresh evidence and pending work.

Ryan approved: laptop develops Shield, Android develops Launcher; GitHub shares
reviewed code, decisions, memory and status. Agents check/fetch before editing
and update/publish handoffs at the end. Never silently overwrite concurrent work.
The local clone had been fetching only the old Shield Home branch; active app
fetch mappings and a local Launcher checkout were added.
New tasks need BOOP repo access. This is not automatic raw conversation syncing.
