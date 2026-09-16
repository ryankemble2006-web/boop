# BOOP v200 Uniform TV Chrome + Voice Demo Design

## Context
v198 is installed on Shield and exposed two UI defects while physically testing Voice Settings: the full-screen BOOP eyes remain visible underneath the settings overlay, and the current Android focus selector can become white-on-white. Ryan also requested that the Shield/Android TV UI use one focus-selector style everywhere and that the pitch/speed controls have an immediate demo button.

This work stays on owner branch `boop-hand-colour-v191` as v200. The separate `boop-home-centred-favourites-lab-v199` work is not modified or merged by this task.

## Goals
1. Every BOOP Android TV menu and submenu uses one focus chrome: dark charcoal fill, BOOP blue `#4DB8FF` 4dp rounded outline, white text, and the same subtle focus enlargement used on Shield Home.
2. Phone/tablet touch UI is unchanged.
3. Voice Settings is a true opaque settings surface. Full-screen BOOP eyes are hidden while the screen is open and do not wake during voice previews.
4. Voice Settings has a `TEST VOICE` action immediately below the pitch/speed controls so slider changes can be auditioned without leaving the screen.
5. Natural voice previews and `TEST VOICE` audibly respect both current speech-rate and pitch values. Android TTS continues to use its existing pitch/rate path.

## Shared TV chrome architecture
Create a shared `com.boop.shared.BoopTvChrome` utility compiled into Unified. It owns the canonical colors, corner radius, border width, and focus scale. The normal state is charcoal with white text. The focused state adds the BOOP-blue outline and focus scale.

`BoopTvChrome` decorates eligible TV controls without replacing existing click/key/focus behavior. It uses state-aware drawable/animator styling rather than installing a new `OnFocusChangeListener`, so existing navigation and callbacks remain intact. Eligible automatic targets are clickable + focusable `TextView`/`Button` controls. It explicitly excludes `EditText`, `SeekBar`, artwork/image views, and custom app-card/puppet surfaces.

Unified installs the decorator only when `Configuration.UI_MODE_TYPE_TELEVISION` is active. A root-tree pass is run for each BOOP TV activity/view after its controls exist, including dynamically created menu/submenu controls. Existing Shield Home `FocusChrome` delegates its colors/drawables to the same canonical utility so Home and all submenus share one source of truth instead of two lookalike implementations.

Specialised controls that already own custom artwork focus, such as app tiles and album art, keep their current purpose-built focus treatment. The uniform rule applies to menu/action buttons, not artwork cards.

## Voice Settings visual ownership
`MainActivity.showVoiceSettings()` must stop waking the full-screen face. When Voice Settings opens, the face view becomes hidden and the settings background is fully opaque black. The settings screen remains the only visible full-screen surface until it closes.

Natural voice previews and the new demo path speak without calling `wakeFaceForInteraction()`. On exit, the face view becomes visible again in its normal idle state. No wake-word, microphone, Home Assistant, or appearance state is changed by opening or closing Voice Settings.

## TEST VOICE behavior
Add one `TEST VOICE` button directly beneath the pitch and speed sliders and before the natural-voice section.

Pressing it speaks a short fixed local phrase such as `This is how BOOP sounds.` using the currently selected backend and current slider values. It does not change the selected voice, does not start recognition, does not open a follow-up microphone session, and does not leave Voice Settings.

For a selected natural voice with a verified local pack, the demo uses that natural speaker. If the natural backend cannot start, the existing safe Android-TTS fallback may speak the same demo phrase. If Android TTS is selected, the demo uses Android TTS directly.

## Natural pitch playback
Natural synthesis already consumes speech rate in generation. v200 applies only the current pitch value at Android PCM playback using `PlaybackParams` on the `AudioTrack`, leaving playback speed at 1.0 so rate is not applied twice. Pitch is clamped through the existing `BoopVoiceTuning` bounds before playback.

If a device rejects `PlaybackParams` or pitch setup, playback falls back safely to unmodified PCM rather than failing the utterance. This fallback must be logged and must not change saved voice settings.

## Scope and preservation
Do not change accepted v189/v191 character artwork, hand/eye/felt geometry, lyrics behavior, media ownership, Close media, favourites ordering/layout, Startup Manager logic, Home Assistant voice-profile protocol, natural model files, or the separate v199 centred-favourites branch.

The existing natural pack already installed on Ryan's Shield remains local and must not be deleted or redownloaded by the app update.

## Verification
TDD must establish RED before production edits for:
- TV chrome canonical values and application to menu/action controls while excluding sliders/edit fields/artwork controls.
- Existing Shield Home focus chrome resolving to the same shared values.
- Voice Settings no longer waking the face, hiding it while open, using an opaque background, and restoring it on close.
- `TEST VOICE` existing in the intended slider section and routing through the current selected voice without recognition/follow-up state.
- Natural playback applying clamped pitch through playback parameters with safe fallback.

Then run existing v198 natural-voice/voice-profile tests plus inherited character, lyrics, music, startup, sharing, Android compile, permanent signer, and APK identity checks. Build as version 200 with a distinct v200 voice/UI version name.

## Physical acceptance
After a signed v200 build passes GitHub CI, install it in-place on the authorized Shield with app data retained. Verify version and unchanged accessibility-service state. Ryan owns final visual acceptance of selector appearance across representative menus and the audible verdict for low/high pitch and speed through `TEST VOICE`.
