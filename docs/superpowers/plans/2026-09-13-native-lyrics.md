# Native synchronized lyrics implementation plan

**Goal:** Replace the fragile Deezer foreground/menu macro with a BOOP-owned, polished full-screen music/lyrics presentation.
**Base:** Accepted Unified v156, `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`.
**Owner:** `boop-unified-native-lyrics-v157`. GitHub owns source, review, tests, signing and APK builds. Do not import unfinished local v157 drafts.
**Spec:** Ryan approved the design in this conversation: integrated artwork/track identity, large readable lyrics, restrained cyan highlight, smooth scrolling, automatic track changes within the music view, no dialog/window chrome and no foreground Deezer dependency.
**Tech stack:** Existing Java/Android views, MediaSession, bounded HTTPS transport and permanent GitHub signing. No new playback engine, account login, permissions or artwork.

## Current evidence (2026-09-13)

- GitHub live v156 reference was read and the new branch was created from that exact SHA.
- Current main BOOP_START_HERE confirms v156 is the accepted Shield successor. Its acceptance supersedes the older pending wording in the v156 handoff.
- The v142 lyrics handoff records exact-ID anonymous availability tests on Shield. Its `/lyrics/<id>` experiment could restore an already-open screen and did not prove a durable deep link. Do not repeat that macro path.
- Existing DeezerLyricsClient requests only `__typename` for synchronized lists. It does NOT retrieve words/timings. Its availability success was not native-rendering evidence.
- A fresh anonymous live probe on the laptop returned 68 timed lines for public track ID 3135556, with numeric milliseconds/duration, nonempty text and provider credit. No account credentials were read; no token or lyric text was printed or retained in the repository.
- Live GraphQL type inspection confirms Lyrics exposes synchronizedLines, synchronizedWordByWordLines, text, copyright, writers and licence. The line query is also present in Music Assistant's maintained source.
- The initial PowerShell schema JSON deserialization failed on the `__type` property; fetching raw response text resolved that probe issue. This was not a Deezer failure.
- Shield and the Android TV emulator are connected. Neither phone is a target.
- No native lyrics APK has been built or installed yet. API success is not a supported-API promise, full-catalogue guarantee or visual acceptance.

## Constraints

Preserve accepted artwork, canonical eye animation/hue, favourites, artist/album links, transport controls, 10-second seeking, thin corners, stable mascot bay, 250ms long-Back and permanent signer. No GitHub screenshot/golden-image/visual checks. Functional tests and source/package/security checks are permitted. Runtime UI and remote tests run on the laptop TV emulator, then Shield. Never install on either phone, clear data or silently change permissions. No credentials, copyrighted lyrics, raw device dumps or personal screenshots in the public repository.

## Implementation and verification

1. Add test-first coverage for strict timed-document parsing, recording identity, malformed/error responses, empty/instrumental data, timing boundaries and gaps, and stale request ownership. Fixtures contain invented test phrases only. Prove a red assertion before completing the implementation.
2. Add a bounded native timed-lyrics client using the already-proven anonymous session and HTTPS transport. Keep availability client behavior/tests intact. Parse only fields verified from live schema; distinguish UNAVAILABLE from UNKNOWN. Retain licence/copyright attribution in the presentation.
3. Replace DeezerLyricsBrowser's positive macro with a BOOP activity handoff only after valid timed data is available. Preserve negative toast/remaining-in-BOOP behavior. Use a bounded in-memory cache; no permanent lyrics store.
4. Add an unexported Shield lyrics activity and borderless view. Subscribe to the existing selected MediaSession, follow pause/resume/seek/track changes, discard stale network results, clear previous-track lyrics immediately, stop network/drawing when hidden, resynchronize on resume. Do not seize audio focus.
5. Run focused GitHub logic tests and the existing non-visual build/test/sign/package pipeline. Do not weaken a failure or treat a compile error as a passing test. Review source diff and intended permissions/artwork scope.
6. Download the exact signed artifact for local TV emulator tests: entry/Back, line transitions, pause/resume, forward/back seeks, track changes, unavailable lyrics, stale replies and process recreation as practical. Then install/test on Shield, verify installed version and APK hash, and test returning after Deezer loses foreground. Keep synthetic versus real media evidence separate.
7. Update SESSION_HANDOFF, BOOP_STATUS and applicable memory with exact commits, run/artifact/signer/hash receipts, verification boundaries and remaining limitations. Verify the live feature-branch HEAD. Do not merge into accepted v156 or advertise an unverified candidate as accepted.

## Research references

- https://developer.android.com/reference/android/media/session/PlaybackState
- https://developer.android.com/reference/android/media/session/MediaController
- https://newsroom-deezer.com/2020/03/deezer-super-sizes-lyrics/
- https://github.com/music-assistant/deezer-python-gql/blob/main/queries/get_track.graphql

The Cast screen is a behavioral/design reference. Its receiver implementation has not yet been independently inspected in this continuation; do not claim it was copied or proven equivalent.
