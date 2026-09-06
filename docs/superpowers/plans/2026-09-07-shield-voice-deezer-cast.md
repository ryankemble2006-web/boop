# Shield Voice Deezer Cast Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route BOOP Shield music requests through local Home Assistant and Music Assistant to a user-selected Shield Cast player, with no Google Assistant fallback.

**Architecture:** The still-separate microphone component supplies a disposable transcript. Pure Java parsing produces a `MusicRequest`; target binding persists a Home Assistant `media_player` entity ID. A repository calls one HA script then confirms that exact player reaches `playing`; the existing Deezer listener independently drives BOOP's animation.

**Tech Stack:** Java 17, Android API 26+, SharedPreferences, existing HA WebSocket, JUnit 4, Music Assistant.

**Spec:** `docs/superpowers/specs/2026-09-07-shield-voice-deezer-cast-design.md`

## Global Constraints

- The overlay stays microphone-free, non-focusable, non-touchable, and free of HA control logic.
- Never invoke or fall back to Google Assistant.
- Direct music parsing stays local; OpenCode only receives non-direct conversation.
- Persist only the target `media_player` entity ID and friendly label; never an IP, Deezer credential, transcript, or metadata.
- An explicit destination wins. Ambiguous or missing destinations ask the user; never silently use another player.
- The user installs/authorizes Music Assistant, Deezer, and Google Cast with OpenCode. BOOP changes no HA config.

---

## Home Assistant contract to create with OpenCode

Create `script.boop_play_music`, with fields `query` (optional text) and `target_player_id` (required `media_player` entity). The script resolves the query through Music Assistant's authorized Deezer provider and plays it to the supplied Cast entity. It must be callable through:

```json
{"domain":"script","service":"turn_on","target":{"entity_id":"script.boop_play_music"},"service_data":{"query":"jazz","target_player_id":"media_player.shield"}}
```

The script's service acknowledgement is dispatch only; BOOP confirms success only after that exact target emits `playing`.

## File structure

- Create `MusicRequest.java`, `MusicIntentParser.java`, `MusicPlayerTarget.java`, `MusicTargetCatalog.java`, `MusicTargetBinding.java`, `MusicPlaybackRepository.java`, and `MusicVoiceCommandRouter.java` under `shield-overlay/app/src/main/java/com/boop/shieldoverlay/`.
- Modify `BoopPreferences.java`, `BoopHomeActivity.java`, `TvSettingsView.java`, and `strings.xml`.
- Create matching JUnit tests under `shield-overlay/app/src/test/java/com/boop/shieldoverlay/`.
- Update `SESSION_HANDOFF.md`, `BOOP_MEMORY.txt`, and `BOOP_STATUS.md` only after implementation evidence exists.

### Task 1: Add a local semantic music parser

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicRequest.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicIntentParser.java`
- Test: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicIntentParserTest.java`

**Interfaces:** `MusicIntentParser.parse(String): MusicRequest | null`; `MusicRequest(String query, String destination)`.

- [ ] **Step 1: Write failing tests**

```java
assertEquals(new MusicRequest(null, null), parser.parse("music"));
assertEquals(new MusicRequest(null, null), parser.parse("put some tunes on please"));
assertEquals(new MusicRequest("jazz", null), parser.parse("play jazz"));
assertEquals(new MusicRequest("radiohead", "Kitchen"), parser.parse("play Radiohead in Kitchen"));
assertNull(parser.parse("turn on hallway light"));
```

- [ ] **Step 2: Verify the test fails**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicIntentParserTest`

Expected: FAIL because parser/value classes do not exist.

- [ ] **Step 3: Implement the grammar**

```java
public MusicRequest parse(String transcript) {
  String text = normalize(transcript);
  if (text == null || !hasMusicCue(text)) return null;
  return new MusicRequest(extractQuery(text), extractDestination(text));
}
```

Recognize `music`, `play`, `put`, `start`, `tunes`, and `songs`, after stripping polite filler. Parse trailing `on`, `in`, or `to <destination>` only; do not infer a destination from an artist title. Implement value equality/hash code for tests.

- [ ] **Step 4: Verify tests pass**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicIntentParserTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicRequest.java shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicIntentParser.java shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicIntentParserTest.java
git commit -m "feat: parse local BOOP music requests"
```

### Task 2: Discover and retain an explicit user Cast target

**Files:**
- Create: `MusicPlayerTarget.java`, `MusicTargetCatalog.java`, `MusicTargetBinding.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopPreferences.java`
- Test: `MusicTargetCatalogTest.java`, `BoopPreferencesTest.java`

**Interfaces:** `MusicTargetCatalog.fromStates(JSONArray): List<MusicPlayerTarget>`; `matchExplicit(String): List<MusicPlayerTarget>`; `BoopPreferences.musicTarget()`, `setMusicTarget(MusicTargetBinding)`, `clearMusicTarget()`.

- [ ] **Step 1: Write failing tests**

```java
assertEquals(List.of(new MusicPlayerTarget("media_player.shield", "Living Room Shield")),
  MusicTargetCatalog.fromStates(statesWithOneNamedMediaPlayer()));
assertEquals(2, catalogWithKitchenAndKitchenTv().matchExplicit("kitchen").size());
preferences.setMusicTarget(new MusicTargetBinding("media_player.shield", "Living Room Shield"));
assertEquals("media_player.shield", preferences.musicTarget().entityId());
```

- [ ] **Step 2: Verify failures**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicTargetCatalogTest --tests com.boop.shieldoverlay.BoopPreferencesTest`

Expected: FAIL because target types and preference API do not exist.

- [ ] **Step 3: Implement catalog and binding**

```java
public record MusicPlayerTarget(String entityId, String displayName) {}
public record MusicTargetBinding(String entityId, String displayName) {}
```

Read only named `media_player.*` states, sort case-insensitively, reject malformed IDs, and persist `music_target_entity_id_v1` plus `music_target_name_v1`. With zero/multiple candidates, the later Settings flow requires explicit user confirmation.

- [ ] **Step 4: Verify tests pass**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicTargetCatalogTest --tests com.boop.shieldoverlay.BoopPreferencesTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicPlayerTarget.java shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicTargetCatalog.java shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicTargetBinding.java shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopPreferences.java shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicTargetCatalogTest.java shield-overlay/app/src/test/java/com/boop/shieldoverlay/BoopPreferencesTest.java
git commit -m "feat: store BOOP Cast target binding"
```

### Task 3: Call the HA script and prove the selected player started

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicPlaybackRepository.java`
- Test: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicPlaybackRepositoryTest.java`

**Interfaces:** `play(MusicRequest, MusicTargetBinding, Callback)`; callback result is `STARTED`, `SETUP_REQUIRED`, `UNAVAILABLE`, or `FAILED`.

- [ ] **Step 1: Write failing tests**

```java
repository.play(new MusicRequest("jazz", null), shield, callback);
assertEquals("call_service", commands.lastType);
assertEquals("script", commands.body.getString("domain"));
assertEquals("boop_play_music", commands.body.getString("service"));
states.emit("media_player.other", "playing"); assertNull(callback.result);
states.emit("media_player.shield", "playing"); assertEquals(STARTED, callback.result);
```

- [ ] **Step 2: Verify failure**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicPlaybackRepositoryTest`

Expected: FAIL because the repository does not exist.

- [ ] **Step 3: Implement bounded dispatch/confirmation**

```java
JSONObject body = new JSONObject().put("domain", "script").put("service", "turn_on")
  .put("target", new JSONObject().put("entity_id", "script.boop_play_music"))
  .put("service_data", new JSONObject().put("target_player_id", target.entityId()));
```

Subscribe before sending. Complete only when the script reply succeeds and the exact bound player reports `playing`; cancel subscription/10-second timeout on every terminal result. Missing binding is `SETUP_REQUIRED`; socket/subscription failure is `UNAVAILABLE`; HA error/timeout is `FAILED`. Never issue generic `play_media`, a Deezer URL, or a command to another player.

- [ ] **Step 4: Verify tests pass**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicPlaybackRepositoryTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicPlaybackRepository.java shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicPlaybackRepositoryTest.java
git commit -m "feat: route BOOP music through Home Assistant"
```

### Task 4: Expose the route to the future voice surface, never the overlay

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicVoiceCommandRouter.java`
- Test: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicVoiceCommandRouterTest.java`

**Interfaces:** `boolean onTranscript(String, Listener)`; Listener has `onMusicStarted()`, `onSetupRequired()`, `onUnavailable(String)`, and `onFailed(String)`.

- [ ] **Step 1: Write failing tests**

```java
assertTrue(router.onTranscript("put some tunes on", listener));
assertEquals(new MusicRequest(null, null), repository.lastRequest);
assertFalse(router.onTranscript("what time is it", listener));
assertEquals("Set up BOOP's Shield music destination first.", listener.lastMessage);
```

- [ ] **Step 2: Verify failure**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicVoiceCommandRouterTest`

Expected: FAIL because the router does not exist.

- [ ] **Step 3: Implement transcript adapter**

```java
public boolean onTranscript(String text, Listener listener) {
  MusicRequest request = parser.parse(text);
  if (request == null) return false;
  MusicTargetBinding target = resolveTarget(request.destination());
  if (target == null) { listener.onSetupRequired(); return true; }
  repository.play(request, target, result -> deliver(result, listener));
  return true;
}
```

The router neither constructs a recognizer nor requests `RECORD_AUDIO`; the separate microphone owner calls it then discards the transcript. It returns `false` for ordinary conversation so the existing OpenCode route owns it.

- [ ] **Step 4: Verify tests pass and run regression suite**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicVoiceCommandRouterTest`

Expected: PASS.

Run: `./gradlew :shield-overlay:app:testDebugUnitTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/MusicVoiceCommandRouter.java shield-overlay/app/src/test/java/com/boop/shieldoverlay/MusicVoiceCommandRouterTest.java
git commit -m "feat: handle BOOP voice music commands"
```

### Task 5: Add remote-first target setup and perform the physical gate

**Files:**
- Modify: `BoopHomeActivity.java`, `TvSettingsView.java`, `strings.xml`
- Test: `MusicTargetSetupControllerTest.java`
- Modify after evidence: `SESSION_HANDOFF.md`, `BOOP_MEMORY.txt`, `BOOP_STATUS.md`

- [ ] **Step 1: Write failing setup tests**

```java
assertEquals(ONE_CANDIDATE, controller.fromStates(oneShield()).state());
assertEquals(CHOOSE_TARGET, controller.fromStates(twoPlayers()).state());
controller.choose(new MusicPlayerTarget("media_player.shield", "Living Room Shield"));
assertEquals("media_player.shield", preferences.musicTarget().entityId());
```

- [ ] **Step 2: Verify failure**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest --tests com.boop.shieldoverlay.MusicTargetSetupControllerTest`

Expected: FAIL because setup controller/view wiring does not exist.

- [ ] **Step 3: Implement Settings flow**

Query HA `get_states` only when the remote user opens Music destination. Display `Not set`, the saved label, or a selectable candidate list. One candidate is offered but still confirmed; several require selection. Left/Back changes nothing. Show clear HA-offline/no-player messages. Do not scan continuously or use a hard-coded device name.

- [ ] **Step 4: Run full build verification**

Run: `./gradlew :shield-overlay:app:testDebugUnitTest`

Expected: PASS.

Run: `./gradlew :shield-overlay:app:assembleDebug`

Expected: BUILD SUCCESSFUL; the debug APK is verification-only.

- [ ] **Step 5: Run physical acceptance after user installs MA/Deezer**

1. In HA Developer Tools, confirm `script.boop_play_music` starts Deezer Cast playback on the selected Shield entity.
2. Set BOOP's target; restart it and verify the entity binding persists.
3. Test `music`, `play jazz`, and an explicit target. Verify no Google Assistant invocation and no substitute player.
4. Verify Deezer Cast stays peaceful, H1 responds only to observed playback, remote passes through, and Home/Routines/pairing remain healthy.
5. Disable the script/provider and verify a plain BOOP error without a fallback.

- [ ] **Step 6: Record evidence and publish safely**

Update handoff/memory/status with actual results only; fetch before push; stage reviewed BOOP files only; verify local and live branch heads match. Do not create a checkpoint without signed-build and physical acceptance evidence.

## Plan self-review

- Tasks 1/4 provide semantic local handling and conversation fallthrough; Task 2 gives each user a durable non-personal target; Task 3 verifies real playback; Task 5 provides remote setup and the no-Google physical gate.
- All types used after creation are defined in earlier tasks. No overlay microphone/control change, hidden credential flow, hard-coded device, or Google fallback is introduced.
