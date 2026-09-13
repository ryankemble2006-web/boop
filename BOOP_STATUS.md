# Unified v165: stronger bounce signed, not installed

Updated 2026-09-13. Owner: `boop-unified-v164-music-bounce`.

Ryan tested installed v164 and said "ha it kinda works :P make him bounce more obviously". This is partial positive motion feedback, not blanket feature acceptance. The last verified Shield installation remains v164; this tuning task did not operate or update any device.

**New v165 change:** double visible vertical displacement with one renderer multiplier. Same actual audio signal, same rise/fall timing, same independent blink speed, same puppet size and resting position. Native Lyrics button/screen/Skip/footer, waveform source, envelope, permissions, artwork and all other existing app inputs remain unchanged from v164, apart from the two version fields. Stronger appearance and possible clipping remain for Ryan to judge, not CI.

**Signed candidate:** `com.boop.alpha1`, `165 / 1.2.165-music-bounce-stronger`; source `fe8c04274064643342734baaafb46077832f335c`; run `34779552167`, job `103783924085`, SUCCESS; artifact `10325220435` / `BOOP-Unified-v165-Music-Bounce-Stronger`. APK SHA256 `bbce760abc5f50fa68663c344fa9b75edfd9feb76559fe35c328c507af09bda2`. Existing permanent signature verified. Downloaded ZIP/APK hashes and build receipt match.

**Verification:** six music test groups passed, including 332 envelope and 27 actual renderer assertions with controlled GL-boundary doubles. v162/v164 source guards passed. Native lyrics data/transport/entry/lifecycle checks, six timing functions, materialized source/art checks, 10 owner/bay contracts, full signed assembly and packaged identity/signature/integrity checks passed. Two inherited v162-only freezes skip future releases; new preservation guards pass instead. No full historical-suite rerun, independent review or hardware visual test is claimed.

**Delivery:** direct signed APK for Ryan; NOT installed or merged. No permission, data, settings, laptop source, phone or emulator changes. Accepted v162 branch still points to `112d09b5b446d6582954a6d89b3700fe16298ecb`; main/accepted owner/other labs were not written. Handoff and full receipt: `SESSION_HANDOFF.md`, `docs/handoffs/2026-09-13-v165-stronger-bounce.md`.
