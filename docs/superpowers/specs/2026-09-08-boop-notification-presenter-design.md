# BOOP Notification Presenter Design

Date: 2026-09-08
Status: user-approved design, implementation not started
Owning app: canonical `boop-unified`
Package: `com.boop.alpha1`
Related visual reference: `animation-lab/lockscreen-notifications/README.md` on `animation-freddie-mercury`

## Purpose

Give BOOP an opt-in, phone-wide notification presentation system that can wake the display, interrupt the user with BOOP's established eyes and floating yellow hands, group notification bursts, and open the original notification target while leaving Android's real notification state intact except for normal tap/open semantics.

This is a presentation layer over Android notifications, not a replacement notification database or a lock-screen replacement.

## Product rules

- Notification presentation is opt-in per app.
- Inside an allowed app, the user can choose Android notification channels/categories where available.
- Granting Notification Access or overlay permission does not automatically allow any app.
- Swiping BOOP away dismisses only BOOP's presentation. The underlying Android notification remains.
- Tapping a single notification uses its original `PendingIntent` and should preserve Android's normal auto-cancel behavior where applicable.
- Tapping a mixed-app bundle opens BOOP's own full-screen inbox first.
- Message-bomb bursts are grouped into one bundle rather than repeatedly interrupting the user.
- BOOP replaces the audible/vibration presentation for BOOP-managed channels. Android's original channel is guided to silent by the user.
- Notification controls live under the existing Voice Settings UI, not as a new top-level settings area.
- The display timeout is user configurable, with an initial default of eight seconds.
- Locked presentation is privacy safe. Unlocked presentation may show richer content.
- BOOP never silently grants permissions, modifies unrelated channels, cancels notifications on swipe, marks messages read, or bypasses Android keyguard security.

## Recommended architecture

Use a `NotificationListenerService` plus a small notification coordinator and two presentation surfaces.

### 1. Notification intake

A BOOP-owned `NotificationListenerService` receives posted and removed notifications. It normalizes only the metadata needed by the presenter, then immediately applies the user's app and channel allowlist.

Notifications from disallowed apps or channels are ignored by BOOP and remain entirely under Android's normal behavior.

The listener is not a second mailbox. It does not persist message bodies, sender history, or a shadow archive.

### 2. Notification coordinator

A single coordinator owns presentation policy and state. Its responsibilities are:

- app and channel allowlisting;
- deduplication;
- a rolling burst/grouping window;
- locked versus unlocked redaction;
- current foreground/presentation state;
- user-configured timeout;
- active BOOP bundle membership;
- rebuild from Android's currently active notifications after process restart;
- dispatch to the correct presentation surface;
- preserve the original Android notification lifecycle.

The initial burst window is four seconds. Notifications arriving during an active bundle update that bundle instead of restarting the entrance animation or replaying the sound for every message.

### 3. Unlocked presenter

When the phone is unlocked and another app is in the foreground, BOOP interrupts with a full-screen BOOP presentation above the current app using Android's supported overlay capability.

The presentation is intentionally interruptive. The user can:

- tap the presented notification to open its original target;
- tap a bundle to enter BOOP's inbox;
- swipe BOOP away to return to the previous app without altering Android's notification;
- ignore BOOP and allow the configurable timeout to remove the presentation.

If BOOP itself or Wall mode is already visible, the same presenter appears in place instead of layering a redundant second BOOP surface.

### 4. Locked and screen-off presenter

For an allowed notification while the display is off, BOOP wakes the display and presents a dedicated privacy-safe Activity above the lock screen using Android-supported screen-on/show-when-locked behavior.

BOOP does not replace, dismiss, or bypass the keyguard. It does not use alarm/call full-screen-intent behavior merely to force notification UI.

Locked presentation exposes only:

- application identity/icon;
- aggregate count where relevant;
- BOOP's visual presentation.

No sender name, message text, account detail, contact photo, or message preview is shown while locked.

If the device becomes unlocked while the presentation is active, the presenter transitions to the richer unlocked card state without replaying the entrance sound.

When an ignored locked presentation times out, BOOP finishes its Activity and releases any wake hold it owns. BOOP does not request privileged device-power control solely to force the display off; normal Android lock-screen/screen timeout behavior resumes immediately after BOOP stops holding the presentation awake.

### 5. BOOP inbox

A bundle spanning one or more applications opens into BOOP's own full-screen inbox. The inbox is assembled from notifications Android still reports as active.

It may show app identity, title and message content only while unlocked. Each item retains its original `PendingIntent` so tapping it opens the source application's intended destination.

The inbox does not become a durable notification archive. When Android removes an active notification, BOOP removes the corresponding inbox item.

## Notification tap semantics

Swiping or timing out BOOP never calls the notification-listener cancellation APIs.

A successful tap sends the original notification `PendingIntent`. If the source notification carries Android's `FLAG_AUTO_CANCEL`, BOOP then requests cancellation of that notification so opening it behaves like a normal Android notification tap. Notifications without auto-cancel semantics remain active unless the source app removes them itself.

If the `PendingIntent` is already cancelled or cannot be launched, BOOP keeps the card available, leaves the Android notification untouched, and shows a short plain-English message such as `Can't open that right now.` The ordinary BOOP timeout still applies.

## Visual and animation contract

The notification presenter reconstructs BOOP from separate runtime layers:

- the exact established BOOP eyes;
- the exact established five-digit floating yellow hands;
- a notification tile/card or app icon prop;
- an optional count badge.

Generated notification compositions remain pose/framing references only. Do not bake a newly generated mascot or composite character image into runtime animation.

The presenter reuses the user's saved iris hue. Sclera, pupils, highlights and black eyelids remain untouched by hue changes.

Animation may use gentle eye darts, the existing blink language, small hand grip adjustments, notification-card pop/bounce and gaze toward the held card. Motion should settle when the screen settles and respect lifecycle, reduced-motion and power safeguards.

## Sound and vibration

BOOP owns the sound/vibration presentation only for channels the user has explicitly selected for BOOP management.

When a user selects such a channel, BOOP guides them to Android's channel settings and explains that the Android channel must be silent so BOOP can provide the replacement cue. BOOP does not silently alter unrelated channel behavior.

The default BOOP cue is a short, local, playful custom sound chosen during implementation. It must be brief enough to tolerate repeated use and must not require network access or a downloadable sound pack.

One presentation or bundle produces one BOOP cue. Additional notifications absorbed into an already visible burst do not replay the cue for every message.

## First-install setup

On first install BOOP includes two explicit setup steps:

1. Notification Access.
2. Display over other apps.

BOOP checks the actual grant state when the user returns from Android settings. Declining either permission never blocks normal BOOP setup or core functionality.

After both permissions are granted, notification presentation still remains inert until the user explicitly chooses at least one allowed application and channel/category.

## Settings placement

All notification controls live under `Voice Settings -> Notifications`.

The section contains:

- current readiness/permission state;
- BOOP Notifications master toggle;
- allowed applications;
- allowed notification channels/categories per app;
- presentation timeout, initially eight seconds;
- routes back to Android Notification Access, overlay and channel settings when repair is needed.

The settings UI should remain simple and remote-friendly where that existing UI contract applies. Detailed controls are deliberately hidden behind Voice Settings rather than exposed on BOOP's primary surface.

## Permission and failure behavior

BOOP must fail safe toward Android's original notification.

If Notification Access is revoked or the listener disconnects/restarts, overlay permission disappears, the process dies, or BOOP cannot present, BOOP does not cancel or alter the source notification.

If BOOP-managed Android channels are silent but BOOP presentation permissions are no longer usable, Voice Settings must clearly flag the unhealthy state and provide a direct route back to the relevant Android settings. BOOP does not silently reconfigure the user's channels.

## Local data and privacy

BOOP persists only local configuration and minimal coordinator state needed for correct behavior, such as:

- selected package names;
- selected channel IDs/categories;
- master enablement;
- presentation timeout;
- small identifiers/timestamps needed for deduplication and grouping.

BOOP does not create a persistent archive of notification titles, bodies, sender names, contact images or message history, and does not upload notification contents to a cloud service.

After restart, temporary inbox state is rebuilt from Android's currently active notifications rather than from a BOOP message database.

## Data flow

1. Android posts a notification.
2. BOOP's listener receives it.
3. The coordinator rejects anything outside the explicit app/channel allowlist.
4. The coordinator deduplicates and either starts a new presentation or merges it into the current four-second burst bundle.
5. Device lock/display/foreground state selects the locked Activity, in-place BOOP surface, or unlocked overlay presenter.
6. BOOP plays one local cue for the resulting presentation/bundle.
7. Swipe dismisses only BOOP's UI.
8. Tap on a single card sends the original `PendingIntent`, applying normal Android auto-cancel semantics only when the source notification requests them.
9. Tap on a mixed bundle opens BOOP's inbox, where individual items use the same tap behavior.
10. Android remains authoritative for whether the notification itself is still active.

## Platform boundary

Implementation must use supported Android notification-listener, overlay and Activity APIs. `Activity.setShowWhenLocked` and `Activity.setTurnScreenOn` are the intended locked-presentation primitives on supported API levels; the implementation must not add device-admin, root, accessibility automation, alarm/call full-screen-intent abuse, or keyguard-bypass behavior just to achieve presentation.

`NotificationListenerService.getActiveNotifications()` is the source for rebuilding temporary inbox state after listener connection/restart. Cancellation APIs are reserved for successful taps that need to mirror `FLAG_AUTO_CANCEL`; they are never used for a BOOP swipe or timeout.

## Testing strategy

Automated tests cover non-visual logic only, consistent with BOOP's existing manual visual/device acceptance rule.

Required automated coverage:

- app allowlist default-deny behavior;
- per-channel allowlist behavior;
- locked-state redaction;
- unlocked rich-content mapping;
- deduplication;
- four-second burst grouping;
- mixed-app grouping;
- no repeated sound trigger when an active bundle absorbs another notification;
- timeout scheduling and cancellation;
- rebuild from Android active notifications after coordinator/process restart;
- swipe/dismiss path never cancelling the Android notification;
- successful `PendingIntent` forwarding;
- `FLAG_AUTO_CANCEL` tap semantics;
- cancelled/broken `PendingIntent` failure handling;
- permission-loss/listener-disconnect state transitions;
- no persistent notification-body storage.

Do not add screenshot tests, golden-image comparisons, source-string appearance guards, emulator visual acceptance, or CI claims that BOOP's animation looks correct.

## Physical acceptance

Ryan owns device/visual/acoustic acceptance. The initial Pixel acceptance pass must exercise:

- first-install permission flow;
- declining each special permission without breaking BOOP;
- selecting the first allowed app/channel;
- screen-off wake;
- locked privacy-safe card;
- unlock transition to richer content;
- interruption over another foreground app;
- in-place presentation while BOOP/Wall is foreground;
- single notification tap-to-open;
- normal auto-cancel behavior after opening where applicable;
- swipe-away without Android notification dismissal;
- same-app message bomb grouping;
- mixed-app grouping into one bundle;
- BOOP inbox navigation;
- presentation timeout and release of BOOP's wake hold;
- process/reboot recovery from Android active notifications;
- revoked-permission warning/repair flow;
- BOOP replacement sound/vibration quality.

No physical acceptance is inferred from compilation or CI success.

## Rollout boundary

Implementation belongs in canonical `boop-unified`, package `com.boop.alpha1`, but should not be tangled into the temporary procedural-eye/sleep experiment. Reconcile the current eye/sleep work first, then implement this subsystem from the resulting canonical AIO state.

The notification listener/coordinator/presenter should be modular and depend on the established eyes/hands renderer only at the presentation boundary. Notification policy must remain independently testable without animation code.

The standalone Shield clean HOME package remains outside this feature unless a later user-approved design explicitly extends phone notification presentation to Shield.

## Acceptance criteria

The feature is ready for a signed test candidate when all of the following are true:

- BOOP presents nothing until at least one app/channel is explicitly allowed;
- the approved first-install special-access flow exists and is non-blocking;
- allowed notifications can wake the screen and present privacy-safe locked UI without bypassing keyguard;
- unlocked allowed notifications interrupt above other apps;
- swiping BOOP never dismisses the underlying Android notification;
- notification taps preserve the source application's original `PendingIntent` behavior and expected auto-cancel semantics;
- bursts group into one presentation and mixed bundles open BOOP's inbox;
- BOOP supplies one replacement sound/vibration cue per presentation/bundle for user-silenced managed channels;
- user settings persist locally while notification content does not become a durable BOOP archive;
- permission failures leave Android notification state intact;
- focused non-visual tests pass;
- visual, device and sound quality remain explicitly pending until Ryan physically accepts them.
