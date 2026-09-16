# BOOP durable memory: shared implementation, separate Wall and Shield apps

Updated 2026-09-16. Current owner is `boop-wall-shield-split-v207`; this filename remains for continuity. One repository, one maintained shared assistant and existing shared render/media libraries, two application shells. Never resurrect the older standalone implementations when fixing these packages.

## Delivered applications and lineage

Wall `com.boop.alpha1`, `207` / `1.2.207-wall`, was clean-installed only on Pixel 7 Pro. Shield `com.boop.shieldoverlay`, `207` / `1.2.207-shield`, was clean-installed only on Nvidia Shield. Installed build source `aa8fd9f6d79f28b441a48df31138a75d38420118`, signed run `35110823569`, artifact `10451993779`. Exact hashes, permanent signature, recovery, first-screen and subsequent-state evidence are in `docs/handoffs/2026-09-16-v207-signed-clean-install.md`.

The accepted v206 combined source is retained, including all subsequent accepted UI fixes. Original owner `boop-hand-colour-v191` remains at `841458b8bbc53773d16a18bb0359de1c0550f5e2`; its dirty local v203 documents were preserved. Fetch current main and application heads before changes. Main is a routing hub, not the app source.

## Preserved Voice and character

Voice is completely frozen in both shells: natural backend, model download/choices, Android fallback, existing pitch/cadence, inference settings and sharing. Ryan deferred the roughly 12-second latency investigation and alternative sources/voices for Astra later. This is neither a fix claim nor a request to revert to pre-tuning code. Do not introduce Voice tests as another split prerequisite.

Preserve the photographic felt master, rounded moving lids, original animation timing, coded animations, free independent eye/felt/hand controls, five-digit anatomy and shared appearance/speed choices. Preserve accepted v206 Home: four top controls, fixed parked favourites, equal visible spacing, no Favourite apps heading, large bottom-right idle BOOP when Now Playing disappears. No Home/art/Voice source replacement was made by the split. All 16 native libraries in both APKs match the signed v206 reference byte-for-byte.

## Routing and package-sensitive contracts

The installed application ID owns the body. No device/profile chooser remains; historical override preferences cannot turn Wall into Shield. Wall retains its built-in phone launcher and shared assistant functions. Shield opens TV/Home. Room, voice and access settings survive. BOOP routing does not grant Android privileges or force Android's Home role.

Keep Java namespaces and semantic action/protocol IDs distinct from Android application IDs. Most explicit class routing already uses getPackageName with fully qualified shared classes. The launcher self-filter uses the process UID, tested with the actual generated repository for both shells.

Shield alone owns legacy authority `com.boop.alpha1.johnny_states` plus `com.boop.shieldoverlay.johnny_states`. The current signed Johnny consumer queries/calls the legacy provider authority; retain compatibility without modifying Johnny. The fixed read-only snapshot and caller package/certificate guard are unchanged. Do not expose credentials or silently broaden HA scope.

## Newcomer test and observed subsequent progress

Both old Unified installs were deliberately removed without keeping data. No old app data, voice models, HA credentials, setup flags or access grants were restored. Both new apps initially reached their real first setup screens with incomplete flags; no Continue, sign-in or completion-flag write was issued by this sequence.

Pixel 7 remained at initial setup in the latest read. Shield later had enabled BOOP listener/overlay access; this sequence cleared only those grants for the fresh-access brief, preserving unrelated listeners. A following read showed Shield setup completed and YouTube foreground. The actor responsible was not established. Do not reset, reinstall, regrant or force it back to first setup. No further device input was sent after noticing the advancement. Report this honestly instead of asserting both still sit at step one.

## Working boundaries

Source edits, focused tests, builds, permanent signing and durable publication stay on GitHub. Desktop Commander on Yoga handles authorized device work with existing tools. Do not upgrade the pinned bridge or demand Work mode. Pixel 10 stays entirely outside routine work. No automatic emulator, autonomous visual acceptance or blind merge/reset.

Build checks, installed hashes, captured first screens and Ryan's physical acceptance are separate. Onboarding and later HA/media/notifications/sharing/Johnny behavior still require joint acceptance; existing source tests do not certify them. Preserve current user/device progress. Full historical root documents remain byte-for-byte in `docs/handoffs/2026-09-16-v207-before-split/`, alongside older dated archives and feature receipts.
