# Eight-second fan wind cameo (v13; extends accepted v12)

The user physically confirmed v12 runs once, returns to normal and does not repeat, then requested three more seconds so its ending feels less abrupt. Use the same current approved private102 WIND PNGs unchanged. Preserve accepted OI, original resources, NowPlaying/audio and saved data.

A confirmed initial on or off-to-on arms one episode. Repeated on, unchanged reconnect observations and unknown/unavailable never rearm. An episode lasts8000ms total:850ms smoothstep lift,6300ms hold/flap,850ms lowering. Normal story resumes even while the real fan remains on. Only a subsequent confirmed off/on rearms; no event queue accumulates.

Early off lowers from current strength over850ms. A real new on during lowering reverses from current strength and replaces the single episode deadline. Keep rightward default, exact Windows frame/flap/gust/cloud/leaf formulas and hand anchor451,211; both private directional sets remain packaged.

OI retains exclusive scene priority. The episode's original monotonic clock continues while OI owns the stage. Resume only within the original episode's remaining lift/hold window, without resetting its deadline; expire if OI used that window. Any resumed lift starts smoothly from zero, and lowering remains anchored to the original7150ms point. Fan off during OI prevents return. Lights on cancels OI and uses only the existing eligible wind episode.

Tests cover8000ms completion,850ms transitions, repeated levels/rearm, early-off/rapid reversal, wrapped clock, skipped-frame deadlines and OI wall-clock expiry, plus existing synthetic compositor and ownership sanitizers. Compile native/Java in the hosted workflow. Package exact private assets locally with the existing signer, verify installed hash/prefs/audio and retain v10/v11 rollback. Joint Shield acceptance remains separate from source/CI checks.
