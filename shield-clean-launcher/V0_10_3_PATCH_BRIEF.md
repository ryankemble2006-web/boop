# BOOP Shield Home v0.10.3 patch brief

Approved by Ryan on 2026-09-08 after physical v0.10.2 testing.

## Physical evidence

- Notification Listener access is the working media authority on the real Nvidia Shield.
- Manually enabling **Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing** immediately made Now Playing appear after HOME refresh.
- The v0.10.2 Accessibility-media experiment did not produce usable media on the Shield and is not the primary path going forward.
- First live Now Playing screenshot proved the data path but showed heavy overlap: headphones BOOP covered transport controls/right side of the card, `Next` was obscured, `Open player` competed for the same area, and the track title had insufficient reserved width.
- The top `Launcher Settings` button also wrapped/clipped.

## Scope: do only these things

### A. Correct Media access route

1. `Media access` status follows the actual BOOP Notification Listener grant, not Accessibility Home Override.
2. Button route order:
   - first: explicit Android TV component `com.android.tv.settings/com.android.tv.settings.privacy.NotificationAccessActivity`;
   - fallback: `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`;
   - modern fallback: `Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS` with BOOP listener component;
   - only if all Notification Access routes fail may general Shield Settings be used.
3. Keep the existing `ShieldNowPlayingListenerService` and MediaSession manager path.
4. Restore BOOP Home Override Accessibility runtime to its protected HOME-window job only. Do not require Accessibility notification events for Now Playing.

### B. Reflow only the Now Playing card

Preserve the existing HOME/favourites geometry outside this block.

Inside Now Playing reserve non-overlapping zones:

- **Left:** fixed square album-art area.
- **Middle:** title, artist, state, progress and transport controls.
- **Right:** fixed mascot bay for the approved headphones BOOP.
- `Open player` stays in the middle/title area and must never share the mascot bounds.
- Title/subtitle use single-line end ellipsis rather than drawing under the mascot.
- Transport buttons remain one row and all five controls must have unobstructed bounds.
- Headphones BOOP remains non-focusable/non-clickable and is clipped to its reserved bay so animation cannot spill across controls/text.
- If no eligible media exists, the whole Now Playing block remains hidden as before.

### C. Tiny top-nav fix

- Widen `Launcher Settings` only enough to keep the existing label on one line.
- Do not move Apps or Shield Settings unless required by that width change.

## Explicitly protected / out of scope

Do not alter:

- Favourite apps row dimensions, banners, ordering/grab behavior or labels;
- accepted Apps drawer treatment;
- HOME black background or accepted focus behavior outside the Now Playing block;
- single Home -> BOOP;
- double Home -> native Shield Recent Apps;
- reboot re-arm of BOOP Home Override;
- stock Android TV Home recovery/trigger mechanism;
- package `com.boop.shieldhome`;
- permanent BOOP signer;
- unified/AIO `com.boop.alpha1`.

## Fast GitHub delivery lane

Ryan owns visual acceptance on the physical Shield.

GitHub CI must **not** run or compile the manual appearance-contract classes selected for exclusion by `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`. No screenshots, golden images, layout/animation judging, emulator visual acceptance, or source guards intended to certify appearance.

CI keeps only non-visual functional logic, Java/Android compilation, signed assembly, exact package/version checks, protected manifest/service presence, permanent signer verification, APK archive integrity and artifact upload.

## Physical v0.10.3 acceptance

After installing the signed APK:

1. `Media access` opens Android TV Notification Access directly if Shield exposes the known activity.
2. BOOP Now Playing reports ON when its Notification Access toggle is enabled.
3. Deezer -> HOME displays title/art/progress/controls.
4. Headphones BOOP does not overlap text, progress, `Open player`, or any transport button.
5. Prev / Rew / Play-Pause / Fwd / Next are all visible and focusable when supported.
6. `Launcher Settings` stays on one line.
7. Favourite apps row looks/behaves unchanged.

Only Ryan's real-device result can mark the visual/layout portion physically green.
