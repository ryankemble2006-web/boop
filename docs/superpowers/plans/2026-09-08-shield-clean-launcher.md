# Shield Clean Launcher Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reversible, stock-feeling Nvidia Shield HOME inside the canonical unified BOOP APK that defaults to favourite apps only, never implements adverts/Shop/Discover, and lets users opt into approved local Android TV content rows.

**Architecture:** Keep the new launcher isolated under `unified/shield-home/` and materialise it only into the generated `shield-lib`. `UnifiedEntryActivity` distinguishes a Shield HOME invocation from an ordinary BOOP/Leanback launch, sending HOME to `com.boop.shieldhome.ShieldLauncherActivity` while preserving the existing Shield puppet path. The favourite-only path uses a cached app catalogue plus persisted favourite order. Optional `TvContract` providers are constructed only when their individual saved toggle is enabled.

**Tech Stack:** Java 17, Android SDK 36, unified app minSdk 29, Android framework Views, `PackageManager`, `SharedPreferences`, `TvContract`, JUnit 4, Gradle 9.6, existing GitHub Actions permanent-signing workflow.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-clean-launcher-design.md`

## Global Constraints

- Canonical branch is `boop-unified`.
- Package stays `com.boop.alpha1`; permanent BOOP signer stays unchanged.
- Google/Android TV stock launcher stays installed and selectable as recovery.
- Installing BOOP must not silently select BOOP as HOME, disable Google components, alter secure settings, or use privileged shell hacks.
- Default HOME is favourite apps plus minimal Apps/Settings navigation only.
- Advertising, sponsored placements, Shop, Discover, promotional autoplay and promotional/ad feed providers do not exist in this launcher.
- Optional row keys are exactly `PLAY_NEXT` and `APP_CHANNELS`; both default OFF independently.
- A disabled optional row is not instantiated, queried, subscribed, polled or fetched.
- Do not modify Android global animation scales. Local focus, scroll and page transitions remain enabled.
- Preserve the phone Launcher, Wall, Shield puppet, HA naming/control, blink, eye, assistant, microphone, density and signing behavior outside the narrow HOME routing change.
- Preserve concurrent approved-eye CI work, including `tests/test_approved_eye_master_contract.py`.
- Automated verification is behavioral/structural only. No screenshots, golden images, aesthetic source-string tests, emulator appearance grading or GitHub visual acceptance.
- Ryan owns real Shield appearance, animation and physical HOME acceptance.

---

## File Map

**Create:**
- `unified/ShieldEntryRoute.java`
- `unified/ShieldEntryRouteTest.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppEntry.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppRepository.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/FavouriteOrder.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeStore.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeContentCard.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRow.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRowProvider.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/OptionalRowRegistry.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvProviderRows.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/PackageRefreshPolicy.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldAppsView.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java`
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java`
- `unified/shield-home/src/test/java/com/boop/shieldhome/TvAppRepositoryTest.java`
- `unified/shield-home/src/test/java/com/boop/shieldhome/FavouriteOrderTest.java`
- `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldHomeStoreTest.java`
- `unified/shield-home/src/test/java/com/boop/shieldhome/OptionalRowRegistryTest.java`
- `unified/shield-home/src/test/java/com/boop/shieldhome/PackageRefreshPolicyTest.java`

**Modify:**
- `unified/UnifiedEntryActivity.java`
- `unified/shield-manifest.xml`
- `unified/launcher-manifest.xml`
- `scripts/materialize-unified.sh`
- `unified/app-build.gradle`
- `.github/workflows/build-boop-unified.yml`
- `SESSION_HANDOFF.md`
- `BOOP_STATUS.md`
- `BOOP_UNIFIED_MEMORY.md`

---

### Task 1: Split Shield HOME routing from the existing Shield puppet launch

**Files:** create `unified/ShieldEntryRoute.java`, `unified/ShieldEntryRouteTest.java`; modify `unified/UnifiedEntryActivity.java`, `scripts/materialize-unified.sh`.

**Interfaces:**
- `ShieldEntryRoute.Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent)`.
- `Target.className()` returns the exact component class.
- `Target.suppressEntryTransition()` is `false` only for `SHIELD_HOME`.
- `SHIELD_HOME` -> `com.boop.shieldhome.ShieldLauncherActivity`.
- `SHIELD_PUPPET` -> `com.boop.shieldoverlay.MainActivity`.

- [ ] **Step 1: Write the failing routing tests**

```java
@Test public void shieldHomeUsesCleanLauncher() {
    ShieldEntryRoute.Target t = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, true);
    assertEquals("com.boop.shieldhome.ShieldLauncherActivity", t.className());
    assertFalse(t.suppressEntryTransition());
}
@Test public void ordinaryShieldLaunchStillUsesPuppet() {
    ShieldEntryRoute.Target t = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, false);
    assertEquals("com.boop.shieldoverlay.MainActivity", t.className());
    assertTrue(t.suppressEntryTransition());
}
@Test public void otherBodiesRemainUnchanged() {
    assertEquals("com.boop.alpha1.MainActivity",
        ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.WALL, true).className());
    assertEquals("com.boop.launcher.MainActivity",
        ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.LAUNCHER, true).className());
}
```

- [ ] **Step 2: Materialise and prove the tests fail before implementation**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*ShieldEntryRouteTest' --stacktrace
```
Expected: compile/test failure because `ShieldEntryRoute` is absent.

- [ ] **Step 3: Implement the routing helper**

```java
final class ShieldEntryRoute {
    enum Target {
        SHIELD_HOME("com.boop.shieldhome.ShieldLauncherActivity", false),
        SHIELD_PUPPET("com.boop.shieldoverlay.MainActivity", true),
        WALL("com.boop.alpha1.MainActivity", true),
        HANDHELD_LAUNCHER("com.boop.launcher.MainActivity", true);
        private final String className;
        private final boolean suppress;
        Target(String className, boolean suppress) { this.className = className; this.suppress = suppress; }
        String className() { return className; }
        boolean suppressEntryTransition() { return suppress; }
    }
    static Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent) {
        if (mode == BoopDeviceProfile.Mode.SHIELD) return homeIntent ? Target.SHIELD_HOME : Target.SHIELD_PUPPET;
        return mode == BoopDeviceProfile.Mode.WALL ? Target.WALL : Target.HANDHELD_LAUNCHER;
    }
}
```

- [ ] **Step 4: Wire `UnifiedEntryActivity` narrowly**

Calculate `homeIntent` with `getIntent() != null && getIntent().hasCategory(Intent.CATEGORY_HOME)`. Resolve target before the assistant-choice gate. Run assistant-choice setup only for `SHIELD_PUPPET`. Start `target.className()`. Call `overridePendingTransition(0, 0)` only when `target.suppressEntryTransition()` is true.

- [ ] **Step 5: Copy helper/test during materialisation and rerun routing tests**

```bash
cp unified/ShieldEntryRoute.java "$MAIN/ShieldEntryRoute.java"
cp unified/ShieldEntryRouteTest.java "$TEST/ShieldEntryRouteTest.java"
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*BoopDeviceProfileTest' --tests '*ShieldEntryRouteTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add unified/ShieldEntryRoute.java unified/ShieldEntryRouteTest.java unified/UnifiedEntryActivity.java scripts/materialize-unified.sh
git commit -m "feat(unified): route Shield HOME separately"
```

---

### Task 2: Build the cached TV app catalogue and favourite model

**Files:** create `TvAppEntry.java`, `TvAppRepository.java`, `FavouriteOrder.java` plus their two tests; modify `unified/launcher-manifest.xml`, `scripts/materialize-unified.sh`.

**Interfaces:**
- `TvAppEntry(String component, String packageName, String label)` with `component()`, `packageName()`, `label()` accessors.
- `TvAppRepository(Context).load()` queries `CATEGORY_LEANBACK_LAUNCHER` then `CATEGORY_LAUNCHER`, dedupes by flattened component, excludes `context.getPackageName()`, sorts by label.
- Pure `TvAppRepository.merge(String ownPackage, List<TvAppEntry> leanback, List<TvAppEntry> launcher)`.
- Pure `FavouriteOrder.reconcile(List<String>, List<TvAppEntry>)`, `add(List<String>, String)`, `remove(List<String>, String)`, `move(List<String>, String, int delta)`.

- [ ] **Step 1: Write failing pure-model tests**

```java
@Test public void catalogueMergesDedupesExcludesBoopAndSorts() {
    List<TvAppEntry> out = TvAppRepository.merge("com.boop.alpha1",
        List.of(new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                new TvAppEntry("com.boop.alpha1/.Entry", "com.boop.alpha1", "BOOP")),
        List.of(new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                new TvAppEntry("other/.Main", "other", "Alpha")));
    assertEquals(List.of("other/.Main", "pkg/.Tv"), out.stream().map(TvAppEntry::component).toList());
}
@Test public void reconcileDropsStaleAndPreservesOrder() {
    List<TvAppEntry> installed = List.of(new TvAppEntry("a/.A", "a", "A"), new TvAppEntry("b/.B", "b", "B"));
    assertEquals(List.of("b/.B", "a/.A"), FavouriteOrder.reconcile(List.of("b/.B", "gone/.Gone", "a/.A"), installed));
}
@Test public void addRemoveAndMoveAreStable() {
    assertEquals(List.of("a", "b", "c"), FavouriteOrder.add(List.of("a", "b"), "c"));
    assertEquals(List.of("a", "c"), FavouriteOrder.remove(List.of("a", "b", "c"), "b"));
    assertEquals(List.of("b", "a", "c"), FavouriteOrder.move(List.of("a", "b", "c"), "b", -1));
}
```

- [ ] **Step 2: Extend materialisation and prove tests fail**

```bash
mkdir -p "$ROOT/shield-lib/src/test/java"
cp -R unified/shield-home/src/main/java/* "$ROOT/shield-lib/src/main/java/"
cp -R unified/shield-home/src/test/java/* "$ROOT/shield-lib/src/test/java/"
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.TvAppRepositoryTest' --tests 'com.boop.shieldhome.FavouriteOrderTest' --stacktrace
```
Expected: failure until models exist.

- [ ] **Step 3: Implement catalogue/favourite logic and TV package visibility**

`load()` performs exactly these two scans per refresh:
```java
List<TvAppEntry> leanback = query(Intent.CATEGORY_LEANBACK_LAUNCHER);
List<TvAppEntry> launcher = query(Intent.CATEGORY_LAUNCHER);
return merge(context.getPackageName(), leanback, launcher);
```
Add a `MAIN` + `LEANBACK_LAUNCHER` query to `unified/launcher-manifest.xml`; retain its current ordinary `LAUNCHER` query.

- [ ] **Step 4: Run tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.TvAppRepositoryTest' --tests 'com.boop.shieldhome.FavouriteOrderTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add unified/shield-home unified/launcher-manifest.xml scripts/materialize-unified.sh
git commit -m "feat(shield-home): add TV app and favourite model"
```

---

### Task 3: Persist favourites and enforce optional rows OFF by default

**Files:** create `ShieldHomeStore.java`, `HomeContentCard.java`, `HomeRow.java`, `HomeRowProvider.java`, `OptionalRowRegistry.java`, `ShieldHomeStoreTest.java`, `OptionalRowRegistryTest.java`.

**Interfaces:**
- Preferences file `boop_shield_home_v1`.
- Keys `favourites_initialised`, `favourites_json`, `row_play_next`, `row_app_channels`.
- `ShieldHomeStore.loadOrSeedFavourites(List<TvAppEntry>)`, `saveFavourites(List<String>)`, `rowEnabled(OptionalRowRegistry.Key)`, `setRowEnabled(OptionalRowRegistry.Key, boolean)`.
- Static `encodeComponents(List<String>)`, `decodeComponents(String)` for deterministic tests.
- `HomeContentCard(String title, String intentUri, String posterArtUri)` with accessors.
- `HomeRow(String title, List<HomeContentCard> cards)` with accessors.
- `HomeRowProvider.load()` returns `List<HomeRow>`.
- `OptionalRowRegistry.Key` contains exactly `PLAY_NEXT`, `APP_CHANNELS`.
- `loadEnabled(EnabledLookup, ProviderFactory)` constructs only enabled providers.

- [ ] **Step 1: Write failing tests**

```java
@Test public void favouritesCodecRoundTripsAndMalformedDataFailsSafe() {
    String raw = ShieldHomeStore.encodeComponents(List.of("a/.A", "b/.B"));
    assertEquals(List.of("a/.A", "b/.B"), ShieldHomeStore.decodeComponents(raw));
    assertEquals(List.of(), ShieldHomeStore.decodeComponents("not-json"));
}
@Test public void registryHasOnlyApprovedNonAdvertisingRows() {
    assertArrayEquals(new OptionalRowRegistry.Key[] {
        OptionalRowRegistry.Key.PLAY_NEXT, OptionalRowRegistry.Key.APP_CHANNELS
    }, OptionalRowRegistry.Key.values());
}
@Test public void disabledRowsCreateNoProviders() {
    AtomicInteger created = new AtomicInteger();
    List<HomeRowProvider> rows = OptionalRowRegistry.loadEnabled(key -> false,
        key -> { created.incrementAndGet(); return List::of; });
    assertTrue(rows.isEmpty());
    assertEquals(0, created.get());
}
@Test public void rowTogglesAreIndependent() {
    List<OptionalRowRegistry.Key> created = new ArrayList<>();
    OptionalRowRegistry.loadEnabled(key -> key == OptionalRowRegistry.Key.PLAY_NEXT,
        key -> { created.add(key); return List::of; });
    assertEquals(List.of(OptionalRowRegistry.Key.PLAY_NEXT), created);
}
```

- [ ] **Step 2: Prove tests fail**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.ShieldHomeStoreTest' --tests 'com.boop.shieldhome.OptionalRowRegistryTest' --stacktrace
```
Expected: failure until store/registry exist.

- [ ] **Step 3: Implement fail-safe storage and lazy registry**

`rowEnabled` uses `preferences.getBoolean(key.preferenceKey(), false)`. `setRowEnabled` writes only the selected key. On first use only, `loadOrSeedFavourites` seeds the currently installed TV app components and sets `favourites_initialised=true`. Once initialised, an intentionally empty favourite list remains empty and newly installed apps are not auto-favourited.

- [ ] **Step 4: Run tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.ShieldHomeStoreTest' --tests 'com.boop.shieldhome.OptionalRowRegistryTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): persist favourites and optional rows"
```

---

### Task 4: Add local Android TV optional providers and package-refresh policy

**Files:** create `TvProviderRows.java`, `PackageRefreshPolicy.java`, `PackageRefreshPolicyTest.java`; extend `OptionalRowRegistryTest.java`.

**Interfaces:**
- `TvProviderRows.factory(Context)` returns `OptionalRowRegistry.ProviderFactory`.
- `PLAY_NEXT` queries `TvContract.WatchNextPrograms.CONTENT_URI` only after provider creation/load.
- `APP_CHANNELS` queries `TvContract.Channels.CONTENT_URI`, filters browsable `TYPE_PREVIEW`, then `TvContract.buildPreviewProgramsUriForChannel(id)`.
- Provider failure returns an empty list; no provider performs HTTP/network I/O.
- `PackageRefreshPolicy.shouldReload(String action)` is true only for `android.intent.action.PACKAGE_ADDED`, `PACKAGE_REMOVED`, `PACKAGE_CHANGED`.

- [ ] **Step 1: Write failing refresh/lazy-provider tests**

```java
@Test public void packageRefreshPolicyIsNarrow() {
    assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_ADDED"));
    assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_REMOVED"));
    assertTrue(PackageRefreshPolicy.shouldReload("android.intent.action.PACKAGE_CHANGED"));
    assertFalse(PackageRefreshPolicy.shouldReload("android.intent.action.TIME_TICK"));
}
@Test public void onlyEnabledProviderIsConstructed() {
    List<OptionalRowRegistry.Key> created = new ArrayList<>();
    OptionalRowRegistry.loadEnabled(key -> key == OptionalRowRegistry.Key.APP_CHANNELS,
        key -> { created.add(key); return List::of; });
    assertEquals(List.of(OptionalRowRegistry.Key.APP_CHANNELS), created);
}
```

- [ ] **Step 2: Prove tests fail**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.PackageRefreshPolicyTest' --tests 'com.boop.shieldhome.OptionalRowRegistryTest' --stacktrace
```
Expected: failure until refresh policy/provider implementation exists.

- [ ] **Step 3: Implement local provider queries**

Use platform `TvContract` API 26+ columns needed for `title`, `intentUri`, `posterArtUri`. Filter channel rows in Java. Catch provider `RuntimeException` and return an empty list so favourites stay functional. Do not add any networking library or promotional provider.

- [ ] **Step 4: Run all Shield-home model/provider tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): add local optional TV rows"
```

---

### Task 5: Build remote-first HOME, Apps and Settings presentation

**Files:** create `TvAppCardView.java`, `ShieldHomeView.java`, `ShieldAppsView.java`, `ShieldHomeSettingsView.java`.

**Interfaces:**
- `TvAppCardView` scales locally `1f -> 1.08f` on focus and `1.08f -> 1f` on blur over 120 ms.
- `ShieldHomeView.Callbacks`: `onAppSelected(TvAppEntry)`, `onFavouriteLongPressed(TvAppEntry)`, `onOpenApps()`, `onOpenSettings()`, `onContentSelected(HomeContentCard)`.
- `ShieldHomeView.render(List<TvAppEntry> favourites, List<HomeRow> optionalRows, Callbacks)`.
- `ShieldAppsView.Callbacks`: `onAppSelected(TvAppEntry)`, `onToggleFavourite(TvAppEntry)`.
- `ShieldAppsView.render(List<TvAppEntry> apps, Set<String> favouriteComponents, Callbacks)`.
- `ShieldHomeSettingsView.Callbacks`: `onSetRowEnabled(OptionalRowRegistry.Key, boolean)`, `onChooseHomeApp()`, `onBackHome()`.
- `ShieldHomeSettingsView.render(boolean playNext, boolean appChannels, Callbacks)`.

- [ ] **Step 1: Implement local card focus animation**

```java
setOnFocusChangeListener((v, focused) -> animate()
    .scaleX(focused ? 1.08f : 1f)
    .scaleY(focused ? 1.08f : 1f)
    .setDuration(120)
    .start());
```
No system animation setting is read or written.

- [ ] **Step 2: Implement favourite-only HOME**

Use one vertical root and a `HorizontalScrollView` favourite row. Empty favourites render one remote-selectable `Add favourites` action. `Apps` and `Settings` are minimal navigation affordances, not content feeds. Optional rows are rendered only from the `optionalRows` argument; an empty list consumes zero row space.

- [ ] **Step 3: Implement Apps grid**

Use framework `GridView` + `BaseAdapter`. Select invokes `onAppSelected`; long press invokes `onToggleFavourite`. The view does not call `PackageManager` or preferences.

- [ ] **Step 4: Implement Settings and reversible HOME selection**

Render `Play Next: ON/OFF` and `App content rows: ON/OFF` as D-pad-selectable rows. `Choose Home app` launches `Settings.ACTION_HOME_SETTINGS`; catch failure and fall back to `Settings.ACTION_SETTINGS`. Never clear another launcher default or disable a package.

- [ ] **Step 5: Compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:compileDebugJavaWithJavac --stacktrace
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): add remote launcher views"
```

---

### Task 6: Add Shield launcher activity lifecycle, cache and remote editing

**Files:** create `ShieldLauncherActivity.java`; modify `unified/shield-manifest.xml`.

**Interfaces:**
- One `TvAppRepository`, `ShieldHomeStore`, single-thread `ExecutorService`, cached installed apps, cached favourite components and current page.
- Package reload only for `PackageRefreshPolicy.shouldReload(action)`.
- `showHome()`, `showApps()`, `showSettings()` use a local 140 ms alpha/translation page transition.
- Favourite long-press actions: `Move left`, `Move right`, `Remove from favourites`.

- [ ] **Step 1: Implement startup and cached reload**

```java
@Override protected void onCreate(Bundle state) {
    super.onCreate(state);
    repository = new TvAppRepository(this);
    store = new ShieldHomeStore(this);
    executor = Executors.newSingleThreadExecutor();
    registerPackageReceiver();
    reloadApps();
}
private void reloadApps() {
    executor.execute(() -> {
        List<TvAppEntry> apps = repository.load();
        List<String> saved = store.loadOrSeedFavourites(apps);
        List<String> reconciled = FavouriteOrder.reconcile(saved, apps);
        if (!reconciled.equals(saved)) store.saveFavourites(reconciled);
        runOnUiThread(() -> {
            installedApps = apps;
            favouriteComponents = reconciled;
            showCurrentPage();
        });
    });
}
```

- [ ] **Step 2: Implement package receiver with tested policy**

Listen to `ACTION_PACKAGE_ADDED`, `ACTION_PACKAGE_REMOVED`, `ACTION_PACKAGE_CHANGED` with data scheme `package`. On API 33+ register with `Context.RECEIVER_NOT_EXPORTED`; below API 33 use the compatible overload. Call `reloadApps()` only when `PackageRefreshPolicy.shouldReload(intent.getAction())`. Unregister and shut down executor in `onDestroy`.

- [ ] **Step 3: Implement app launching and favourite editing**

Use `ComponentName.unflattenFromString(entry.component())` in an explicit `ACTION_MAIN` intent. If launch fails because the activity became stale, call `reloadApps()` and return HOME. Favourite edits use `FavouriteOrder`, save once, and rerender cached HOME without package rescans.

- [ ] **Step 4: Keep optional providers off the favourite-only fast path**

```java
List<HomeRowProvider> providers = OptionalRowRegistry.loadEnabled(store::rowEnabled, TvProviderRows.factory(this));
if (providers.isEmpty()) {
    renderHome(List.of());
    return;
}
executor.execute(() -> loadAndPostOptionalRows(providers));
```
Provider failure omits that optional row for the session and does not block favourites.

- [ ] **Step 5: Declare internal activity without a second HOME filter**

```xml
<activity
    android:name="com.boop.shieldhome.ShieldLauncherActivity"
    android:exported="false"
    android:launchMode="singleTask"
    android:screenOrientation="landscape"
    android:theme="@style/Theme.BoopHome" />
```
`UnifiedEntryActivity` remains the only exported HOME/LAUNCHER entry point.

- [ ] **Step 6: Test/compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*ShieldEntryRouteTest' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:compileDebugJavaWithJavac --stacktrace
```
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add unified/shield-home unified/shield-manifest.xml
git commit -m "feat(shield-home): wire clean HOME activity"
```

---

### Task 7: Version, CI and signed APK contracts

**Files:** modify `unified/app-build.gradle`, `.github/workflows/build-boop-unified.yml`.

**Interfaces:** versionCode `46`; versionName `1.2.0-unified-shield-home`; existing package/signer identity unchanged.

- [ ] **Step 1: Bump only the unified candidate version**

```gradle
versionCode 46
versionName "1.2.0-unified-shield-home"
```

- [ ] **Step 2: Preserve current integration tests and add Shield-home tests**

The workflow’s integration-contract command must still include both current tests:
```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py tests/test_approved_eye_master_contract.py
```
After materialisation add:
```bash
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
```
Add `--tests '*ShieldEntryRouteTest'` to the generated app routing/lifecycle test command.

- [ ] **Step 3: Extend signed-APK structural verification**

Change version greps to 46 / `1.2.0-unified-shield-home`. Add `com.boop.shieldhome.ShieldLauncherActivity` to the manifest class loop. Keep `com.boop.alpha1.UnifiedEntryActivity` as launchable activity, verify `android.intent.category.HOME` remains in the manifest tree, and keep permanent signer digest comparison unchanged.

- [ ] **Step 4: Run the full non-visual local-equivalent verification**

```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py tests/test_approved_eye_master_contract.py
bash scripts/materialize-unified.sh
gradle --no-daemon -p launcher lintDebug --stacktrace
gradle --no-daemon -p shield-overlay :app:testDebugUnitTest --tests '*Home*Test' --tests '*Room*Test' --tests '*TvNavigationModelTest' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*BoopDeviceProfileTest' --tests '*ShieldEntryRouteTest' --tests '*BoopAssistant*' --stacktrace
```
Expected: PASS. No emulator, screenshot or appearance checks.

- [ ] **Step 5: Commit**

```bash
git add unified/app-build.gradle .github/workflows/build-boop-unified.yml
git commit -m "ci(unified): verify clean Shield HOME"
```

---

### Task 8: Review, publish, verify CI and update handoff

**Files:** modify `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` after exact GitHub results are read.

**Interfaces:** record exact implementation commit, completed workflow run, artifact name/id, APK SHA-256, signer SHA-256, test totals and `physical acceptance pending`. Do not update `main` unless a shared ownership/product contract changes.

- [ ] **Step 1: Fetch live heads before publication**

```bash
git fetch origin boop-unified main
```
If the live branch advanced, preserve and reconcile the new commits without reset or force push, then rerun affected tests.

- [ ] **Step 2: Review against every approved requirement**

Verify concrete code paths for favourite-only default, no advertising/Shop/Discover provider, independent optional rows OFF by default, lazy providers, stock HOME recovery, no global animation changes, cached package scans, remote add/remove/reorder, stale-app handling, and untouched unrelated BOOP behavior.

- [ ] **Step 3: Push reviewed code and inspect the completed GitHub workflow**

```bash
git push origin boop-unified
```
Read the completed workflow result and artifact metadata before claiming green.

- [ ] **Step 4: Require signed candidate evidence**

Require build success, `BOOP-Unified` artifact, `com.boop.alpha1`, version 46, expected permanent signer digest, APK ZIP integrity, approved-eye contract PASS, new Shield-home tests PASS, and existing focused Shield/unified tests PASS.

- [ ] **Step 5: Record CI-green, physical-pending state**

Update `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` with the exact GitHub values. Explicitly retain physical checks for HOME selection, favourite-only layout, absence of ads/Shop/Discover/blank ad space, smooth focus/scrolling, remote add/remove/reorder, optional-row persistence, stock-launcher restore and repeated-open scale stability.

- [ ] **Step 6: Commit docs, push and verify the live remote head**

```bash
git add SESSION_HANDOFF.md BOOP_STATUS.md BOOP_UNIFIED_MEMORY.md
git commit -m "docs(unified): hand off clean Shield HOME candidate"
git push origin boop-unified
git fetch origin boop-unified
git rev-parse origin/boop-unified
```
The reported commit must equal live GitHub `boop-unified` HEAD. Do not move the protected physical rollback until Ryan accepts the APK on real hardware.
