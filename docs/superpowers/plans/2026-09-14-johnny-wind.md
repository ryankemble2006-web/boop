# Sustained fan wind implementation

Use current approved private 102 WIND PNGs unchanged; no asset regeneration. Preserve accepted OI, original resources, NowPlaying/audio and saved data.

Replace queued fan-edge calls with confirmed fan level synchronization. Unknown/unavailable holds last confirmed desired fan state and does not lower him. Startup confirmed on starts wind; repeat observations never restart. Native state drives smoothstep lift/drop over850ms from current strength, preserving continuity on rapid toggles. Match Windows frame/flap/gust/cloud/leaf formulas and fixed hand anchor451,211, rightward direction by default. Include both supplied directions privately but never infer direction from oscillation.

Use existing normal ADS safe cleanup to enter one dedicated wind actor at canonical island origin. Render original island and independent day/night, private wind pose and original moving clouds/leaves with matching pixel gusts. No audio subsystem.

OI takes brief priority over sustained wind: immediately use night, safely clear wind actor/effects, play accepted OI once, then resume wind with850ms lift only if fan remains confirmed on. Fan off during OI prevents wind resumption. Lights on cancels OI and resumes confirmed wind state. No historical OI on reconnect. Wind fan state is a level, not an event queue.

Tests: fan levels, unknown/reconnect,850ms easing and rapid reversal, frame bounds, OI/wind priority and cancellation, cleanup ownership, synthetic compositor; compile native/Java in existing hosted workflow. Package exact private assets locally with approved hashes and existing signer, verify installed hash/prefs/audio, retain v10 rollback. Joint Shield tests: on response, sustained flapping, off lowering, rapid toggles, lights during wind. Host checks are not physical acceptance.
