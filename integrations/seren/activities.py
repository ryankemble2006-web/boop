import contextlib
import itertools
import time

import xbmc
import xbmcgui

from resources.lib.database import trakt_sync
from resources.lib.database.trakt_sync import hidden
from resources.lib.modules import trakt_dropped
from resources.lib.modules.exceptions import ActivitySyncFailure
from resources.lib.modules.global_lock import GlobalLock
from resources.lib.modules.globals import g
from resources.lib.modules.metadataHandler import MetadataHandler
from resources.lib.modules.timeLogger import stopwatch


class PendingStateChanged(Exception):
    """A local mutation superseded the in-flight remote snapshot."""


class TraktSyncDatabase(trakt_sync.TraktSyncDatabase):
    sync_errors = False

    def __init__(self):
        super().__init__()
        self.progress_dialog = None
        self.silent = True
        self.current_dialog_text = None
        self.startup_sync_changed = 0
        self.startup_sync_failed = False
        self.startup_sync_skipped = False
        self._sync_activities_list = [
            # (title, (remote, keys), local_activities_key, func)
            ("Dropped Shows", ("shows", "dropped_at"), "dropped_sync", self._sync_dropped),
            ("Hidden Shows", ("shows", "hidden_at"), "hidden_sync", self._sync_hidden),
            (
                "Watched Shows",
                ("episodes", "watched_at"),
                "shows_watched",
                self.sync_watched_episodes,
            ),
            (
                "Collected Shows",
                ("episodes", "collected_at"),
                "shows_collected",
                self.sync_collection_episodes,
            ),
            (
                "Show bookmarks",
                ("episodes", "paused_at"),
                "episodes_bookmarked",
                self._sync_show_bookmarks,
            ),
            (
                "Shows ratings",
                ("shows", "rated_at"),
                "shows_rated",
                self._sync_rated_shows,
            ),
            (
                "Hidden Movies",
                ("movies", "hidden_at"),
                "hidden_sync",
                self._sync_hidden,
            ),
            (
                "Watched Movies",
                ("movies", "watched_at"),
                "movies_watched",
                self._sync_watched_movies,
            ),
            (
                "Collected Movies",
                ("movies", "collected_at"),
                "movies_collected",
                self._sync_collection_movies,
            ),
            (
                "Movie bookmarks",
                ("movies", "paused_at"),
                "movies_bookmarked",
                self._sync_movie_bookmarks,
            ),
            (
                "Movie ratings",
                ("movies", "rated_at"),
                "movies_rated",
                self._sync_rated_movies,
            ),
        ]

    @contextlib.contextmanager
    def _state_apply_guard(self):
        section = getattr(self, "_active_sync_section", None)
        if section is None:
            yield
            return
        outbox = self.trakt_api.outbox
        account = self.trakt_api.write_queue.account
        with outbox.state_guard():
            if (outbox.section_revision(account, section) != self._active_sync_revision
                    or section in outbox.pending_sections(account)):
                self._pending_sync_skipped = True
                self._current_activity_skipped = True
                raise PendingStateChanged("Preserving newer local Trakt changes")
            yield

    def execute_sql(self, query, data=None):
        with self._state_apply_guard():
            return trakt_sync.TraktSyncDatabase.execute_sql(self, query, data)

    def fetch_remote_activities(self, silent=False, force_activity_check=False):
        """
        Enforces a timeout for the activities call to Trakt based on database timestamp of last pull.
        :return: If last call occurred less than 10 minutes ago, return None, else return latest activities
        :rtype: Any
        """
        self.refresh_activities()
        if "last_activities_call" not in self.activities:
            g.log("Last activities call timestamp not present in database, migrating database change")
            self._insert_last_activities_column()
            self.refresh_activities()
            last_activities_call = 0
        else:
            last_activities_call = self.activities["last_activities_call"]

        if not force_activity_check and time.time() < (last_activities_call + (5 * 60)):
            g.log("Activities endpoint called too frequently, skipping sync", 'info')
            self.startup_sync_skipped = True
            return None
        else:
            remote_activities = self.trakt_api.get_json("sync/last_activities")
            self._update_last_activities_call()
            return remote_activities

    @stopwatch
    def sync_activities(self, silent=False, force_activity_check=False):
        with GlobalLock("trakt.sync"):
            return self._sync_activities_locked(silent, force_activity_check)

    def _sync_activities_locked(self, silent=False, force_activity_check=False):
        self.startup_sync_changed = 0
        self.startup_sync_failed = False
        self.startup_sync_skipped = False
        self.sync_errors = False
        trakt_auth = g.get_setting("trakt.auth")
        update_time = str(self._get_datetime_now())
        if not trakt_auth:
            g.log("TraktSync: No Trakt auth present, no sync will occur", "warning")
            return
        self.refresh_activities()
        self._ensure_dropped_sync_column()
        self.pending_sections = self.trakt_api.outbox.pending_sections(self.trakt_api.write_queue.account)
        self._pending_sync_skipped = False
        remote_activities = self.fetch_remote_activities(silent, force_activity_check)
        if remote_activities is None:
            g.log("Activities Sync Failure: Unable to connect to Trakt or activities called too often", "error")
            return True
        scope = self.trakt_api.cache_scope("sync/watchlist")
        for section, stamp in {
            "watchlist": [remote_activities.get(kind, {}).get("watchlisted_at") for kind in ("movies", "shows")],
            "lists": remote_activities.get("lists", {}),
        }.items():
            self.trakt_api.list_cache.observe(scope, section, stamp)
        if self.requires_update(remote_activities["all"], self.activities["all_activities"]) or self._dropped_requires_update(remote_activities):
            try:
                self._check_for_first_run(silent, trakt_auth)
                self._do_sync_acitivites(remote_activities)
            finally:
                self._finalize_process(update_time)
        self._update_all_shows_statisics()
        self._update_all_season_statistics()

        return self.sync_errors

    def _ensure_dropped_sync_column(self):
        # Migrate in place under trakt.sync; keep the user's existing sync/history.
        if "dropped_sync" not in self.activities:
            self.execute_sql(
                "ALTER TABLE activities ADD COLUMN dropped_sync TEXT NOT NULL DEFAULT '1970-01-01T00:00:00'"
            )
            self.refresh_activities()

    def _dropped_requires_update(self, remote_activities):
        return self.requires_update(
            remote_activities.get("shows", {}).get("dropped_at", self.base_date),
            self.activities["dropped_sync"],
        )

    def _sync_dropped(self):
        try:
            identities = trakt_dropped.read_dropped(self.trakt_api)
            with self._state_apply_guard():
                hidden.TraktSyncDatabase().replace_dropped_shows(identities)
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _finalize_process(self, update_time):
        if self.progress_dialog is not None:
            self.progress_dialog.close()
            del self.progress_dialog
            self.progress_dialog = None

        if not self.sync_errors and not getattr(self, "_pending_sync_skipped", False):
            self._update_activity_record("all_activities", update_time)
            xbmc.executebuiltin('RunPlugin("plugin://plugin.video.seren/?action=widgetRefresh&playing=False")')

    @staticmethod
    def _activity_section(name):
        name = name.lower()
        if "bookmark" in name:
            return "playback"
        if "watched" in name:
            return "history"
        if "collected" in name:
            return "collection"
        if "rating" in name:
            return "ratings"
        return "hidden"

    def _do_sync_acitivites(self, remote_activities):
        total_activities = len(self._sync_activities_list)
        for idx, activity in enumerate(self._sync_activities_list):
            self._current_activity_skipped = False

            try:
                update_time = str(self._get_datetime_now())

                if g.abort_requested():
                    return
                self.current_dialog_text = f"Syncing {activity[0]}"
                self._update_progress(int(float(idx + 1) / total_activities * 100))

                last_activity_update = remote_activities

                if activity[1] is not None:
                    for key in activity[1]:
                        last_activity_update = last_activity_update.get(key, {})
                    if not last_activity_update:
                        raise ActivitySyncFailure("Trakt omitted the activity timestamp")
                    if not self.requires_update(last_activity_update, self.activities[activity[2]]):
                        g.log(f"Skipping {activity[0]}, does not require update")
                        continue

                section = self._activity_section(activity[0])
                if section in getattr(self, "pending_sections", set()):
                    self._pending_sync_skipped = True
                    g.log(f"Preserving pending local Trakt {section} changes")
                    continue
                g.log(f"Running Activity: {activity[0]}")
                if hasattr(self, "trakt_api"):
                    self._active_sync_section = section
                    self._active_sync_revision = self.trakt_api.outbox.section_revision(self.trakt_api.write_queue.account, section)
                activity[3]()
                if getattr(self, "_current_activity_skipped", False):
                    continue
                self.startup_sync_changed += 1
                self._update_activity_record(activity[2], update_time)
            except PendingStateChanged:
                self._pending_sync_skipped = True
                continue
            except ActivitySyncFailure as e:
                g.log(f"Failed to sync activity: {activity[0]} - {e}")
                self.sync_errors = True
                continue
            finally:
                self._active_sync_section = None

    def _check_for_first_run(self, silent, trakt_auth):
        if not silent and str(self.activities["all_activities"]) == self.base_date and trakt_auth is not None:
            g.notification(g.ADDON_NAME, g.get_language_string(30177))
            # Give the people time to read the damn notification
            xbmc.sleep(500)
            self.silent = False
            self.progress_dialog = xbmcgui.DialogProgressBG()
            self.progress_dialog.create(f"{g.ADDON_NAME}Sync", "Seren: Trakt Sync")

    def _sync_movie_bookmarks(self):
        try:
            self._sync_bookmarks("movies")
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _sync_show_bookmarks(self):
        try:
            self._sync_bookmarks("episodes")
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _sync_hidden(self):
        try:
            db = hidden.TraktSyncDatabase()
            get = MetadataHandler.get_trakt_info
            sections = [
                ("calendar",),
                ("progress_watched",),
                ("progress_collected",),
                ("recommendations",),
            ]

            self._queue_with_progress(self._fetch_hidden_section, sections)
            items = self.mill_task_queue.wait_completion()
            rows = [(item.get("trakt_id"), get(item.get("season", item), "mediatype"), key)
                    for key, value in items.items() for item in value]
            from resources.lib.database import SQLiteConnection
            from resources.lib.database.state_delta import replace_rows
            with self._state_apply_guard(), SQLiteConnection(self._db_file) as connection:
                with connection.transaction(immediate=True) as cursor:
                    replace_rows(cursor, "hidden", ["trakt_id", "mediatype", "section"],
                                 ["trakt_id", "mediatype", "section"], rows, "section != 'dropped'")
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _fetch_hidden_section(self, section):
        items = []
        for paged_items in self.trakt_api.get_all_pages_json(f"users/hidden/{section}", ignore_cache=True):
            items.extend(paged_items)
        return {section: items}

    def _sync_watched_movies(self):
        try:
            # Trakt watched endpoints now require explicit pagination.
            trakt_watched = list(itertools.chain.from_iterable(
                self.trakt_api.get_all_pages_json(
                    "sync/watched/movies", extended="full", limit=250, ignore_cache=True
                )
            ))
            if not trakt_watched:
                self.execute_sql("UPDATE movies SET watched=0 WHERE watched != 0")
                return
            self.insert_trakt_movies(trakt_watched)
            watched_ids = {str(i.get("trakt_id")) for i in trakt_watched if i.get("trakt_id")}
            if watched_ids:
                ids = ",".join(watched_ids)
                self.execute_sql(
                    [
                        # Only touch rows whose state actually changes. This is
                        # important on startup: a large library should not be
                        # rewritten just because Trakt was checked.
                        f"UPDATE movies SET watched=0 WHERE watched != 0 AND trakt_id NOT IN ({ids})",
                        f"UPDATE movies SET watched=1 WHERE watched != 1 AND trakt_id IN ({ids})",
                    ]
                )
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _sync_collection_movies(self):
        try:
            # Trakt collection endpoints are paginated and capped at 250.
            trakt_collection = list(itertools.chain.from_iterable(
                self.trakt_api.get_all_pages_json(
                    "sync/collection/movies", extended="full", limit=250, ignore_cache=True
                )
            ))
            if not trakt_collection:
                self.execute_sql("UPDATE movies SET collected=0 WHERE collected != 0")
                return
            self.insert_trakt_movies(trakt_collection)
            collection_ids = {str(i.get("trakt_id")) for i in trakt_collection if i.get("trakt_id")}
            if collection_ids:
                ids = ",".join(collection_ids)
                self.execute_sql(
                    [
                        f"UPDATE movies SET collected=0 WHERE collected != 0 AND trakt_id NOT IN ({ids})",
                        f"UPDATE movies SET collected=1 WHERE collected != 1 AND trakt_id IN ({ids})",
                    ]
                )
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _sync_rated_movies(self):
        try:
            trakt_rated = self.trakt_api.get_all_pages_json(
                "/sync/ratings/movies", extended="full", limit=50, ignore_cache=True
            )
            trakt_rated = list(itertools.chain.from_iterable(trakt_rated))

            self.insert_trakt_movies(trakt_rated)

            get = MetadataHandler.get_trakt_info
            with self.create_temp_table(
                "_movies_rated", ["trakt_id", "user_rating"], primary_key="trakt_id"
            ) as temp_table:
                temp_table.insert_data(
                    [
                        {"trakt_id": get(movie, "trakt_id"), "user_rating": get(movie, "user_rating")}
                        for movie in trakt_rated
                    ]
                )
                self.execute_sql(
                    [
                        "UPDATE movies SET user_rating=NULL WHERE user_rating IS NOT NULL AND NOT EXISTS "
                        "(SELECT 1 FROM _movies_rated WHERE _movies_rated.trakt_id=movies.trakt_id)",
                        """
                        UPDATE movies
                        SET user_rating = (
                            SELECT user_rating
                            FROM _movies_rated
                            WHERE _movies_rated.trakt_id = movies.trakt_id
                        )
                        WHERE EXISTS (
                            SELECT user_rating
                            FROM _movies_rated
                            WHERE _movies_rated.trakt_id = movies.trakt_id
                        ) AND user_rating IS NOT (
                            SELECT user_rating FROM _movies_rated WHERE _movies_rated.trakt_id=movies.trakt_id
                        )
                        """,
                    ]
                )
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def sync_watched_episodes(self):
        try:
            get = MetadataHandler.get_trakt_info
            # Trakt's 2026 watched endpoint now separates lightweight watched
            # progress from full metadata.  Do not mill every watched show here:
            # that turns a watched-state sync into many /shows/{id}/seasons calls
            # and can leave Kodi parked on "Syncing Watched Shows".
            #
            # full,progress gives us the show record plus the complete watched
            # season/episode map.  Persist that map and let normal metadata
            # browsing/milling pick it up later via insert_trakt_episodes().
            trakt_watched = list(
                itertools.chain.from_iterable(
                    self.trakt_api.get_all_pages_json(
                        "sync/watched/shows",
                        extended="full,progress",
                        limit=100,
                        ignore_cache=True,
                        timeout=(5, 20),
                    )
                )
            )

            self._ensure_watched_episode_state_table()

            # A successful empty watched response means the account currently
            # has no watched episodes, so clear the persisted snapshot as well.
            watched_rows = []

            if trakt_watched:
                self.insert_trakt_shows(trakt_watched)
                watched_rows = [
                    (
                        show.get("trakt_id"),
                        get(season, "season"),
                        get(episode, "episode"),
                        get(episode, "playcount") or 1,
                        get(episode, "last_watched_at"),
                    )
                    for show in trakt_watched
                    for season in get(show, "seasons", [])
                    for episode in get(season, "episodes", [])
                    if show.get("trakt_id") is not None
                    and get(season, "season") is not None
                    and get(episode, "episode") is not None
                ]
            from resources.lib.database import SQLiteConnection
            from resources.lib.database.state_delta import replace_rows
            with self._state_apply_guard(), SQLiteConnection(self._db_file) as connection:
                with connection.transaction(immediate=True) as cursor:
                    replace_rows(cursor, "watched_episode_state",
                                 ["trakt_show_id", "season", "episode", "watched", "last_watched_at"],
                                 ["trakt_show_id", "season", "episode"], watched_rows)

            # Apply the lightweight watched snapshot first so existing rows are
            # immediately correct.  Then let Seren's normal metadata-completeness
            # check backfill only what is missing.
            #
            # Fresh database / first run: every watched show needs milling, so this
            # builds the complete seasons + episodes foundation required by Up Next.
            # Later syncs: completed shows are skipped locally and only genuinely new
            # or incomplete shows are fetched from Trakt.
            self._apply_watched_episode_state(reset_missing=True)
            if trakt_watched:
                previous_snapshot_state = getattr(self, "_watched_snapshot_current", False)
                self._watched_snapshot_current = True
                try:
                    self._mill_if_needed(trakt_watched, self._queue_with_progress)
                finally:
                    self._watched_snapshot_current = previous_snapshot_state
                # Milling creates episode rows, so re-apply the persisted watched
                # snapshot to the newly-created rows before statistics are calculated.
                self._apply_watched_episode_state(reset_missing=True)
        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def sync_collection_episodes(self):
        try:
            get = MetadataHandler.get_trakt_info
            # Collection shows are paginated too; keep the full response because
            # the sync below consumes season and episode collection data.
            trakt_collection = list(itertools.chain.from_iterable(
                self.trakt_api.get_all_pages_json(
                    "sync/collection/shows", extended="full", limit=250, ignore_cache=True
                )
            ))
            if not trakt_collection:
                self.execute_sql("UPDATE episodes SET collected=0,collected_at=NULL WHERE collected != 0")
                return

            self.insert_trakt_shows(trakt_collection)
            self._mill_if_needed(trakt_collection, self._queue_with_progress)

            with self.create_temp_table(
                "_episodes_collected",
                ["trakt_show_id", "season", "episode", "collected_at", "collected"],
                primary_key="trakt_show_id, season, episode",
            ) as temp_table:
                temp_table.insert_data(
                    [
                        {
                            "trakt_show_id": show.get("trakt_id"),
                            "season": get(season, "season"),
                            "episode": get(episode, "episode"),
                            "collected_at": get(episode, "collected_at"),
                            "collected": get(episode, "collected"),
                        }
                        for show in trakt_collection
                        for season in get(show, "seasons", [])
                        for episode in get(season, "episodes", [])
                    ]
                )

                self.execute_sql(
                    [
                        """
                        UPDATE episodes
                        SET collected=0, collected_at=NULL
                        WHERE collected != 0
                          AND NOT EXISTS (
                              SELECT 1 FROM _episodes_collected
                              WHERE _episodes_collected.trakt_show_id = episodes.trakt_show_id
                                AND _episodes_collected.season = episodes.season
                                AND _episodes_collected.episode = episodes.number
                          )
                        """,
                        """
                        UPDATE episodes
                        SET (collected, collected_at) = (
                            SELECT collected, collected_at
                            FROM _episodes_collected
                            WHERE _episodes_collected.trakt_show_id = episodes.trakt_show_id
                                AND _episodes_collected.season = episodes.season
                                AND _episodes_collected.episode = episodes.number
                        )
                        WHERE EXISTS(
                            SELECT 1 FROM _episodes_collected
                            WHERE _episodes_collected.trakt_show_id = episodes.trakt_show_id
                                AND _episodes_collected.season = episodes.season
                                AND _episodes_collected.episode = episodes.number
                              AND (episodes.collected IS NOT (SELECT collected FROM _episodes_collected WHERE _episodes_collected.trakt_show_id = episodes.trakt_show_id AND _episodes_collected.season = episodes.season AND _episodes_collected.episode = episodes.number)
                                   OR episodes.collected_at IS NOT (SELECT collected_at FROM _episodes_collected WHERE _episodes_collected.trakt_show_id = episodes.trakt_show_id AND _episodes_collected.season = episodes.season AND _episodes_collected.episode = episodes.number))
                        )
                        """,
                    ]
                )

            self.update_shows_statistics(trakt_collection)
            self.update_season_statistics(
                self.fetchall(
                    f"""
                    SELECT trakt_id FROM seasons
                    WHERE trakt_show_id IN ({','.join({str(i.get('trakt_id')) for i in trakt_collection})})
                    """
                )
            )

        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e) from e

    def _sync_rated_shows(self):
        try:
            trakt_rated_shows = self.trakt_api.get_all_pages_json(
                "sync/ratings/shows", extended="full", limit=50, ignore_cache=True
            )
            trakt_rated_shows = list(itertools.chain.from_iterable(trakt_rated_shows))
            self.insert_trakt_shows(trakt_rated_shows)

            trakt_rated_seasons = self.trakt_api.get_all_pages_json(
                "sync/ratings/seasons", extended="full", limit=50, ignore_cache=True
            )
            trakt_rated_seasons = list(itertools.chain.from_iterable(trakt_rated_seasons))
            self.insert_trakt_shows(i.get("show") for i in trakt_rated_seasons)

            def fetch_rated_episodes(rating):
                return self.trakt_api.get_json(
                    f"sync/ratings/episodes/{rating}", extended="full", no_paging=True, timeout=90
                )

            self._queue_with_progress(fetch_rated_episodes, [(i,) for i in range(1, 11)])
            trakt_rated_episodes = self.mill_task_queue.wait_completion()
            self.insert_trakt_shows(i.get("show") for i in trakt_rated_episodes)

            self._mill_if_needed(
                trakt_rated_shows + trakt_rated_seasons + trakt_rated_episodes, self._queue_with_progress
            )

            get = MetadataHandler.get_trakt_info

            with self.create_temp_table(
                "_shows_rated", ["trakt_id", "user_rating"], primary_key="trakt_id"
            ) as temp_table:
                temp_table.insert_data(
                    [
                        {"trakt_id": get(show, "trakt_id"), "user_rating": get(show, "user_rating")}
                        for show in trakt_rated_shows
                    ]
                )
                self.execute_sql(
                    [
                        "UPDATE shows SET user_rating=NULL WHERE user_rating IS NOT NULL AND NOT EXISTS "
                        "(SELECT 1 FROM _shows_rated WHERE _shows_rated.trakt_id=shows.trakt_id)",
                        """
                        UPDATE shows
                        SET user_rating = (
                            SELECT user_rating
                            FROM _shows_rated
                            WHERE _shows_rated.trakt_id = shows.trakt_id
                        )
                        WHERE EXISTS (
                            SELECT user_rating
                            FROM _shows_rated
                            WHERE _shows_rated.trakt_id = shows.trakt_id
                        ) AND user_rating IS NOT (
                            SELECT user_rating FROM _shows_rated WHERE _shows_rated.trakt_id=shows.trakt_id
                        )
                        """,
                    ]
                )

            with self.create_temp_table(
                "_seasons_rated", ["trakt_id", "user_rating"], primary_key="trakt_id"
            ) as temp_table:
                temp_table.insert_data(
                    [
                        {"trakt_id": season.get("trakt_id"), "user_rating": get(season.get("season"), "user_rating")}
                        for season in trakt_rated_seasons
                    ]
                )
                self.execute_sql(
                    [
                        "UPDATE seasons SET user_rating=NULL WHERE user_rating IS NOT NULL AND NOT EXISTS "
                        "(SELECT 1 FROM _seasons_rated WHERE _seasons_rated.trakt_id=seasons.trakt_id)",
                        """
                        UPDATE seasons
                        SET user_rating = (
                            SELECT user_rating
                            FROM _seasons_rated
                            WHERE _seasons_rated.trakt_id = seasons.trakt_id
                        )
                        WHERE EXISTS (
                            SELECT user_rating
                            FROM _seasons_rated
                            WHERE _seasons_rated.trakt_id = seasons.trakt_id
                        ) AND user_rating IS NOT (
                            SELECT user_rating FROM _seasons_rated WHERE _seasons_rated.trakt_id=seasons.trakt_id
                        )
                        """,
                    ]
                )

            with self.create_temp_table(
                "_episodes_rated", ["trakt_id", "user_rating"], primary_key="trakt_id"
            ) as temp_table:
                temp_table.insert_data(
                    [
                        {"trakt_id": episode.get("trakt_id"), "user_rating": get(episode.get("episode"), "user_rating")}
                        for episode in trakt_rated_episodes
                    ]
                )
                self.execute_sql(
                    [
                        "UPDATE episodes SET user_rating=NULL WHERE user_rating IS NOT NULL AND NOT EXISTS "
                        "(SELECT 1 FROM _episodes_rated WHERE _episodes_rated.trakt_id=episodes.trakt_id)",
                        """
                        UPDATE episodes
                        SET user_rating = (
                            SELECT user_rating
                            FROM _episodes_rated
                            WHERE _episodes_rated.trakt_id = episodes.trakt_id
                        )
                        WHERE EXISTS (
                            SELECT user_rating
                            FROM _episodes_rated
                            WHERE _episodes_rated.trakt_id = episodes.trakt_id
                        ) AND user_rating IS NOT (
                            SELECT user_rating FROM _episodes_rated WHERE _episodes_rated.trakt_id=episodes.trakt_id
                        )
                        """,
                    ]
                )

        except PendingStateChanged:
            raise
        except Exception as e:
            raise ActivitySyncFailure(e)

    def _filter_lists_items_that_needs_updating(self, requested):
        # TODO: This is never called, the query is also broken and we don't seem to sync lists
        if len(requested) == 0:
            return requested

        get = MetadataHandler.get_trakt_info
        query = """WITH requested(trakt_id, meta_hash, updated_at) AS (VALUES {}) select r.trakt_id as trakt_id from
        requested as r left join lists as db on r.trakt_id == db.trakt_id  where db.trakt_id IS NULL or (Datetime(
        db.last_updated) < Datetime(r.updated_at)) or db.list_type == 'Unknown' """.format(
            ",".join(
                f"""({i.get("trakt_id")}, '{self.trakt_api.meta_hash}', '{i.get("dateadded", get(i, "dateadded"))}')"""
                for i in requested
                if i.get("trakt_id")
            )
        )

        result = {r["trakt_id"] for r in self.fetchall(query)}
        return [r for r in requested if r.get("trakt_id") and r.get("trakt_id") in result]

    def _update_progress(self, progress, text=None):
        home = xbmcgui.Window(10000)
        if home.getProperty("BOOP.TraktStartupSync") == "running":
            home.setProperty("Seren.StartupText", text or self.current_dialog_text)
        if not self.silent:
            if text:
                self.progress_dialog.update(progress, text)
            else:
                self.progress_dialog.update(progress, self.current_dialog_text)

    def _update_activity_record(self, record, time):
        self.execute_sql(f"UPDATE activities SET {record}=? WHERE sync_id=1", (time,))

    def _sync_bookmarks(self, bookmark_type):
        from resources.lib.database import SQLiteConnection
        from resources.lib.database.state_delta import replace_rows
        get = MetadataHandler.get_trakt_info
        # Read and validate every page before modifying local state.
        progress = list(itertools.chain.from_iterable(self.trakt_api.get_all_pages_json(
            f"sync/playback/{bookmark_type}", extended="full", timeout=(4, 10), limit=50, ignore_cache=True)))
        rows = []
        for item in progress:
            percent = get(item, "percentplayed") if bookmark_type == "movies" else item.get("percentplayed")
            obj = item if bookmark_type == "movies" else item.get("episode", {})
            duration = get(obj, "duration")
            ident = item.get("trakt_id") or obj.get("trakt_id")
            paused = get(item, "paused_at") if bookmark_type == "movies" else item.get("paused_at")
            if ident and percent and 0 < float(percent) < 100 and duration:
                rows.append((ident, int(float(percent) / 100 * duration), percent, bookmark_type[:-1], paused))
        # An action may have been queued while the remote pages were fetched.
        if "playback" in self.trakt_api.outbox.pending_sections(self.trakt_api.write_queue.account):
            self._pending_sync_skipped = True
            self._current_activity_skipped = True
            return
        with self._state_apply_guard(), SQLiteConnection(self._db_file) as connection:
            with connection.transaction(immediate=True) as cursor:
                replace_rows(cursor, "bookmarks", ["trakt_id", "resume_time", "percent_played", "type", "paused_at"],
                             ["trakt_id"], rows, "type=?", (bookmark_type[:-1],))
        if bookmark_type == "movies":
            self.insert_trakt_movies(progress)
        else:
            shows = [item["show"] for item in progress if item.get("show")]
            self.insert_trakt_shows(shows)
            self._mill_if_needed(shows, mill_episodes=True)

    def _queue_with_progress(self, func, args):
        for idx, arg in enumerate(args):
            self.mill_task_queue.put(func, *arg)
            progress = int(float(idx + 1) / len(args) * 100)
            self._update_progress(progress)
