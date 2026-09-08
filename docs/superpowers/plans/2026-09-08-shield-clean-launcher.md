# Shield Clean Launcher Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a reversible, stock-feeling Nvidia Shield HOME inside the canonical unified BOOP APK that defaults to favourite apps only, never implements adverts/Shop/Discover, and lets users opt into approved local Android TV content rows later.

**Architecture:** Keep the new launcher isolated under `unified/shield-home/` and materialise it only into the generated `shield-lib`. `UnifiedEntryActivity` distinguishes a Shield HOME invocation from a normal BOOP/Leanback launch, sending HOME to `com.boop.shieldhome.ShieldLauncherActivity` while preserving the existing Shield puppet route for ordinary launches. The favourite-only path owns only cached installed-app state and persisted favourites; optional TV-provider rows are created only when their saved toggle is enabled.

**Tech Stack:** Java 17, Android SDK 36 with minSdk 29 in the unified app, Android framework Views, `PackageManager`, `SharedPreferences`, `TvContract`, JUnit 4, existing Gradle 9.6 unified materialisation/signing workflow.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-clean-launcher-design.md`

## Global Constraints

- Canonical branch: `boop-unified`.
- Canonical package remains `com.boop.alpha1`; permanent BOOP signer remains unchanged.
- Stock Google/Android TV launcher stays installed and must remain selectable as recovery.
- Installing BOOP must not silently select BOOP as HOME, disable Google components, change secure settings, or require privileged shell hacks.
- Default Shield HOME contains favourite apps plus minimal Apps/Settings navigation only.
- Advertising, sponsored placements, Shop, Discover, promotional autoplay and promotional/ad feed providers do not exist in this launcher.
- `PLAY_NEXT` and `APP_CHANNELS` are optional local TV-provider rows and default OFF independently.
- Disabled optional rows are not instantiated, queried, subscribed, polled or fetched.
- Do not modify global animation scales. Local focus/scroll/page transitions remain enabled.
- Preserve current phone Launcher, Wall, Shield puppet, HA naming/control, blink, eye, assistant, microphone, density and signing behavior unless the narrow HOME routing change requires otherwise.
- Automated tests are behavioral only. No screenshots, golden images, aesthetic source-string tests, emulator appearance grading or GitHub visual acceptance.
- Ryan owns real Shield appearance, animation and physical HOME acceptance.

---

## File Map

**Create:**
- `unified/ShieldEntryRoute.java` - pure routing decision for Shield HOME versus existing bodies.
- `unified/ShieldEntryRouteTest.java` - JVM routing contract.
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppEntry.java` - immutable launchable-TV-app model.
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppRepository.java` - one-shot LEANBACK/LAUNCHER catalogue scan and dedupe.
- `unified/shield-home/src/main/java/com/boop/shieldhome/FavouriteOrder.java` - pure add/remove/move/reconcile rules.
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeStore.java` - favourite/order and optional-row preferences.
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeContentCard.java` - optional-row card model.
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRow.java` - optional-row model.
- `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRowProvider.java` - optional provider interface.
- `unified/shield-home/src/main/java/com/boop/shieldhome/OptionalRowRegistry.java` - OFF-by-default provider gate.
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvProviderRows.java` - local `TvContract` Play Next/app-channel providers.
- `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java` - remote-focusable app/content card with local scale animation.
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java` - favourite-only HOME renderer plus optional rows when supplied.
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldAppsView.java` - all-TV-app grid and favourite toggle callbacks.
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java` - optional-row toggles and supported HOME-settings route.
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java` - lifecycle/cache/package receiver/navigation/launch orchestration.
- `unified/shield-home/src/test/java/com/boop/shieldhome/TvAppRepositoryTest.java`.
- `unified/shield-home/src/test/java/com/boop/shieldhome/FavouriteOrderTest.java`.
- `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldHomeStoreTest.java`.
- `unified/shield-home/src/test/java/com/boop/shieldhome/OptionalRowRegistryTest.java`.

**Modify:**
- `unified/UnifiedEntryActivity.java` - route only Shield HOME to new activity and keep existing Shield launch/setup path.
- `unified/shield-manifest.xml` - declare the internal Shield launcher activity.
- `unified/launcher-manifest.xml` - add package visibility for `LEANBACK_LAUNCHER` activities.
- `scripts/materialize-unified.sh` - copy new source/tests and routing helper into generated modules.
- `unified/app-build.gradle` - versionCode 46 / versionName `1.2.0-unified-shield-home`.
- `.github/workflows/build-boop-unified.yml` - run new generated unit tests and verify new activity/version in signed APK.
- `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` - record exact CI/signer state and physical acceptance boundary after implementation.

---

### Task 1: Split Shield HOME routing from the existing Shield puppet launch

**Files:**
- Create: `unified/ShieldEntryRoute.java`
- Create: `unified/ShieldEntryRouteTest.java`
- Modify: `unified/UnifiedEntryActivity.java`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- Produces: `ShieldEntryRoute.Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent)`.
- Produces: `String Target.className()` and `boolean Target.suppressEntryTransition()`.
- `SHIELD_HOME` class name is exactly `com.boop.shieldhome.ShieldLauncherActivity` and does not suppress the Android entry transition.
- `SHIELD_PUPPET` remains exactly `com.boop.shieldoverlay.MainActivity` and keeps current assistant first-run handling.

- [ ] **Step 1: Write the failing routing tests**

```java
package com.boop.alpha1;

import static org.junit.Assert.*;
import org.junit.Test;

public final class ShieldEntryRouteTest {
    @Test public void shieldHomeUsesCleanLauncher() {
        ShieldEntryRoute.Target target = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, true);
        assertEquals("com.boop.shieldhome.ShieldLauncherActivity", target.className());
        assertFalse(target.suppressEntryTransition());
    }

    @Test public void ordinaryShieldLaunchStillUsesPuppet() {
        ShieldEntryRoute.Target target = ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.SHIELD, false);
        assertEquals("com.boop.shieldoverlay.MainActivity", target.className());
        assertTrue(target.suppressEntryTransition());
    }

    @Test public void otherBodiesRemainUnchanged() {
        assertEquals("com.boop.alpha1.MainActivity",
                ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.WALL, true).className());
        assertEquals("com.boop.launcher.MainActivity",
                ShieldEntryRoute.resolve(BoopDeviceProfile.Mode.LAUNCHER, true).className());
    }
}
```

- [ ] **Step 2: Materialise and verify the new test fails before implementation**

Run:
```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*ShieldEntryRouteTest' --stacktrace
```
Expected: compile/test failure because `ShieldEntryRoute` is not present yet.

- [ ] **Step 3: Add the pure routing helper**

```java
package com.boop.alpha1;

final class ShieldEntryRoute {
    enum Target {
        SHIELD_HOME("com.boop.shieldhome.ShieldLauncherActivity", false),
        SHIELD_PUPPET("com.boop.shieldoverlay.MainActivity", true),
        WALL("com.boop.alpha1.MainActivity", true),
        HANDHELD_LAUNCHER("com.boop.launcher.MainActivity", true);

        private final String className;
        private final boolean suppressEntryTransition;
        Target(String className, boolean suppress) {
            this.className = className;
            this.suppressEntryTransition = suppress;
        }
        String className() { return className; }
        boolean suppressEntryTransition() { return suppressEntryTransition; }
    }

    static Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent) {
        if (mode == BoopDeviceProfile.Mode.SHIELD) {
            return homeIntent ? Target.SHIELD_HOME : Target.SHIELD_PUPPET;
        }
        return mode == BoopDeviceProfile.Mode.WALL ? Target.WALL : Target.HANDHELD_LAUNCHER;
    }
}
```

- [ ] **Step 4: Wire `UnifiedEntryActivity` without changing non-HOME Shield setup behavior**

Use `getIntent() != null && getIntent().hasCategory(Intent.CATEGORY_HOME)` to calculate `homeIntent`. Resolve the target before the assistant-choice gate. Run the assistant-choice gate only when target is `SHIELD_PUPPET`. Start `target.className()`, and call `overridePendingTransition(0, 0)` only when `target.suppressEntryTransition()` is true.

- [ ] **Step 5: Copy helper/test during materialisation and rerun routing tests**

Add:
```bash
cp unified/ShieldEntryRoute.java "$MAIN/ShieldEntryRoute.java"
cp unified/ShieldEntryRouteTest.java "$TEST/ShieldEntryRouteTest.java"
```

Run:
```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*BoopDeviceProfileTest' --tests '*ShieldEntryRouteTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 6: Commit this independently reviewable routing slice**

```bash
git add unified/ShieldEntryRoute.java unified/ShieldEntryRouteTest.java unified/UnifiedEntryActivity.java scripts/materialize-unified.sh
git commit -m "feat(unified): route Shield HOME separately"
```

---

### Task 2: Build the cached TV app catalogue and favourite-order model

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppEntry.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppRepository.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/FavouriteOrder.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/TvAppRepositoryTest.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/FavouriteOrderTest.java`
- Modify: `unified/launcher-manifest.xml`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- `TvAppEntry(String component, String packageName, String label)`; component uses `ComponentName.flattenToString()`.
- `TvAppRepository(Context context).load()` returns one alphabetically sorted list, querying `CATEGORY_LEANBACK_LAUNCHER` first and `CATEGORY_LAUNCHER` second, deduped by flattened component, excluding `context.getPackageName()`.
- `TvAppRepository.merge(String ownPackage, List<TvAppEntry> leanback, List<TvAppEntry> launcher)` is pure and unit-testable.
- `FavouriteOrder.reconcile`, `add`, `remove`, and `move` operate on component strings and never uninstall apps.

- [ ] **Step 1: Write catalogue merge and favourite model tests**

```java
@Test public void leanbackAndLauncherAreDedupedAndBoopIsExcluded() {
    List<TvAppEntry> out = TvAppRepository.merge("com.boop.alpha1",
        List.of(new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                new TvAppEntry("com.boop.alpha1/.Entry", "com.boop.alpha1", "BOOP")),
        List.of(new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                new TvAppEntry("other/.Main", "other", "Alpha")));
    assertEquals(List.of("other/.Main", "pkg/.Tv"),
        out.stream().map(TvAppEntry::component).toList());
}

@Test public void reconcileDropsStaleButPreservesSavedOrder() {
    List<String> saved = List.of("b/.B", "gone/.Gone", "a/.A");
    List<TvAppEntry> installed = List.of(
        new TvAppEntry("a/.A", "a", "A"), new TvAppEntry("b/.B", "b", "B"));
    assertEquals(List.of("b/.B", "a/.A"), FavouriteOrder.reconcile(saved, installed));
}

@Test public void moveAndRemoveAreStable() {
    assertEquals(List.of("b", "a", "c"), FavouriteOrder.move(List.of("a", "b", "c"), "b", -1));
    assertEquals(List.of("a", "c"), FavouriteOrder.remove(List.of("a", "b", "c"), "b"));
}
```

- [ ] **Step 2: Extend materialisation for Shield-home main/test source and verify tests fail**

Add before Shield tests are run:
```bash
mkdir -p "$ROOT/shield-lib/src/test/java"
cp -R unified/shield-home/src/main/java/* "$ROOT/shield-lib/src/main/java/"
cp -R unified/shield-home/src/test/java/* "$ROOT/shield-lib/src/test/java/"
```

Run:
```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
```
Expected: failure until the models are implemented.

- [ ] **Step 3: Implement the catalogue and pure favourite operations**

`load()` must perform exactly two package scans per refresh cycle:
```java
List<TvAppEntry> leanback = query(Intent.CATEGORY_LEANBACK_LAUNCHER);
List<TvAppEntry> launcher = query(Intent.CATEGORY_LAUNCHER);
return merge(context.getPackageName(), leanback, launcher);
```
Do not call `load()` from focus listeners or view rendering.

- [ ] **Step 4: Add TV package visibility**

Add a second `<queries><intent>` entry in `unified/launcher-manifest.xml` for `MAIN` + `LEANBACK_LAUNCHER`. Keep the existing ordinary launcher query.

- [ ] **Step 5: Run generated Shield-home unit tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.TvAppRepositoryTest' --tests 'com.boop.shieldhome.FavouriteOrderTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 6: Commit catalogue/model slice**

```bash
git add unified/shield-home unified/launcher-manifest.xml scripts/materialize-unified.sh
git commit -m "feat(shield-home): add TV app and favourite model"
```

---

### Task 3: Persist favourites and gate optional rows OFF by default

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeStore.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/HomeContentCard.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRow.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/HomeRowProvider.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/OptionalRowRegistry.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldHomeStoreTest.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/OptionalRowRegistryTest.java`

**Interfaces:**
- SharedPreferences file: `boop_shield_home_v1`.
- Favourite keys: `favourites_initialised`, `favourites_json`.
- Optional keys: `row_play_next`, `row_app_channels`, both default false.
- `ShieldHomeStore.loadOrSeedFavourites(List<TvAppEntry> installed)` seeds installed TV components only once; after initialisation an intentionally empty favourite list remains empty.
- `OptionalRowRegistry.Key` values are exactly `PLAY_NEXT` and `APP_CHANNELS`.
- `OptionalRowRegistry.loadEnabled(EnabledLookup enabled, ProviderFactory factory)` creates a provider only after its key evaluates true.

- [ ] **Step 1: Write persistence codec/default and provider-creation tests**

```java
@Test public void favouritesRoundTripAsJson() {
    String raw = ShieldHomeStore.encodeComponents(List.of("a/.A", "b/.B"));
    assertEquals(List.of("a/.A", "b/.B"), ShieldHomeStore.decodeComponents(raw));
}

@Test public void malformedFavouriteDataFailsSafeToEmpty() {
    assertEquals(List.of(), ShieldHomeStore.decodeComponents("not-json"));
}

@Test public void disabledRowsCreateNoProviders() {
    AtomicInteger created = new AtomicInteger();
    List<HomeRowProvider> providers = OptionalRowRegistry.loadEnabled(
        key -> false,
        key -> { created.incrementAndGet(); return List::of; });
    assertTrue(providers.isEmpty());
    assertEquals(0, created.get());
}

@Test public void rowTogglesAreIndependent() {
    List<HomeRowProvider> providers = OptionalRowRegistry.loadEnabled(
        key -> key == OptionalRowRegistry.Key.PLAY_NEXT,
        key -> List::of);
    assertEquals(1, providers.size());
}
```

- [ ] **Step 2: Run tests and confirm failure**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.ShieldHomeStoreTest' --tests 'com.boop.shieldhome.OptionalRowRegistryTest' --stacktrace
```
Expected: failure until store/registry exist.

- [ ] **Step 3: Implement fail-safe storage and lazy registry**

`rowEnabled(Key key)` must use `preferences.getBoolean(key.preferenceKey(), false)`. `setRowEnabled` writes only the selected key. `loadOrSeedFavourites` stores all currently installed launchable TV components only when `favourites_initialised` is false, then marks it true. Later newly installed apps appear in Apps but are not auto-favourited.

- [ ] **Step 4: Run tests**

Use the command from Step 2. Expected: PASS.

- [ ] **Step 5: Commit storage/registry slice**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): persist favourites and optional rows"
```

---

### Task 4: Implement local Android TV Play Next and app-channel providers

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/TvProviderRows.java`
- Modify: `unified/shield-home/src/test/java/com/boop/shieldhome/OptionalRowRegistryTest.java`

**Interfaces:**
- `TvProviderRows.factory(Context context)` returns an `OptionalRowRegistry.ProviderFactory`.
- `PLAY_NEXT` queries `TvContract.WatchNextPrograms.CONTENT_URI` only when created and loaded.
- `APP_CHANNELS` queries `TvContract.Channels.CONTENT_URI`, keeps browsable `TYPE_PREVIEW` channels, then queries `TvContract.buildPreviewProgramsUriForChannel(id)`.
- Provider errors return an empty list for that provider and never break favourites.
- No provider performs HTTP/network requests.

- [ ] **Step 1: Extend the registry test to prove provider factory creation remains lazy**

```java
@Test public void onlyEnabledProviderIsConstructed() {
    List<OptionalRowRegistry.Key> created = new ArrayList<>();
    OptionalRowRegistry.loadEnabled(
        key -> key == OptionalRowRegistry.Key.APP_CHANNELS,
        key -> { created.add(key); return List::of; });
    assertEquals(List.of(OptionalRowRegistry.Key.APP_CHANNELS), created);
}
```

- [ ] **Step 2: Implement `TvProviderRows` with local `ContentResolver` queries**

Use platform `TvContract` constants available from API 26. Query only columns needed to build `HomeContentCard(title, intentUri, posterArtUri)`. Filter channel rows in Java rather than assuming provider SQL selection support. Wrap `query`/cursor processing in `RuntimeException` handling that returns an empty list.

- [ ] **Step 3: Compile and run all Shield-home tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
```
Expected: PASS.

- [ ] **Step 4: Commit provider slice**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): add optional local TV rows"
```

---

### Task 5: Build remote-first HOME, Apps and Settings views with local animation

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldAppsView.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java`

**Interfaces:**
- `TvAppCardView` is focusable/clickable, renders an app/content label and optional icon, and animates local scale from `1f` to `1.08f` over 120 ms on focus, back to `1f` on blur.
- `ShieldHomeView.render(List<TvAppEntry> favourites, List<HomeRow> optionalRows, Callbacks callbacks)` never performs discovery or storage I/O.
- `ShieldAppsView.render(List<TvAppEntry> apps, Set<String> favourites, Callbacks callbacks)` owns no persistence.
- `ShieldHomeSettingsView.render(boolean playNext, boolean appChannels, Callbacks callbacks)` exposes toggle callbacks plus `onChooseHomeApp()`.

- [ ] **Step 1: Implement the card focus behavior without global animation changes**

Core focus behavior:
```java
setOnFocusChangeListener((v, focused) -> animate()
    .scaleX(focused ? 1.08f : 1f)
    .scaleY(focused ? 1.08f : 1f)
    .setDuration(120)
    .start());
```
Do not call `Settings.Global`, `ValueAnimator.setDurationScale`, shell settings, or system animation APIs.

- [ ] **Step 2: Implement favourite-only HOME rendering**

Use one vertical root and one `HorizontalScrollView` favourites row. With no favourites, render one remote-selectable `Add favourites` action that opens Apps. Add minimal `Apps` and `Settings` actions without creating an additional content feed row. Append only the `HomeRow` objects passed to `render`; empty optional rows consume zero layout space.

- [ ] **Step 3: Implement Apps grid and remote long-press callbacks**

Use framework `GridView`/`BaseAdapter`. Select launches the app. Long press invokes a callback to add/remove favourite membership. The view receives cached data only and never calls `PackageManager` itself.

- [ ] **Step 4: Implement Settings toggles and stock-launcher recovery route**

The two row toggles show explicit ON/OFF text and toggle on D-pad Select. `Choose Home app` opens `Settings.ACTION_HOME_SETTINGS`; if unavailable, fall back to `Settings.ACTION_SETTINGS`. Do not clear defaults programmatically or disable another launcher.

- [ ] **Step 5: Compile generated Shield library**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:compileDebugJavaWithJavac --stacktrace
```
Expected: PASS.

- [ ] **Step 6: Commit view slice**

```bash
git add unified/shield-home
git commit -m "feat(shield-home): add remote launcher views"
```

---

### Task 6: Add launcher activity lifecycle, cache refresh and favourite editing

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java`
- Modify: `unified/shield-manifest.xml`

**Interfaces:**
- Activity owns one `TvAppRepository`, one `ShieldHomeStore`, one background `ExecutorService`, cached `List<TvAppEntry> installedApps`, cached favourite components and current page.
- Package scans occur at initial load, explicit lifecycle reload and package add/remove/change broadcasts, never on focus or redraw.
- `showHome()`, `showApps()`, and `showSettings()` swap presentation views with a local ~140 ms alpha/translation transition.
- Long-press favourite edit actions are exactly `Move left`, `Move right`, `Remove from favourites` with invalid edge moves disabled/omitted.

- [ ] **Step 1: Implement activity startup and cached reload**

Pseudo-structure to follow exactly:
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
        runOnUiThread(() -> { installedApps = apps; favouriteComponents = reconciled; showCurrentPage(); });
    });
}
```

- [ ] **Step 2: Register package-change refresh only**

Listen for `ACTION_PACKAGE_ADDED`, `ACTION_PACKAGE_REMOVED`, and `ACTION_PACKAGE_CHANGED` with data scheme `package`. Use `Context.RECEIVER_NOT_EXPORTED` on API 33+ and the compatible overload below it. Unregister in `onDestroy`.

- [ ] **Step 3: Implement launching and favourite edit actions**

Parse `TvAppEntry.component()` with `ComponentName.unflattenFromString`, launch with an explicit `ACTION_MAIN` intent, and catch stale/unavailable activity failures by calling `reloadApps()` and returning HOME. Long-press edit updates `FavouriteOrder`, saves once, then rerenders cached HOME without rescanning packages.

- [ ] **Step 4: Load optional providers only after settings say they are enabled**

`showHome()` calls `OptionalRowRegistry.loadEnabled(store::rowEnabled, TvProviderRows.factory(this))`. If the returned provider list is empty, render favourites immediately and do no TV-provider work. When non-empty, load those rows on the background executor and post the resulting optional rows without blocking basic favourites navigation.

- [ ] **Step 5: Declare activity in the generated Shield manifest**

Add to `unified/shield-manifest.xml`:
```xml
<activity
    android:name="com.boop.shieldhome.ShieldLauncherActivity"
    android:exported="false"
    android:launchMode="singleTask"
    android:screenOrientation="landscape"
    android:theme="@style/Theme.BoopHome" />
```
Do not add a second HOME intent filter. `UnifiedEntryActivity` remains the one exported HOME/LAUNCHER entry point.

- [ ] **Step 6: Materialise, compile and run all new unit tests**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*ShieldEntryRouteTest' --stacktrace
```
Expected: PASS.

- [ ] **Step 7: Commit activity/integration slice**

```bash
git add unified/shield-home unified/shield-manifest.xml
git commit -m "feat(shield-home): wire clean HOME activity"
```

---

### Task 7: Version, CI and signed-package contracts

**Files:**
- Modify: `unified/app-build.gradle`
- Modify: `.github/workflows/build-boop-unified.yml`

**Interfaces:**
- versionCode becomes `46`.
- versionName becomes `1.2.0-unified-shield-home`.
- Existing signer/package checks remain unchanged except version expectations and addition of `com.boop.shieldhome.ShieldLauncherActivity` to manifest-class verification.

- [ ] **Step 1: Bump only the unified candidate version**

```gradle
versionCode 46
versionName "1.2.0-unified-shield-home"
```

- [ ] **Step 2: Add non-visual generated Shield-home tests to CI**

After materialisation, run:
```bash
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest \
  --tests 'com.boop.shieldhome.*' --stacktrace
```
Add `--tests '*ShieldEntryRouteTest'` to the existing unified routing/lifecycle unit-test command.

- [ ] **Step 3: Extend APK structural verification**

Change version greps to 46 / `1.2.0-unified-shield-home`, and add `com.boop.shieldhome.ShieldLauncherActivity` to the manifest class loop. Keep `com.boop.alpha1.UnifiedEntryActivity` as the launchable activity and keep permanent signer comparison intact.

- [ ] **Step 4: Run the full non-visual local-equivalent verification**

```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py
bash scripts/materialize-unified.sh
gradle --no-daemon -p launcher lintDebug --stacktrace
gradle --no-daemon -p shield-overlay :app:testDebugUnitTest --tests '*Home*Test' --tests '*Room*Test' --tests '*TvNavigationModelTest' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :shield-lib:testDebugUnitTest --tests 'com.boop.shieldhome.*' --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests '*BoopDeviceProfileTest' --tests '*ShieldEntryRouteTest' --tests '*BoopAssistant*' --stacktrace
```
Expected: PASS. Do not add emulator, screenshot or appearance tests.

- [ ] **Step 5: Commit release-contract slice**

```bash
git add unified/app-build.gradle .github/workflows/build-boop-unified.yml
git commit -m "ci(unified): verify clean Shield HOME"
```

---

### Task 8: Review, publish, verify live CI and record handoff

**Files:**
- Modify after code/CI results are known: `SESSION_HANDOFF.md`
- Modify after code/CI results are known: `BOOP_STATUS.md`
- Modify after code/CI results are known: `BOOP_UNIFIED_MEMORY.md`

**Interfaces:**
- Handoff records exact implementation commit, successful workflow run, artifact name/id, APK SHA-256, signer SHA-256, tests, and `physical acceptance pending`.
- Do not alter `main` unless a shared product/ownership contract actually changes; this launcher implementation is branch-owned progress.

- [ ] **Step 1: Fetch live `boop-unified` and `main` again before publication**

```bash
git fetch origin boop-unified main
```
If `boop-unified` advanced, reconcile without reset/force push and rerun affected tests.

- [ ] **Step 2: Review the implementation against the approved spec**

Confirm every spec item has a concrete code path: favourite-only default, no advert/Shop/Discover provider, optional rows OFF and lazy, stock HOME recovery, no global animation changes, cached package scans, remote edit controls, stale-app handling, and no unrelated BOOP behavior changes.

- [ ] **Step 3: Publish reviewed code and let the existing workflow build/sign the APK**

```bash
git push origin boop-unified
```
Wait only for the synchronous GitHub workflow result available during the session. Do not claim green before reading the completed run and artifact metadata.

- [ ] **Step 4: Verify the workflow evidence**

Require: build success, signed `BOOP-Unified` artifact, package `com.boop.alpha1`, version 46, expected permanent signer digest, APK ZIP integrity, new Shield-home tests PASS, existing focused Shield/unified tests PASS.

- [ ] **Step 5: Update branch handoff/status/memory with CI-green but physical-pending evidence**

Record the exact values returned by GitHub. Explicitly state that Ryan still must physically confirm HOME selection, favourite-only layout, no ads/Shop/Discover/blank ad space, smooth focus/scrolling, remote add/remove/reorder, optional-row persistence, stock-launcher restore and repeated-open scale stability.

- [ ] **Step 6: Commit/push documentation and verify live remote HEAD one final time**

```bash
git add SESSION_HANDOFF.md BOOP_STATUS.md BOOP_UNIFIED_MEMORY.md
git commit -m "docs(unified): hand off clean Shield HOME candidate"
git push origin boop-unified
git fetch origin boop-unified
git rev-parse origin/boop-unified
```
The final reported commit must equal live GitHub `boop-unified` HEAD. Do not promote the protected physical rollback until Ryan accepts the APK on real hardware.
