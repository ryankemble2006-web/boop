# Unified v162 native lyrics integration: verification in progress

Updated 2026-09-13. Owner `boop-unified-native-lyrics-v162`.
Ryan explicitly requested integrating the tested lyrics feature into v161 as v162,
replacing the existing Now Playing Lyrics button route, and removing the Lab only
once the integrated feature is confirmed. This supersedes the Lab's earlier wait.

## Exact inputs and scope

Unified base: `593ad609ff87f651d5273bd17f5a2c0ca3ef5198`, accepted v161 from
`boop-unified-eye-sync-safe-v159`. App/build source `0b6ee6f91e05f00138a94ec2c9fd846117020754`.
Ryan accepted speed on both Shield and Pixel7 and automatic two-way eye colour.
Speed remains device-local. These accepted source files are not changed here.

Lyrics input: `5ce581be6f1da10eb47640c4c11636a4bfa9e330`, source branch
`boop-lyrics-lab-side-by-side-v157`, lab v159 footer source `e6f9bbb736ac90287815bea3a9c48496505675ed`.
Ryan confirmed manual Skip updates lyrics while remaining inside Lab. Existing
player controls/design are accepted. The newer footer removes Back to Now Playing
and places the full provider credit at bottom right at half its earlier size.
The main lyric typography is not halved.

## Integration

The eight shared/native entry/Activity source files are copied by exact Git blob,
including the corrected layout and footer. Unified's existing manager/state bus
and launcher remain unchanged. The existing Lyrics button now follows the internal
native activity through the browser adapter, not a Deezer menu macro or Lab intent.
The internal Activity is unexported, with no new permission, service, external
intent filter, account credentials, dependency or permanent lyric storage.
Do not import the separate Lab observer/shell/notification listener: Unified
already keeps active-token callbacks across temporary non-displayable states.

## Verification status at implementation

Saved test-first run `34773211530`, source `73fe0ee0d94c9a5fdac464265b8cdd2a9a51ae3d`,
failed because v161 still used the old macro and lacked the native activity/data
classes. This is the expected regression baseline, not a broken delivery.
The integration adds immutable-source/v161-scope checks, retained native timed-data
and entry tests, and internal Activity/state/lifecycle tests at Android boundaries.
The existing full signed pipeline now runs the extra checks without replacing any
v161 test step. Passing tests/build/install are pending at this checkpoint.

## Delivery and cleanup boundary

GitHub source/non-visual checks/build/signing, then requested Shield installation
and joint confirmation with Ryan. No automatic emulator or hosted visual gate.
Do not touch either phone for this Shield-lyrics delivery. Keep approved settings,
artwork, coded animations, single-face ownership and existing permanent signer.
Retain installed `com.boop.lyricslab` until Ryan confirms v162's Now Playing Lyrics
screen works. Do not uninstall it or delete its GitHub source branch/history yet.
This is not permission to clear data, change grants, send playback keys or reset
settings. The integration's physical acceptance remains distinct from Lab's pass.

Previous v161 handoff/status/memory remain at the pinned base commit and in its
dated acceptance receipts. Full integration plan:
`docs/superpowers/plans/2026-09-13-unified-v162-native-lyrics.md`.
Current shared workflow: `main@24a260b6e7cdd5aed792ccfbb683e8e495eb5f80:BOOP_START_HERE.md`.
No local source checkout is modified or claimed synchronized. Keep private device
addresses, credentials, APKs, screenshots and diagnostics outside public Git.
