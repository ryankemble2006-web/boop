import sqlite3
import sys
import threading
from random import randint

import xbmc
import xbmcgui

from resources.lib.common import tools

if tools.is_stub():
    # noinspection PyUnresolvedReferences
    from mock_kodi import MOCK

from resources.lib.modules.globals import g

from resources.lib.modules.seren_version import do_version_change
from resources.lib.modules.serenMonitor import SerenMonitor
from resources.lib.modules.update_news import do_update_news
from resources.lib.modules.manual_timezone import validate_timezone_detected

g.init_globals(sys.argv)
xbmcgui.Window(10000).setProperty("BOOP.TraktStartupSync", "running")
xbmcgui.Window(10000).setProperty("Seren.StartupText", "Checking Trakt activities")
do_version_change()

g.log("##################  STARTING SERVICE  ######################")
g.log(f"### {g.ADDON_ID} {g.VERSION}")
g.log(f"### Platform: {g.PLATFORM}")
g.log(f"### Python: {sys.version.split(' ', 1)[0]}")
g.log(f"### SQLite: {sqlite3.sqlite_version}")  # pylint: disable=no-member
g.log(f"### Detected Kodi Version: {g.KODI_VERSION}")
g.log(f"### Detected timezone: {repr(g.LOCAL_TIMEZONE.zone)}")
g.log("#############  SERVICE ENTERED KEEP ALIVE  #################")


def startup_notification(*args, **kwargs):
    """Our skins own loading feedback; other skins retain the normal notice."""
    home = xbmcgui.Window(10000)
    if home.getProperty("Seren.StartupText") != "Loading Home":
        home.setProperty("Seren.StartupText", str(args[1]))
    while xbmcgui.getCurrentWindowId() == 12999:
        if home.getProperty("Seren.StartupSkinManaged"):
            return
        if g.wait_for_abort(0.1):
            return
    if not home.getProperty("Seren.StartupSkinManaged"):
        g.notification(*args, **kwargs)


def sync_trakt_at_startup():
    """Run one quiet incremental Trakt activity check as Kodi starts, during the skin loading screen.

    This is deliberately one-shot. Playback handoff is event-driven; Seren does
    not poll Trakt every few seconds while idle. The activity endpoint already
    tells us whether anything changed, so unchanged accounts cost one request.
    """
    from resources.lib.modules.setup_flow import process_pending_rebuild

    if process_pending_rebuild():
        xbmcgui.Window(10000).setProperty("BOOP.TraktStartupSync", "failed")
        return
    if not g.get_setting("trakt.auth", ""):
        xbmcgui.Window(10000).setProperty("Seren.StartupText", "Not authed, continuing")
        xbmcgui.Window(10000).setProperty("BOOP.TraktStartupSync", "no_auth")
        # Always give the user a startup status, even when Trakt is not authorised.
        # Keep it long enough to survive a normal Kodi splash/skin transition.
        startup_notification(g.ADDON_NAME, "Not authed, continuing", time=1000, sound=False)
        return
    try:
        # Start immediately while the skin is still on its loading screen.
        from resources.lib.database.trakt_sync.activities import TraktSyncDatabase

        sync_db = TraktSyncDatabase()
        failed = sync_db.sync_activities(silent=True, force_activity_check=True)
        if failed or sync_db.startup_sync_failed:
            status_text = "Trakt sync unavailable — local data kept"
        elif sync_db.startup_sync_skipped:
            status_text = "Trakt already checked recently"
        elif sync_db.startup_sync_changed:
            count = sync_db.startup_sync_changed
            status_text = f"Startup sync complete — {count} {'update' if count == 1 else 'updates'}"
        else:
            status_text = "Startup sync complete — already up to date"
        xbmcgui.Window(10000).setProperty("Seren.StartupText", status_text)
        xbmcgui.Window(10000).setProperty("BOOP.TraktStartupSync", "failed" if failed or sync_db.startup_sync_failed else ("done" if sync_db.startup_sync_changed else "unchanged"))
        if sync_db.startup_sync_failed:
            startup_notification(g.ADDON_NAME, "Trakt sync unavailable — local data kept", time=6000, sound=False)
        elif sync_db.startup_sync_skipped:
            startup_notification(g.ADDON_NAME, "Trakt already checked recently", time=6000, sound=False)
        elif sync_db.startup_sync_changed:
            count = sync_db.startup_sync_changed
            suffix = "update" if count == 1 else "updates"
            startup_notification(g.ADDON_NAME, f"Startup sync complete — {count} {suffix}", time=6000, sound=False)
        else:
            startup_notification(g.ADDON_NAME, "Startup sync complete — already up to date", time=6000, sound=False)
    except Exception:
        xbmcgui.Window(10000).setProperty("BOOP.TraktStartupSync", "failed")
        g.log("Startup Trakt activity refresh failed", "debug")


from resources.lib.modules.trakt_outbox_service import TraktOutboxService
from resources.lib.indexers.trakt import TraktAPI
outbox_service = TraktOutboxService(lambda: TraktAPI(refresh_auth=False).write_queue)
monitor = SerenMonitor()
monitor.outbox_service = outbox_service
outbox_service.start()
startup_sync_thread = threading.Thread(target=sync_trakt_at_startup, name="SerenTraktStartupSync", daemon=True)
startup_sync_thread.start()
try:
    xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=longLifeServiceManager")')

    do_update_news()
    validate_timezone_detected()
    try:
        g.clear_kodi_bookmarks()
    except TypeError:
        g.log(
            "Unable to clear bookmarks on service init. This is not a problem if it occurs immediately after install.",
            "warning",
        )

    xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=torrentCacheCleanup")')

    g.wait_for_abort(30)  # Sleep for a half a minute to allow widget loads to complete.
    while not monitor.abortRequested():
        xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=runMaintenance")')
        if not g.wait_for_abort(15):  # Sleep to make sure we don't possibly clobber settings
            xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=cleanOrphanedMetadata")')
        if not g.wait_for_abort(15):  # Sleep to make sure we don't possibly clobber settings
            xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=updateLocalTimezone")')
        if g.wait_for_abort(60 * randint(13, 17)):
            break
finally:
    # Kodi forcibly terminates add-on services after five seconds.
    outbox_service.flush_on_exit(budget=3)
    del monitor
    g.deinit()
