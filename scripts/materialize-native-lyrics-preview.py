#!/usr/bin/env python3
"""Build a separate emulator fixture from the exact production renderer, without test hooks in BOOP."""
from pathlib import Path
import shutil

root = Path('work/native-lyrics-preview')
root.mkdir(parents=True, exist_ok=True)
shutil.copy2('launcher/build.gradle', root / 'build.gradle')
(root / 'settings.gradle').write_text('''pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = 'BoopLyricsRuntimeFixture'
include ':app'
''', encoding='utf-8')
(root / 'gradle.properties').write_text('org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8\n', encoding='utf-8')
app = root / 'app'
java = app / 'src/main/java/com/boop/shieldhome'
java.mkdir(parents=True, exist_ok=True)
source = Path('unified/shield-home/src/main/java/com/boop/shieldhome')
for name in ('ShieldLyricsView', 'LyricsLinesView', 'DeezerLyricsDocument', 'NowPlayingSnapshot', 'NowPlayingActionPolicy'):
    shutil.copy2(source / (name + '.java'), java / (name + '.java'))
shutil.copy2('tests/runtime/LyricsPreviewActivity.java', java / 'LyricsPreviewActivity.java')
(app / 'src/main/AndroidManifest.xml').write_text('''<manifest xmlns:android="http://schemas.android.com/apk/res/android">
<uses-feature android:name="android.software.leanback" android:required="false" />
<application android:allowBackup="false" android:label="BOOP lyrics test" android:enableOnBackInvokedCallback="false" android:theme="@android:style/Theme.Material.NoActionBar">
<activity android:name="com.boop.shieldhome.LyricsPreviewActivity" android:exported="true" android:screenOrientation="landscape" android:launchMode="singleTop">
<intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LAUNCHER"/><category android:name="android.intent.category.LEANBACK_LAUNCHER"/></intent-filter>
</activity></application></manifest>
''', encoding='utf-8')
(app / 'build.gradle').write_text('''plugins { id 'com.android.application' }
android {
 namespace 'com.boop.shieldhome'
 compileSdk 36
 defaultConfig { applicationId 'com.boop.lyricspreview'; minSdk 29; targetSdk 36; versionCode 157; versionName '157-runtime-fixture' }
 signingConfigs { boopDev {
  storeFile file(System.getenv('BOOP_SIGNING_STORE_FILE'))
  storePassword System.getenv('BOOP_DEV_STORE_PASSWORD')
  keyAlias 'boop-dev'
  keyPassword System.getenv('BOOP_DEV_KEY_PASSWORD')
 } }
 buildTypes { debug { signingConfig signingConfigs.boopDev } }
 compileOptions { sourceCompatibility JavaVersion.VERSION_17; targetCompatibility JavaVersion.VERSION_17 }
}
''', encoding='utf-8')
print('Exact production renderer copied to separate emulator-only fixture. No BOOP source rewritten.')
