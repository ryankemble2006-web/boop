# Five-second fan wind cameo (v12; supersedes sustained v11)

The user physically confirmed v11 lowering, then requested fan-on to animate for five seconds and return to normal activity. Use the same current approved private102 WIND PNGs unchanged. Preserve accepted OI, original resources, NowPlaying/audio and saved data.

A confirmed initial on or off-to-on arms one episode. Repeated on, unchanged reconnect observations and unknown/unavailable never rearm. An episode lasts5000ms total:850ms smoothstep lift,3300ms hold/flap,850ms lowering. Normal story resumes even while the real fan remains on. Only a subsequent confirmed off/on rearms; no event queue accumulates.

Early off lowers from current strength over850ms. A real new on during lowering reverses from current strength and replaces the single episode deadline. Keep rightward default, exact Windows frame/flap/gust/cloud/leaf formulas and hand anchor451,211; both private directional sets remain packaged.

OI retains exclusive scene priority. The episode's original monotonic clock continues while OI owns the stage. Resume only within the original episode's remaining lift/hold window, without resetting its deadline; expire if OI used that window. Any resumed lift starts smoothly from zero, and lowering remains anchored to the original4150ms point. Fan off during OI prevents return. Lights on cancels OI and uses only the existing eligible wind episode.

Tests cover5000ms completion,850ms transitions, repeated levels/rearm, early-off/rapid reversal, wrapped clock, skipped-frame deadlines and OI wall-clock expiry, plus existing synthetic compositor and ownership sanitizers. Compile native/Java in the hosted workflow. Package exact private assets locally with the existing signer, verify installed hash/prefs/audio and retain v10/v11 rollback. Joint Shield acceptance remains separate from source/CI checks.
