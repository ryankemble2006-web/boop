# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- 0.9.5 stronger focus pop remains a physical visual candidate
- v0.10.1 Notification Access routing: physical FAIL
- v0.10.2 Accessibility media path: physical FAIL
- Notification Listener access manually enabled on real Shield: **physical PASS, Now Playing appeared immediately**
- Current candidate: code 18 / `0.10.3-now-playing-layout`
- Build source: `db7aadc18c872b75da8dfa2713c520c7e39d993b`
- Workflow: `34284059958` SUCCESS
- Artifact ID: `10078781887`
- APK SHA-256: `a5400182fbf9364c0d60ae2b5f7148682e3c5eb78bb4ad6b9f4a0994974f20aa`
- Artifact ZIP SHA-256: `888231d6633864b50901e6f18461f654b17e6275a0ca22af0f0080ddf6e78edc`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package green; v0.10.3 visual layout pending Ryan's Shield

## Physically proven media path

Ryan manually enabled **Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing**. After HOME refresh the Deezer Now Playing card appeared immediately. That is the authority path.

v0.10.3 therefore:

- makes `Media access` reflect the Notification Listener grant;
- tries exact Android TV `com.android.tv.settings.privacy.NotificationAccessActivity` first;
- keeps generic/detail Notification Listener settings as fallbacks;
- returns BOOP Home Override Accessibility to `typeWindowStateChanged` only.

## v0.10.3 collision candidate

The first working Now Playing screenshot showed headphones BOOP overlapping transport controls/right-side content, obscured `Next`, competing `Open player`, insufficient title width and a wrapped/clipped `Launcher Settings` top button.

Candidate changes:

- existing 182dp Now Playing card height retained;
- 230dp right-hand mascot reservation added;
- headphones BOOP constrained to a clipped 230x154dp matching bay;
- title/subtitle use one-line end ellipsis;
- five controls compacted to fit their middle region;
- `Open player` stays outside mascot space;
- `Launcher Settings` widened 190dp -> 220dp and forced to one line;
- Favourite apps dimensions/order/grab behavior unchanged.

These are **not** visually green until Ryan tests them on the real Shield.

## Fast CI rule

Ryan explicitly chose real-device visual acceptance over GitHub visual checks. CI now sets `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` and excludes the selected historical appearance/layout contract classes from the fast test source set.

No GitHub screenshots, golden images, emulator layout judgement, focus-scale judgement or animation appearance acceptance.

CI still verifies functional Java/Android logic, signed assembly, exact package/version, protected manifest/service presence, permanent signer, APK archive integrity and artifact upload.

TDD permission-route RED: `1213094c165457b579578d220eb2eec0158656ae`, workflow `34283351013`, failed exactly on the missing exact-TV route. Final v0.10.3 workflow `34284059958` passed the fast functional/build/sign/package lane.

## Locked behavior

Preserve:

- single Home -> BOOP;
- double Home -> native Recent Apps;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide HOME banners and grab/reorder;
- accepted floating-square Apps drawer;
- no HOME black focus plate or normal favourite stars;
- artwork-only HOME focus scaling;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations.

## Next gate

Install/update v0.10.3. Test `Media access` direct routing, Deezer -> HOME, collision clearance, all five media controls, one-line `Launcher Settings`, unchanged favourites, then single/double Home.

Real Shield behavior is the authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
