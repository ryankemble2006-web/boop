"""Build only the committed launcher/tools dependency closure, never materialize Unified."""
from pathlib import Path
import hashlib
import shutil

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'work/shield-launcher-src'
HOME = ROOT / 'unified/shield-home/src/main'
EXCLUDED = {'BoopMediaBridge.java', 'BoopMediaCornerService.java', 'BoopCastVisibilityService.java'}

def copy_tree(src, dst):
    if src.is_dir(): shutil.copytree(src, dst, dirs_exist_ok=True)

def replace_once(path, old, new):
    text = path.read_text(encoding='utf-8')
    if text.count(old) != 1: raise RuntimeError(f'{path.name}: expected one extraction anchor: {old}')
    path.write_text(text.replace(old, new, 1), encoding='utf-8')

if OUT.exists(): shutil.rmtree(OUT)
copy_tree(HOME/'java', OUT/'java')
for name in EXCLUDED: (OUT/'java/com/boop/shieldhome'/name).unlink()
copy_tree(ROOT/'shield-clean-launcher/app/src/main/res', OUT/'res')
copy_tree(HOME/'res', OUT/'res')
copy_tree(ROOT/'unified/tv-banners/res', OUT/'res')
(OUT/'assets').mkdir(parents=True, exist_ok=True)
shared=OUT/'java/com/boop/shared'; shared.mkdir(parents=True, exist_ok=True)
for name in ('BoopState.java', 'DeezerAlbumMatch.java', 'MediaRequest.java'):
    shutil.copy2(ROOT/'unified/shared'/name, shared/name)
copy_tree(ROOT/'shield-launcher/overrides/java', OUT/'java')
copy_tree(ROOT/'shield-launcher/overrides/res', OUT/'res')

home=OUT/'java/com/boop/shieldhome'
replace_once(home/'ShieldLauncherActivity.java',
    'startActivity(new Intent().setClassName(getPackageName(),"com.boop.alpha1.BoopProfileActivity"));',
    'openAccessibilitySettings();')
replace_once(home/'ShieldLauncherActivity.java',
    'startActivity(new Intent().setClassName(getPackageName(),"com.boop.shieldoverlay.BoopHomeActivity"));',
    'requestHomeRole();')
replace_once(home/'ShieldLauncherActivity.java', '        nowPlayingSnapshot = snapshot;',
    '        nowPlayingSnapshot = snapshot;\n        com.boop.shared.BoopState.INSTANCE.media(snapshot == null ? "" : Long.toString(snapshot.sessionId()), snapshot != null && snapshot.isPlaying(), android.os.SystemClock.uptimeMillis());')
replace_once(home/'ShieldLauncherActivity.java', 'startActivity(new Intent(homeOverrideSettingsAction()));', 'startActivity(new Intent(this, ShieldAccessibilityRouteActivity.class));')
replace_once(home/'ShieldHomeSettingsView.java', '"BOOP device and room settings"', '"Use BOOP with the Home button"')
replace_once(home/'ShieldHomeSettingsView.java', '"Home Assistant controls"', '"Choose default Home app"')
p=home/'ShieldNowPlayingManager.java'; text=p.read_text(encoding='utf-8')
old='com.boop.alpha1.BoopClosePlayerActivity'
if text.count(old)!=2: raise RuntimeError('Expected both explicit Close Player routes')
p.write_text(text.replace(old,'com.boop.shieldhome.LauncherClosePlayerActivity'),encoding='utf-8')
p=home/'StartupLocalBridge.java'; text=p.read_text(encoding='utf-8')
if text.count('boop-unified-local-adb.key')!=3: raise RuntimeError('Unexpected local ADB identity locations')
p.write_text(text.replace('boop-unified-local-adb.key','boop-launcher-local-adb.key'),encoding='utf-8')
# These PNGs are copied byte-for-byte. This verifies integrity, not appearance.
for source in (ROOT/'shield-clean-launcher/app/src/main/res').rglob('*.png'):
    dest=OUT/'res'/source.relative_to(ROOT/'shield-clean-launcher/app/src/main/res')
    if hashlib.sha256(source.read_bytes()).digest()!=hashlib.sha256(dest.read_bytes()).digest():
        raise RuntimeError('Launcher artwork changed: '+source.name)
print('Standalone launcher materialized:', len(list((OUT/'java').rglob('*.java'))), 'Java sources; no Unified/HA/voice build')
