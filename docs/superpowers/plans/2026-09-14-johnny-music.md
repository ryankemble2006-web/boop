# Original-frame music surprises (v14 candidate)

Approved scope: silent original-pixel play/pause reactions in HA Lab, preserving accepted v13 wind, OI, day/night, NowPlaying and settings. No capture, beat inference, audio initialization, Windows-demo edits or extra private assets.

Play uses untouched JOHNWALK.BMP frames16,33,34,35: front-facing stand/steps, with4.8seconds of toe shuffling and two small integer-pixel hops. Private extraction inspection confirmed32 is back-facing and SJGFTJMP carries a gift; neither is used. Pause uses MJTELE.BMP frames18–21 for a2.6second puzzled shrug and frame16 for the original question mark. Sprites load directly from existing RESOURCE files. Draw each whole actor at bottom-center(380,299) with integer side/hop offsets; question mark at(415,210). No resampling or generated pixels.

Native API requestMusic(1=play,2=pause) returns false when HA owns or needs the stage; false events are dropped. cancelMusic invalidates pending/active generations. At most one pending event; latest opposite replaces, same-kind duplicate coalesces, pending expires1500ms. Normal ADS yields through existing safe cleanup. Wind/OI invalidate active and pending music, and wait for its actor cleanup before entering. No music replay after higher-priority reactions; accepted wind/OI timing and deadline policy unchanged.

Parent media policy debounces confirmed PLAYING/PAUSED transitions900ms, suppresses startup/source-change/reconnect/metadata-only events, cancels on transition, applies same-kind cooldown and never retries a rejected native request. Media callbacks only signal reaction controls; no player routing or focus changes.

Tests cover generation replacement/coalescing, stale expiry/clock wrap, wind/OI priority and mutual ownership, pose ranges/durations, original ADS slot cleanup, existing Java media/HA policies and ownership sanitizers. Hosted native/Java compile is separate from private packaging/install and user physical appeal acceptance.
