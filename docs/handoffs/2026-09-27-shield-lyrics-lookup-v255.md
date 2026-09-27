# Shield255: editable lyrics lookup

Ryan physically accepted254's lyrics lookup and speed, then requested a Lookup button with editable text for removing remaster/version suffixes. Branch `boop-shield-lyrics-lookup-v255` is based on254 owner `170d6b6a`.

The lyrics screen adds Lookup beneath the lyrics column. The remote can reach it from transport controls or Queue. Its dialog has prefilled Track title and Artist fields, Search and Cancel. Failed Home preflight opens the same editor. Editing creates a temporary query copy; it does not change the media session, title display, playback position or controls. Explicit searches bypass the successful-document cache. LRCLIB remains primary and Deezer fallback. Successful corrected lyrics are cached for the original recording under the existing five-minute cache lifetime; corrections are not written to settings.

Track changes and lifecycle cancellation dismiss the editor and invalidate pending results. Recording identity includes album and duration on the metadata-only route. Android's asynchronous dialog dismissal is covered so it cannot suppress or erase the next request.

Tests were written first and failed for missing fresh-query support, missing metadata-copy support, inaccessible Home/Activity editors, asynchronous dismissal, and stale metadata-only recording edits. Focused checks now pass. Review findings were reproduced and fixed. Android preview and signed CI validation are ongoing. The Android fixture uses synthetic metadata and no playback commands. Physical Shield controls have not been operated for this feature.
