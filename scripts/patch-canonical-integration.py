#!/usr/bin/env python3
"""Scoped integration adapters; old materialization stages run first, artwork stays untouched."""
from pathlib import Path
import shutil

ROOT = Path('boop-build/BOOP-Alpha1')
MAIN = ROOT/'app/src/main/java/com/boop/alpha1'
SHIELD = ROOT/'shield-lib/src/main/java/com/boop/shieldoverlay'

def once(text, old, new):
    if text.count(old) != 1:
        raise SystemExit(f'Canonical integration expected one anchor: {old[:100]!r}, found {text.count(old)}')
    return text.replace(old,new,1)

shared=ROOT/'shield-lib/src/main/java/com/boop/shared'
shared.mkdir(parents=True,exist_ok=True)
for path in Path('unified/shared').glob('*.java'): shutil.copy2(path,shared/path.name)
home=ROOT/'shield-home-lib'
shutil.copytree('unified/shield-home/src/main/java',home/'src/main/java',dirs_exist_ok=True)
shutil.copytree('shield-clean-launcher/app/src/main/res',home/'src/main/res',dirs_exist_ok=True)
shutil.copytree('unified/shield-home/src/main/res',home/'src/main/res',dirs_exist_ok=True)
shutil.copy2('unified/shield-home-lib.gradle',home/'build.gradle')
shutil.copy2('unified/shield-home-manifest.xml',home/'src/main/AndroidManifest.xml')
with (ROOT/'settings.gradle').open('a') as file: file.write("\ninclude ':shield-home-lib'\n")
for filename in ['BoopClosePlayerActivity.java','LocalPlayerCloseGate.java','LocalPlayerCloseClient.java','BoopHomeOverrideService.java','BoopProfileActivity.java','BoopLocalMedia.java','DeezerArtistClient.java','DeezerCatalogue.java','DeezerNativeController.java']:
    shutil.copy2(Path('unified')/filename,MAIN/filename)

ha_client=MAIN/'HomeAssistantClient.java'
text=ha_client.read_text()
text=once(text,'            String colour = LightColourCommandParser.parseColour(text);','''            CommandOutcome artistOutcome = new DeezerArtistClient().process(
                    baseUrl, accessToken, text, room, roomSource);
            if (artistOutcome != null) return artistOutcome;

            String colour = LightColourCommandParser.parseColour(text);''')
ha_client.write_text(text)
for test in ['LocalPlayerCloseClientTest.java','DeezerArtistClientTest.java','DeezerNativeTest.java','DeezerDirectTest.java']:
    shutil.copy2('unified/'+test,ROOT/'app/src/test/java/com/boop/alpha1'/test)

manifest=ROOT/'app/src/main/AndroidManifest.xml'
text=manifest.read_text()
# Android 16 otherwise bypasses the existing Back/long-Back handlers.
text=once(text,'android:allowBackup="false"','android:allowBackup="false" android:enableOnBackInvokedCallback="false"')
text=once(text,'    <application', '    <queries><package android:name="deezer.android.app" /></queries>\n    <application')
text=once(text,'        <activity\n            android:name=".UnifiedEntryActivity"', '        <activity android:name=".BoopProfileActivity" android:exported="false" android:theme="@style/Theme.BOOP" />\n        <activity\n            android:name=".UnifiedEntryActivity"')
text=once(text,'        <activity android:name=".BoopProfileActivity"', '        <activity android:name=".BoopClosePlayerActivity" android:exported="false" android:theme="@style/Theme.BOOP" />\n        <activity android:name=".BoopProfileActivity"')
manifest.write_text(text)

wall=MAIN/'MainActivity.java'
text=wall.read_text()
shutil.copy2('unified/BoopVoiceTokenStore.java',MAIN/'BoopVoiceTokenStore.java')
text=once(text,'tokenStore = new SecureTokenStore(this);','tokenStore = BoopVoiceTokenStore.create(this);')
text=once(text,'    private void ensureHouseConnection() {','''    private void ensureHouseConnection() {
        if (BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD) {
            startActivity(new Intent(this, com.boop.shieldoverlay.BoopHomeActivity.class));
            finish(); // The next remote request must reload any repaired pairing.
            return;
        }''')
text=once(text,'        handleAuthIntent(intent);','''        handleAuthIntent(intent);
        if (intent.getBooleanExtra("boop_open_voice_settings", false)) {
            interactionSurface.post(this::showVoiceSettings);
        }''')
text=once(text,'        handleAuthIntent(getIntent());','''        handleAuthIntent(getIntent());
        if (getIntent().getBooleanExtra("boop_open_voice_settings", false)) {
            interactionSurface.post(this::showVoiceSettings);
        }''')
text=once(text,'    private void handleRecognizedSpeech(String transcript) {','''    private void handleRecognizedSpeech(String transcript) {
        if ("boop settings".equalsIgnoreCase(transcript.trim()) || "device settings".equalsIgnoreCase(transcript.trim())) {
            startActivity(new Intent(this,BoopProfileActivity.class)); return;
        }
        if (BoopLocalMedia.handle(this,transcript,this::speak)) return;''')
text=once(text,'        voiceSettingsOverlay.addView(done, doneParams);','''        Button profiles = new Button(this);
        profiles.setText("Device and room settings"); profiles.setTextSize(20f);
        profiles.setOnClickListener(v -> startActivity(new Intent(this,BoopProfileActivity.class)));
        voiceSettingsOverlay.addView(profiles);
        voiceSettingsOverlay.addView(done, doneParams);''')
# Publish speech/lifecycle state without altering the accepted natural runtime/selection path.
text=once(text,'    private void speak(String text) {','''    private void speak(String text) {
        com.boop.shared.BoopState.INSTANCE.speech(false,true);''')
text=once(text,'    private void finishTtsUtterance() {','''    private void finishTtsUtterance() {
        com.boop.shared.BoopState.INSTANCE.speech(false,false);''')
text=text.replace('        listening = true;','        listening = true;\n        com.boop.shared.BoopState.INSTANCE.speech(true,false);')
text=text.replace('        listening = false;','        listening = false;\n        com.boop.shared.BoopState.INSTANCE.speech(false,false);')
wall.write_text(text)

# Back from HA controls returns to its caller; it must never show the old omnipresent overlay.
ha=SHIELD/'BoopHomeActivity.java'
text=ha.read_text()
text=once(text,'        BoopOverlayController.show(this);','        // Canonical media ownership decides whether a corner is visible.')
text=once(text,'        boolean hasRoom = preferences != null && preferences.hasSelectedRoom();','''        boolean chooseRoom = getIntent().getBooleanExtra("boop_choose_room",false);
        getIntent().removeExtra("boop_choose_room");
        boolean hasRoom = !chooseRoom && preferences != null && preferences.hasSelectedRoom();''')
ha.write_text(text)

# Phone launcher gets an always available hardware MENU and long-Back-free settings entry.
launcher=ROOT/'launcher-lib/src/main/java/com/boop/launcher/MainActivity.java'
text=launcher.read_text()
text=once(text,'menu.getMenu().add("Home settings");','menu.getMenu().add("Home settings");menu.getMenu().add("BOOP settings");')
text=once(text,'else openHomeSettings();','else if("BOOP settings".contentEquals(item.getTitle()))startActivity(new android.content.Intent().setClassName(getPackageName(),"com.boop.alpha1.BoopProfileActivity"));else openHomeSettings();')
marker=text.rfind('}')
text=text[:marker]+'''\n @Override public boolean onCreateOptionsMenu(android.view.Menu menu) {
  menu.add("BOOP device and room settings").setOnMenuItemClickListener(item -> {
   startActivity(new android.content.Intent().setClassName(getPackageName(),"com.boop.alpha1.BoopProfileActivity")); return true;
  }); return true;
 }
'''+text[marker:]
launcher.write_text(text)
print('Canonical shared state, existing Shield Home, settings and escape adapters integrated')

# Unify the dashboard room with the voice/profile room, preserving old room selections.
prefs=SHIELD/'BoopPreferences.java'
text=prefs.read_text()
text=once(text,'    private final Store store;','    private final Store store;\n    private SharedPreferences roomPreferences;')
text=once(text,'        this.store = new SharedPreferencesStore(preferences);','''        this.store = new SharedPreferencesStore(preferences);
        roomPreferences = context.getApplicationContext().getSharedPreferences("boop_unified", Context.MODE_PRIVATE);
        if (!roomPreferences.contains("room_name")) {
            AreaInfo legacy = selectedRoom();
            if (legacy != null) saveSharedRoom(legacy);
        }''')
text=once(text,'    public AreaInfo selectedRoom() {','''    private void saveSharedRoom(AreaInfo area) {
        if (roomPreferences == null) return;
        roomPreferences.edit().putString("room_id",area.id()).putString("room_name",area.name()).apply();
        com.boop.shared.BoopState.INSTANCE.room(area.id(),area.name());
    }
    public AreaInfo selectedRoom() {
        if (roomPreferences != null && roomPreferences.contains("room_name")) {
            String id = clean(roomPreferences.getString("room_id",null));
            String name = clean(roomPreferences.getString("room_name",null));
            return id == null || name == null ? null : new AreaInfo(id,name);
        }''')
text=once(text,'        store.putString(KEY_AREA_NAME, area.name());','        store.putString(KEY_AREA_NAME, area.name());\n        saveSharedRoom(area);')
text=once(text,'    public void clearSelectedRoom() {','''    public void clearSelectedRoom() {
        if (roomPreferences != null) {
            roomPreferences.edit().remove("room_id").remove("room_name").apply();
            com.boop.shared.BoopState.INSTANCE.room("living_room","Living Room");
        }''')
prefs.write_text(text)
