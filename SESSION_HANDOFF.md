# Puppet integration: implementation saved, NOT deployed

Updated 2026-09-13. Task branch/worktree: `boop-unified-puppet-integration`.
Base: `boop-unified-v146-integration@2e4a6835c321cc3af980b5cc1e940f495ba5467a`.
Both physical devices were verified on v147 before this task. They have NOT been
updated by this task. Do not call candidate v148 physically accepted or finished.

## Current source and verification
Implementation commit `ce484b4` adds single-owner surfaces, Lab sign scenes,
15%-faster blink phase, independent animation speed, shared iris/voice controls,
appearance-only LAN registers, an optional HA mirror and notification intake fixes.
Follow-up `fca1b90a4262e1e14ebf3d11e87336ed5e906347` updates two old voice source-test
contracts for the explicitly requested independent pitch while retaining PCM16,
MODE_STATIC, STATE_NO_STATIC_DATA and truthful backend-failure safeguards.

Local Android compilation and an emulator-only debug APK build succeeded.
Five focused Java harnesses passed after observed failures: clock/cover ownership,
blink-only timing, five-device state convergence/conflicts/reconnect/validation,
voice range/defaults and notification app/category selection. 27 focused Python
contracts passed. The added tests are also wired into the normal Unified workflow.
Catalogue, eye/hand masters and shader source have no Git changes. Existing
animations are preserved; only bilateral blink phases intentionally run 1.15x.

Signed CI run `34736930258` at ce484b4 FAILED before signing. Materialization,
canonical animation, shared-state and Startup Manager checks passed. Failure was
two old source tests forbidding all PlaybackParams and matching the old playback
method signature. fca1b90a repairs those expectations; a fresh full CI run is still
required. Never bypass the remaining checks or deploy the local emulator signer.

## Confirmed causes, not guesses
The primary canonical face uses a topmost GLSurfaceView. Ordinary menu overlays
do not hide it. Voice/developer entry did not relinquish that primary surface.
The developer notification preview also constructed a separate face above its
notification view. The patch introduces independent cover owners and one complete
canonical sign scene rather than stacking the old and new renderers.

The physical phone had notification-listener access, but every live event failed
app/category selection because enabled apps were populated while enabled channels
were empty. Display-over-other-apps was also not allowed. Migration now enables
new categories only for already selected apps, without opting in unrelated apps;
setup exposes the missing Android overlay permission. Actual end-to-end live
notification presentation still needs emulator and physical verification.

## Emulator/device evidence and its limits
Task-owned emulator: `emulator-5590`, Pixel 10 AVD, -read-only, disposable changes.
Only that instance's old BOOP installation was removed for a local test signer.
No physical uninstall, data clear, signer replacement or HOME change occurred.
The emulator initially reported a System UI ANR; dismissing its system dialog
allowed inspection. Voice Settings was photographed without the floating eyes.
Developer/notification inspection is NOT complete: a later tool-entry attempt
still showed Voice Settings. Do not mistake that capture for a developer-menu pass.
The test emulator temporarily uses 540x1200/240dp; cold restart/resources and the
normal device layout must be checked. Other sessions own emulators 5562 and 5564.
Do not stop, wipe or reconfigure those other instances.

## Connection failure at interruption
Yoga presence/ping can report online, but repeated read_file, write_file,
start_process and list_sessions calls return `Not connected`. This is a terminal/
file-channel failure, not proof the laptop is powered off or ADB was disabled.
The last write of work/ci_corrections.py FAILED; do not assume it exists.
GitHub publication remains available; fca1b90a and this handoff were saved remotely.
Fetch the task branch before resuming local edits so those commits are preserved.

## Exact continuation
1. Re-establish working file/terminal calls; fetch/check live task branch and
   fast-forward the clean tracked task changes. Preserve all other worktrees.
2. In the final ownership patch, have tool-entry runnables close the other menu
   before opening Voice/Developer. Investigate first-launch voice setup ordering
   using actual UI, not a pretend dev animation hook. The patch already covers
   primary GL surfaces and removes the separate notification preview face.
3. Review/apply HA-merge error reconciliation: a successful state.merge followed
   by a failed HA POST must still apply the merged state locally/over LAN.
4. Run `scripts/test-puppet-core.py`, focused contracts and the full existing
   `build-boop-unified.yml` via workflow_dispatch. Resolve any further failures.
5. Finish the separate Lab v13 sender/receiver test. Its existing signed build is
   `animation-lab-system-scale-poc@57c1bdf932fa739b27bbd353e27e6f9d090ce262`,
   Actions `34733070034`. It was downloaded, not physically verified by this task.
6. On the isolated emulator post genuine Android notifications, then use Shield
   Lab to send them to Pixel Lab. Select the Lab app in BOOP notification settings
   and verify the original listener path, privacy, PendingIntent and dismissal.
7. Install only the permanent-signed Unified candidate with adb install -r on
   Shield and Pixel. Verify pulled APK hash/version, inspect all three reported
   duplicate screens, animations at Android 0x, bidirectional colour/voice sync,
   both speech engines and Home/Back/lifecycle. Preserve credentials and voice packs.
8. Publish exact receipts and update current notes after real verification.

## Local aids and boundaries
Task work/ contains session.py helpers, native materialize_native.py, pytest
work/test-deps, logs and private screenshots. The native materializer executes
existing patches with runpy and catches only successful SystemExit. It reuses the
installed SDK/model caches; do not redownload or rebuild project archaeology.
Three shield-overlay input files become dirty during standard materialization;
those generated side effects were excluded from the candidate commit. Other
sessions' worktrees and original dirty inputs were not edited.

Sharing currently exchanges ONLY bounded hue/pitch/rate/speed, no arbitrary
commands, account credentials or notification content. LAN sharing is intended
for a trusted home network and is not authenticated; the settings UI states this
and provides an off switch. HA uses existing paired OAuth and an owned marked
sensor state, falling back to LAN. Real two-device/HA behavior remains unverified.
User asked for no progress interruptions and autonomous completion; do not ask
for design approval again or claim deployment without fresh device evidence.
