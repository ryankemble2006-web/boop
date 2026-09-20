# Native Deezer heart diagnosis, 2026-09-20

This is a documentation-only continuation of Shield v234. Source remains `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`; live branch was checked at `35fc7685471768a14c2ed6dc5c3428e36dd7957b` before recording these findings.

## Physical failure and capability evidence

Ryan's screenshot of BOOP's right lyrics heart showed `Deezer isn't exposing this favourite control.` This is a real failed integration result, not physical acceptance of the feature. The media-session route in v234 must not be presented as working on this installed Deezer.

RDC was restored and Android Debug Bridge verified the intended Shield. Earlier installed-provider inspection returned `deezer.android.app`, versionName `1.0.1.1`, versionCode `301000101`. Before and after the user's provider-heart tests, Deezer's media session reported rating type 0, action mask 273714 (SET_RATING absent), and an empty custom-action list. Thus the currently implemented heart/custom-action routes are not advertised by this provider. This is not proof that no other authorized integration is possible.

## User's log-only test

Live recording and repeated UI hierarchy/screenshot capture made navigation too slow. Ryan explicitly requested NO LIVE RECORDING and to perform the action first, then inspect logs. The capture worker was stopped in the preceding turn. Do not restart live recording or repeated UI polling without a new explicit request.

After Ryan reported add and remove pressed, a bounded, one-shot read of Deezer's own main/system log entries since the recorded baseline returned only two garbage-collection messages. There were no favourite API names, requests, responses or button-handler messages in this retrieved window. This does not establish whether the favourites changed; ordinary log output is not an invocation trace. A one-shot media-session read still showed the absent capabilities above. A process check found no screenrecord, screencap or uiautomator processes on the Shield. No remote input or new screenshot was issued in this log-only turn.

## Existing files recovered offline

The prior private control capture contains 41 UI samples. One sample has the player controls visible; the heart-area clickable nodes have no resource IDs, content descriptions or useful checked/selected state. Its already-saved screenshot was successfully opened through RDC and shows a normal heart alongside a separate crossed-out heart. This supplies visual/control-layout evidence only, not the handlers' semantics or a verified add/remove round trip.

Clarify whether the user's removal action is toggling the normal favourite heart off or using the separate crossed-out heart. Do not silently substitute dislike/recommendation exclusion for unfavouriting. The accepted BOOP layout remains remove-left/add-right on lyrics and a single HOME toggle until explicitly revised.

## Next useful investigation

Inspect the installed provider's heart implementation or an already cached matching package offline, using existing tools and without UI polling. Discover whether there is a callable exported action or a suitably identifiable control before making another APK. No such route was found or implemented by this log-only check. No new permissions, installs, signing changes, app code or builds were made. Raw logs, UI dumps and images remain private on the user's machine and are not published here.
