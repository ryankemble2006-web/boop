# Launcher startup sync receipt

`service.py` preserves the installed Seren startup sync and adds a read-only Home-window property, `BOOP.TraktStartupSync`: `running`, `done`, `unchanged`, `no_auth`, or `failed`.

BOOP waits for the service receipt instead of dispatching `launcherSyncTrakt` again. It reads and caches Next Up before releasing its temporary Kodi display. Apply this service change when building a Seren update; an unrelated upstream replacement of service.py will otherwise omit the receipt and BOOP will retain its cached episodes after timing out.

The Shield launch targets exported `org.xbmc.kodi.Main` directly, using a display matching the physical app display size and density. The Splash activity redirects Main to display 0 and must not be used for background sync.

Startup sync now starts immediately, with no 3 + 3 second pre-sync or 5 second notification timer. The actual NTV Aura, Eden and Aura Motion skins set `Seren.StartupSkinManaged` as their first Startup.xml action. Their bundled `startup_handoff.py` waits for the service receipt while keeping the loading screen visible, then opens Home. They do not dispatch another sync. If Seren is absent, no extra wait is added. If unauthorised, the skin displays `Not authed, continuing` for 1000 ms before Home. Older Seren without a receipt is bounded to ten seconds; an in-progress sync is bounded to 180 seconds. Abort remains responsive.

The modified skin source is under `2026-10-07/cod/work/optimization/source/skin.*/`; corresponding installed desktop skins and the Shield's installed NTV Aura were updated directly. No repository release has been published. Other skins retain immediate service-owned startup sync and normal notifications.

The skins' actual loading labels now read `Seren.StartupText`. `activities.py` publishes the existing `current_dialog_text` / explicit progress text during the startup sync even when the sync is silent, so stage names come from Seren rather than a separate skin translation. The service publishes its exact completion/error notification text before its terminal receipt. The skin publishes `Loading Home` immediately before `ReplaceWindow`; subsequent notifications cannot overwrite it. NTV Aura's Home loader also says `Loading Home`. Deploy both service.py and the activity-progress change with the skin files.
