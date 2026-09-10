# BOOP shared context

Updated 2026-09-10. Shared cross-project understanding owned by `main`.

## How to use this file

This is the broad mental map behind BOOP: product philosophy, durable decisions, device roles, adjacent project vocabulary, accepted interaction patterns and enough history to stop a fresh session from rebuilding the wrong assumptions.

For *current code/build truth*, always follow `AGENTS.md` and `BOOP_START_HERE.md` to the live owning branch, then read that branch's `SESSION_HANDOFF.md`, status and memory files. Current user instructions and fresh physical-device evidence outrank this shared summary.

Also read [`BOOP_PERSONALITY.md`](BOOP_PERSONALITY.md) for Ryan's conversational shorthand and collaboration style. That file explains *how to work together*; this file explains *what world the collaboration is operating in*.

This repository is public. Keep shared context technically useful without publishing credentials, private addresses, personal account data, raw private conversations, private media or secrets.

## Freshness snapshot at this update

Live shared branch before this documentation commit:

- `main`: `79b1025f94ea55407ea72866d77b19cccb85aab7`.

Live canonical Unified branch observed during this continuity update:

- `boop-unified`: `99474d141e7affad17cdbe854e94dd3986076980`.

Latest signed Unified test candidate recorded by the owning branch:

- source/build head `11650313221ae5bf997dbb93b6a905bfdc7da1ed`;
- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- package `com.boop.alpha1`;
- permanent signer unchanged;
- canonical Unified workflow `34433115316`: SUCCESS;
- artifact ID `10135283428`;
- v91 is **CI/build verified but not physically accepted**.

Current protected usable physical rollback:

- `checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`.

Ryan physically confirmed v88 keeps ordinary Android speech usable after failed Natural Voice attempts. Natural Voices are not considered finished at v88.

Do not treat these numbers as timeless. Re-fetch the branch before engineering work.

# 1. Product identity

## BOOP in one sentence

**BOOP is a useful household puppet that lives through different device bodies and works for the human, with local Home Assistant control underneath and a playful physical interface on top.**

It is not supposed to feel like a generic chatbot pasted onto a screen.

## Puppet, not “AI personality”

Ryan's recurring product rule is that BOOP is a **puppet**. It may be expressive, funny and personable, but the interface should remain grounded in eyes, voice, hands, physical cues and useful actions.

The aim is not to persuade anyone that a machine is alive. The aim is to make technology approachable by giving it a simple animated body with understandable behaviour.

“BOOP lives in the human world with us” is the useful design lens. Controls should therefore map to human-world actions and visible states rather than computer abstractions wherever possible.

## Works FOR BOOP / BOOP works FOR you

The ecosystem wording is intentional:

- compatible devices/accessories can say **Works FOR BOOP**;
- BOOP itself **works FOR you**.

This is playful compatibility language, not permission to imply partnerships or endorsements that do not exist.

## Open/user-owned direction

BOOP leans strongly toward:

- open source;
- user-owned hardware;
- local/offline capability for basic functions;
- low or no compulsory subscription cost;
- easy community-made accessories/puppets;
- no dependence on one sealed consumer ecosystem.

Donations are acceptable. Selling the assistant/personality as a locked service is not the motivating idea.

# 2. The one-puppet mental model

The architectural direction approved for the later canonical rebuild is:

> **BOOP is one logical puppet; state is data; every surface renders that same puppet rather than independently implementing him.**

That sentence matters. BOOP appears on Wall, phone/tablet and Shield surfaces, but those should be bodies/views of the same logical character and state rather than drifting clones.

This does not mean all platform code must literally be identical. It means shared behaviour and state should have clear ownership and surfaces should not silently invent separate versions of the character.

# 3. Current app topology

## Canonical Unified app

Normal BOOP app development uses:

- branch `boop-unified`;
- package `com.boop.alpha1`;
- permanent BOOP signer;
- one APK containing the established Wall, phone Launcher and Shield bodies.

The separate historical Wall/Launcher/Shield branches remain rollback/reference lineages. They are not the default place to add new shared features.

## Unified profile routing

Preserve the routing order unless Ryan explicitly changes it:

1. explicit persistent recovery/debug override wins first;
2. Android TV / Leanback / television mode -> Shield body;
3. Pixel 7 Pro -> Wall body;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> Wall body;
5. sub-600dp handheld Android -> Launcher body.

The tablet route is intentionally generic, not a Xiaomi-only model hack.

## Standalone clean Shield HOME exception

A separate clean Nvidia Shield HOME replacement remains deliberately standalone:

- branch `boop-shield-clean-launcher`;
- package `com.boop.shieldhome`.

It must **not** be silently merged into Unified merely because Unified is canonical. Ryan wants Shield hardware testing and recovery confidence before any later merge decision.

Stock launcher recovery is part of that experiment's safety boundary.

# 4. Current Natural Voice work

Natural Voices are the active narrow engineering gate before the larger canonical rebuild.

## Intended user experience

BOOP should offer optional natural local/offline voices after a one-time in-app model download, while preserving ordinary Android TextToSpeech as a reliable fallback.

No OpenAI voice API or paid TTS subscription is required for this subsystem.

Pinned current voices:

- Emma: `bf_emma`, SID 21;
- Isabella: `bf_isabella`, SID 22;
- George: `bm_george`, SID 26;
- Fable: `bm_fable`, SID 25.

Pinned pack: `kokoro-multi-lang-v1_0`.

## Selection semantics

A Natural Voice row is **preview first**, not blind selection.

The durable rule is:

1. verify current installed pack;
2. synthesize/play the requested natural preview;
3. only after successful requested natural playback persist that speaker;
4. persist runtime proof for the current pack/version;
5. route ordinary BOOP speech naturally only when selection + current runtime proof both match;
6. a failed natural preview must leave Android TTS usable;
7. never secretly substitute Android TTS while pretending a Natural Voice preview succeeded.

## Why v91 exists

The physical diagnostic progression matters because it demonstrates the preferred debugging style.

### v85

Natural rows appeared to work but fallback Android TTS could disguise failure. This proved that “sound came out” was insufficient evidence of the requested backend.

### v86

A Sherpa callback/JNI path caused a physical process failure. The callback generation path is therefore banned for the current Sherpa version.

### v87

Non-callback synthesis removed the hard callback crash, but natural selection/fallback behaviour was still unsafe.

### v88

Runtime-proof gating restored ordinary Android speech even after failed Natural Voice attempts. Ryan physically accepted this as the current usable rollback, not as proof that Kokoro itself works.

### v89

Photographable diagnostics produced `BOOP DEV E890` / runtime files incomplete. Investigation found BOOP's own new preflight had invented two bogus required files. The pack itself was not proven corrupt.

### v90

Removing only those bogus requirements progressed physical testing to `BOOP DEV E893` with `Natural speech audio output unavailable`.

That was useful evidence: file preflight passed, Sherpa/Kokoro initialised, synthesis returned audio, and the remaining failure was Android playback.

### v91

The specific repair keeps signed PCM16 and `AudioTrack` `MODE_STATIC` but accepts the valid pre-write `STATE_NO_STATIC_DATA` condition, rejecting only `STATE_UNINITIALIZED` at that stage.

Do not redesign playback transport unless new physical evidence requires it.

## Required physical proof before calling Natural Voices finished

The owning branch currently requires:

- ordinary HA command still acts and Android BOOP still speaks;
- Emma preview speaks naturally;
- at least one second Natural Voice such as George or Isabella speaks;
- a normal BOOP/HA reply uses the selected Natural Voice;
- failures remain photographable and Android fallback survives.

One successful Emma preview alone is not enough.

# 5. Home Assistant relationship

## Authority model

Home Assistant is the local authority for house/device control.

BOOP should control exposed HA devices dynamically rather than requiring a manually authored sentence template for every device.

The design philosophy is:

**BOOP works for HA; HA works for the user.**

In practical terms, BOOP is a friendly interaction layer over a capable local automation system, not a competing smart-home database.

## Local first

Basic household control must not depend on cloud chat availability.

Local Home/HA command routing stays ahead of general chat fallback. If the internet/cloud assistant is unavailable, “turn on the fan” should not become impossible merely because conversation is unavailable.

## Room context

Room inference is desirable because users should be able to say “turn on the fan” naturally when BOOP knows where it is.

When room identity is ambiguous, the product may ask “Which room?” rather than controlling a similarly named device somewhere else.

Room-scoped behaviour should fail closed. Convenience must not become cross-room surprise.

## Dynamic devices

New HA devices should become controllable through exposure/discovery rather than requiring new hardcoded command templates.

Accepted examples from earlier prototypes included exposed fans and simple room-aware commands. The durable lesson is the dynamic model, not the specific device inventory.

# 6. Voice, wake and microphone contracts

These are protected because multiple experimental listeners quickly create latency, conflicts and confusing ownership.

- one controller-owned 16 kHz microphone stream;
- never add a second competing microphone listener merely for a feature shortcut;
- BOOP remains a permanent wake name;
- custom names are additive, not replacements;
- custom-name training uses five local spoken examples and stores compact pronunciation state rather than raw enrolment PCM;
- preserve the exact 1,600-sample / 100 ms wake-to-command bridge;
- default BOOP uses zero intentional Sherpa trailing blanks;
- ordinary and post-wake Android recognition requests keep offensive-word masking disabled; BOOP adds no separate profanity blacklist;
- after normal TTS, wake re-arms only when speech is actually finished;
- hard wake-engine/microphone startup failures remain fail-safe latched;
- diagnostics are pull-only and raw audio is not persistently logged.

Ryan values natural-feeling latency. Avoid architectures that force every basic local action through a distant chat round trip.

# 7. Visual identity

## Eyes are the face

BOOP's face is primarily the approved pair of eyes. The eyes are not generic placeholders and must not be recreated from memory.

Permanent approved eye master:

`boop-unified/unified/assets/boop-eyes/boopApprovedEyes.png`

Recorded identity:

- 1774 x 887 RGBA;
- SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

Do not:

- regenerate;
- redraw;
- “improve”;
- flatten;
- recompress;
- crop/resize on disk;
- destructively recolour;
- reconstruct transparency;
- substitute a stylistically similar asset.

Runtime scaling, posing, masking, blinking and non-destructive animation are allowed.

## Eye colour

User-selectable eye colour changes the procedural iris only.

Default remains cyan/blue at 190 degrees. Do not hue-shift sclera, pupil, highlights, lids or the rest of the artwork.

## Blink

The blink should animate the canonical eyes, not replace them with regenerated frames. If the canonical idle blink authority is missing or uncertain during later rebuild work, recover/request the approved animation source rather than inventing it.

Ryan has explicitly used real video/physical viewing to judge blink quality. Visual acceptance belongs to him.

## Yellow hands

Approved BOOP hands are:

- bright yellow;
- five digits per hand;
- floating without arms;
- expressive through pose/motion;
- not to be regenerated merely because a new pose is needed.

Locked notification hand binary:

`unified/assets/boop-notifications/boop-yellow-hands-approved.png`

SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`.

# 8. Notifications and privacy

BOOP's notification presentation is a mirror/puppet layer over Android's real notification, not a new notification authority.

Durable rules:

- Android's original notification remains authoritative;
- locked presentation can show app identity/icon/count before authentication rather than exposing private content;
- tapping BOOP's mirror preserves the source `PendingIntent` behaviour;
- swiping/timing out the BOOP mirror does not silently dismiss the original shade notification;
- sound/vibration is guarded to avoid duplicate alerts;
- notification presentation is not a reason to add full-screen-intent, query-all, accessibility-service or device-admin authority.

Ryan likes privacy that is visceral and understandable rather than buried in policy text.

Product-level privacy concepts include:

- a physical sleep mask supplied with BOOP;
- a mechanical camera shutter with an audible/tactile click or clunk;
- a playful “shower curtain” privacy metaphor for a speaker body.

The point is visible, physical privacy state.

# 9. Accessibility

Accessibility is part of the character design, not a later settings page.

Long-term BOOP concepts explicitly include:

- ASL and BSL support;
- large visible yellow hands for signing/gesture language;
- behaviour that can communicate visibility/distance;
- clear non-touch interaction;
- large remote-friendly targets where controls do exist.

Do not make touch mandatory for core interaction. Wall in particular is conceived as a non-touch puppet surface.

# 10. BOOP product family concepts

These product concepts are broader than the current Android prototype. They are design direction, not proof of shipping hardware.

## BOOP Wall

Concept:

- wide horizontal/oval-ish screen body;
- two eyes as the dominant face;
- vocal waveform can act as a mouth-like element;
- mirror mechanic;
- magnetic sleep-mask mount;
- non-touch operation;
- designed to live visibly in a room rather than resemble a tablet dashboard.

The current Pixel-based Wall prototype is a development body, not a licence to redesign the conceptual product around a phone UI.

## BOOP Speaker

Concept:

- small oval body;
- wireless charging;
- strong water resistance target;
- shower-curtain privacy gag/mechanic;
- visible/physical camera/privacy behaviour if a camera is ever present;
- mechanical motor/shutter sounds can be part of the character because physical state should be legible.

## BOOP Robot / “Gucci Bot”

Concept direction:

- tiny rolling physical BOOP;
- user target around 15 cm minimum height;
- established silhouette is treated as locked once accepted;
- pencil-lift/utility play ideas;
- edge-stop/safety behaviour;
- mechanical eyelids allowed;
- still BOOP, not a separate mascot species.

Do not assume every speculative robot feature belongs in the current Android backlog.

# 11. Setup, permissions and parental model

Ryan prefers a one-time permission/setup app experience rather than an “advanced mode” maze.

Parental/family interaction concepts include:

- bedtime controls;
- “ask mum/dad” gates;
- “Let me ask your dad/mum” response path;
- timed warnings before powering off an Xbox or similar device;
- controls explained in ordinary language.

The product should not hide significant authority behind clever UI. Permission should be understandable and revisitable.

A playful setup idea uses a “fly to phone and back” whoosh for serial/device transfer. This is a design metaphor, not an engineering contract unless explicitly revived.

# 12. Media is a first-class BOOP domain

Ryan sees media control as one of BOOP's strongest practical uses, especially on Nvidia Shield.

Important direction:

- local/fast media actions should not wait for cloud conversation;
- BOOP can pause music immediately;
- natural requests such as play/pause/skip should feel appliance-fast;
- media artwork/Now Playing presentation can become part of the puppet surface;
- previously landed media-control improvements must be inspected before the later canonical rebuild adds more, so working functionality is not duplicated or redesigned.

The current canonical rebuild prompt explicitly defers additional media work until existing landed media behaviour is audited.

# 13. Developer lab / testing UX

Ryan prefers debugging tools that work from the sofa and produce evidence that can be photographed.

Accepted patterns include:

- exact spoken trigger `developer menu`;
- in-place developer UI rather than fragile activity hops;
- giant `BOOP DEV E###` error screens with a short detail;
- simple manual preview controls;
- real current BOOP face kept visible while trying animations;
- local demo fixtures that do not accidentally exercise privileged runtime paths.

The natural-speech E890–E899 ladder is a good example of the philosophy: separate stages so one photograph tells the next developer where the failure lives.

# 14. Visual QA philosophy

Ryan owns BOOP visual acceptance on real devices.

Do not add automated screenshot/golden-image/pixel/geometry/animation appearance tests and then claim the face is correct. Those were explicitly rejected as an acceptance authority.

GitHub should verify things machines are good at:

- compilation;
- non-visual contracts;
- functional tests;
- package/version/entry activity;
- permanent signer;
- artifact/hash integrity;
- security/sanitisation.

A non-visual emulator/process smoke can prove “it launches without this crash”. It cannot prove “BOOP looks right”.

# 15. Release and rollback discipline

One canonical BOOP APK lineage is the normal rule.

Prefer one intentional functional change per version when practical. Avoid bundling unrelated tweaks merely because a build pipeline is already open.

A physically accepted build creates rollback authority. Record:

- exact Git commit/tag;
- workflow run;
- signed artifact identity;
- physical result.

If the next experiment fails, return to the exact checkpoint/artifact. Do not guess from filenames or timestamps.

Git history/artifacts are the archive. A random local downloads folder is not the archive.

CI green, signer green and physical green are separate states.

# 16. The canonical rebuild

Ryan has already approved a larger later **BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN**.

It is intentionally gated behind Natural Voice physical acceptance.

Do not create/start that rebuild early simply because its plan exists.

Once voices are physically accepted:

1. fetch live `boop-unified` and `main`;
2. branch from the exact physically verified voice-working Unified head;
3. follow the approved rebuild phases;
4. preserve one-logical-puppet architecture;
5. inspect already-landed media work before changing media;
6. preserve exact canonical visual assets;
7. keep Ryan as visual/device/acoustic acceptance authority.

The rebuild is meant to consolidate accumulated functionality without losing the character or silently resetting working behaviour.

# 17. Shield Turbo is adjacent, not the same app

SHIELD TURBO is a separate BOOP-repository project aimed at safe Nvidia Shield tuning/maintenance. Do not confuse its permissions/performance experiments with Unified app contracts.

Durable Turbo direction from prior work includes:

- safe user-facing tuning rather than unsupported overclocking;
- NVIDIA processor mode experiments proved through device evidence;
- reboot persistence/thermal-watchdog ideas;
- fail back to normal under severe thermal state;
- no root/voltage hacks as the normal design;
- photographable full-screen diagnostics for unsupported/failed actions;
- a startup note can explain that silent optimisation may briefly appear to hang rather than adding unreliable notification attempts.

Turbo may later be visually/operationally integrated with other Shield tooling, but that is not permission to merge packages or permissions casually.

# 18. Shield HOME / launcher thinking

Ryan dislikes heavyweight vendor launcher clutter and has experimented with a clean Shield HOME replacement.

General desired qualities:

- immediate remote navigation;
- simple home layout;
- Now Playing/media art where useful;
- safe way back to stock/recovery during experiments;
- avoid disabling platform dependencies without proof;
- accessibility-based enable/swap experiments must preserve an escape route.

A separate clean HOME branch/package exists specifically because replacing HOME can make a Shield awkward to recover. Respect that boundary.

# 19. Phone/tablet launcher direction

A separate phone launcher idea also informs BOOP UI taste:

- pure black/minimal presentation;
- no unnecessary permanent chrome;
- free icon placement and variable icon sizing;
- swipe-based access to Home Assistant widgets/panels;
- app drawer via simple gesture;
- no forced folders/dock/plus-button clutter;
- preserve ordinary app icons rather than reskinning everything;
- strong “bail out” path;
- back should behave predictably.

These are useful design instincts. They are not automatically requirements for every BOOP screen.

# 20. Plain-English product rule

“Computer talk” is effectively banned from consumer-facing BOOP UI when a normal phrase exists.

Prefer:

- “Which room?” over exposing entity IDs;
- “Natural voice files are missing” over a stack trace for normal users;
- “Not available yet” over scary error language for an intentionally unsupported platform path;
- obvious large buttons over tiny settings toggles;
- a direct action over nested settings trees.

Developer diagnostics may still be exact and technical because their purpose is evidence.

# 21. Hardware/prototype worldview

BOOP is intentionally being developed on hardware Ryan already owns rather than waiting for custom manufacturing.

Past/prototype bodies include:

- Nvidia Shield TV as a powerful living-room host;
- a spare Pixel 7 Pro as Wall/prototype hardware;
- Android phone/tablet surfaces;
- ordinary USB webcam concepts;
- remapped remote buttons for fast BOOP invocation.

The lesson is “prove the interaction on available hardware first”. It is not a permanent bill of materials.

# 22. Local-first latency lesson

Ryan has compared commercial smart-speaker ideas with the current BOOP path and repeatedly comes back to one key advantage: **local LAN control feels immediate**.

A cloud chat assistant can enrich conversation, but BOOP should not sacrifice fast deterministic local actions just to route everything through a smarter language model.

This is a central architectural trade-off. Keep local control paths short.

# 23. No Apple-first strategy

BOOP development is not currently organised around Apple platforms. Do not divert core work into an Apple-first architecture unless Ryan explicitly changes direction.

The practical ecosystem is Android, Nvidia Shield, Home Assistant and open/local tooling.

# 24. Adjacent Kodi/Forki universe

Ryan also has a long-running Kodi development/modding ecosystem. Fresh sessions should recognise the vocabulary without contaminating BOOP contracts.

## Kodi wizard

The custom Kodi wizard has historically included practical maintenance/distribution tools such as speed test, URL downloader, APK installer, portable Kodi creation and destructive cleaners.

Its UX priorities mirror BOOP in useful ways:

- remote-friendly;
- large fonts/buttons;
- simple flows;
- plain English;
- avoid frightening users with unnecessary technical language.

## Aura Kodi 21 port

Aura work focuses on preserving the established skin look/behaviour while improving Kodi 21 compatibility, native favourites and remote navigation.

A “canary” may appear in that history because narrow skin experiments helped establish the shared shorthand. Do not therefore assume canary is *only* an Aura term.

## Seren refresh

Seren work aims to preserve familiar UI/settings while modernising Kodi 21/Trakt behaviour, with immediate list/watch-state behaviour and sensible provider handling.

## Forki family

Forki is a custom Kodi APK/fork family for Nvidia Shield. Names such as **Forki**, **Forky Dev** and **Forked Again** refer to separate installable lineages/experiments, not BOOP personalities.

The experimental Forky Dev lineage has carried audio/mixer/silent-playback experiments. Clean Forki is intended to preserve normal Kodi audio while carrying selected compatible improvements such as Dolby Vision support and bundled tooling.

The shared lesson relevant to BOOP is careful fork identity/rollback, not a requirement to put Kodi code inside BOOP.

# 25. Other recurring project context

Ryan regularly applies the same experimental style to practical physical projects: Home Assistant devices, ventilation/HVAC modifications, aquarium filtration/layout, remote-control hardware and small electronics.

What transfers into BOOP is the engineering behaviour:

- prefer reversible mods;
- use existing hardware creatively;
- measure the actual failure;
- avoid destructive changes in a rental/real home;
- build simple manual fallbacks;
- value physical noise, airflow, heat, ergonomics and human use over a purely theoretical design.

Do not publish private household specifics merely to make this file feel comprehensive.

# 26. Muppets / Disney / Imagineering influence

Muppet and Imagineering references are a genuine part of BOOP's creative vocabulary.

This does **not** mean BOOP should copy a Disney/Muppet character. The influence is in principles such as:

- a simple puppet silhouette can carry a lot of expression;
- practical/mechanical effects are emotionally legible;
- hidden machinery should serve an obvious human-facing illusion;
- small playful physical details can make setup/privacy/control memorable;
- character comes from behaviour and timing, not from plastering a face onto every screen.

Use the influence as design intuition, not as brand imitation.

# 27. Design vocabulary and anti-patterns

## Preferred

- puppet;
- eyes;
- hands;
- physical cue;
- local action;
- room awareness;
- one logical state;
- obvious control;
- reversible experiment;
- canary;
- last-good checkpoint;
- photographable diagnostic;
- Works FOR BOOP;
- “Which room?”;
- big chunky remote UI.

## Usually wrong for BOOP

- generic chat window as the main identity;
- settings-dashboard aesthetic everywhere;
- tiny touch targets;
- cloud-only household control;
- hidden privacy state;
- multiple competing microphone owners;
- recreating locked art;
- “AI companion” dependency framing;
- opaque smart-home actions across rooms;
- treating version number as proof of quality;
- visual CI claiming the puppet looks correct.

# 28. Safety and authority boundaries

BOOP should remain useful without quietly accumulating excessive Android authority.

Do not add accessibility, device-admin, full-screen-intent, query-all-packages, notification or other privileged capabilities simply because they make one prototype easier. Each permission needs an explicit product reason and recovery model.

Likewise, do not auto-install APKs, grant permissions or deploy to devices merely because CI produced a green build. Ryan controls physical deployment.

# 29. How Ryan evaluates success

A feature is usually successful when all relevant layers are true:

1. the intended behaviour is actually implemented;
2. focused tests/build/signing/integrity are green;
3. the exact artifact under discussion is identifiable;
4. the real target device does the thing;
5. visual/acoustic behaviour is acceptable if relevant;
6. the previous good state remains recoverable.

A polished explanation cannot substitute for layer 4.

# 30. Current deferred direction after Natural Voices

The large overhaul/canonical rebuild is expected to revisit deferred feature ideas after first auditing what has already landed.

Media control is a particular area where previous improvements may already exist. Do not duplicate them just because an old idea list mentions them.

The correct sequence is:

- finish/prove Natural Voices;
- establish exact voice-working base;
- create rebuild branch from that proven base;
- inventory current behaviour;
- preserve good pieces;
- rebuild/consolidate the architecture around the one-puppet model;
- only then add genuinely missing deferred ideas.

# 31. What “completely transfer the context” means

Ryan's 2026-09-10 experiment is not asking for an impossible byte-for-byte transplant of one model's hidden state into another. It is testing how close practical continuity can get when the durable mental model is written down explicitly.

A successful transfer means a fresh Codex/Boop can:

- recognise the same shorthand;
- understand why design decisions exist;
- know which parts are locked;
- know where current engineering truth lives;
- distinguish physical acceptance from CI;
- recognise adjacent project names without merging them;
- understand the product's local-first puppet philosophy;
- resume work without asking Ryan to retell several weeks of decisions.

That is the target.

# 32. Continuity precedence

When this file conflicts with newer evidence:

1. current explicit Ryan instruction wins;
2. fresh physical result wins over old acceptance notes;
3. live owning-branch docs/Git/CI receipts win for implementation state;
4. current `BOOP_RULES.md` and exact protected asset identities win for locked contracts;
5. this file supplies the broader interpretation/history;
6. old historical branches/notes are evidence of what happened, not automatically current direction.

Fix stale shared context when authorised rather than teaching every future session to memorise exceptions.

# 33. Do not make Ryan repeat these durable points

Before asking basic questions, recover the repository context for facts such as:

- Unified is the normal app lineage;
- one APK routes to Wall/Launcher/Shield bodies;
- clean Shield HOME is still a deliberate standalone exception;
- HA/local control comes before cloud chat for basic actions;
- approved eyes/hands must not be regenerated;
- Ryan owns visual/device/acoustic acceptance;
- CI green is not physical green;
- v88 is the current usable voice rollback while v91 awaits physical Natural Voice proof at this snapshot;
- the canonical rebuild waits for real Natural Voice acceptance;
- `read only`, `lock it`, `cook it`, `poke it`, `update memory` and `canary` have established workflow meanings described in `BOOP_PERSONALITY.md`.

If a future branch makes one of these facts obsolete, update the shared context instead of preserving the obsolete statement as folklore.

# 34. Final product north star

BOOP should feel simple enough that a child, parent or visitor can understand what it is doing, while remaining open enough that an experienced tinkerer can extend it.

The eyes and hands make the machine legible. Home Assistant gives it local capability. The assistant layer gives it language. Git/checkpoints make experimentation safe. The human remains the authority.

That combination, not any one Android screen or model backend, is the BOOP project.