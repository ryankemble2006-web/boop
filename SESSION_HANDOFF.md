# Accepted Shield / Now Playing checkpoint: v156

Updated 2026-09-13. Owner: `boop-unified-artist-link-v156`.
Source remains `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`; package `com.boop.alpha1`, version `156 / 1.2.156-artist-link`.
GitHub signed run `34751618661` succeeded; artifact `10316785152` (`BOOP-Unified`).
APK SHA256 `e99c0275f3f636255994511dce921a5a1fa09225d39b569c725e6cc757b75e7c`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
The earlier recovery verified Shield's installed version and APK hash against this artifact.

Ryan accepted the completed Shield sweep: canonical notification/Now Playing renderers, thin rounded artwork frames, 10-second Left/Right seeking, Down from progress to Pause, reserved puppet bay (no seek flash), 250 ms settings hold, favourite right-edge stop, BOOP-owned + favourites selector, and cyan-focus artist navigation into Deezer. Do not repeat these as pending acceptance gates.

Recovered local TV fixture evidence exercised Pause -> Progress -> Artist, the artist-action callback, cyan focus and return navigation. Ryan supplied the physical acceptance; this does not assert every possible provider/notification case has been tested.

## Next work, separately owned
Shared eye colour first, then animation speed. Preserve existing Wall hue controls, iris-only shader, authored animations and exact 1x timing. GitHub owns source/build/test/signing; local emulators and real devices own runtime and visual checks. Pixel 10 remains excluded from deployment until Ryan explicitly resumes it.

Unfinished drafts are now preserved on GitHub, not accepted releases:
- `wip/boop-colour-v158-recovery` at `484d292fc5a0f72e2e73603e7084713b6ec660b5`: incompatible overlapping binding edits.
- `wip/boop-colour-v159-recovery` at `7ea7d26d10ae2ffb50afcd77a983047f508ad419`: reviewed binding direction and protocol tests, but runtime class is incomplete, app wiring/settings absent, and no build/deployment acceptance.
Native lyrics remains separately owned; do not merge it implicitly.

This update is documentation only. Detailed earlier implementation/build history remains in `a901c1e9:SESSION_HANDOFF.md` and its parent commits; this current checkpoint supersedes their stale pending-delivery statements.
