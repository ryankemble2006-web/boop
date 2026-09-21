#!/usr/bin/env python3
"""Verify both signed APKs against source and the hash-pinned accepted v206 APK."""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import zipfile

root = Path('boop-build/BOOP-Alpha1')
tools = Path(os.environ['ANDROID_HOME']) / 'build-tools/36.0.0'
out = Path('split-artifact'); out.mkdir(exist_ok=True)
expected = Path('shield-overlay/signing/boop-dev-cert-sha256.txt').read_text().strip().lower()
source = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
baseline = json.loads(Path('split/v206-native-baseline.json').read_text())
baseline_sha = 'b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca'
assert baseline['apkSha256'] == baseline_sha, 'Baseline provenance mismatch'
assert baseline['source'] == '9d57019d9370dbe3f47061b6e8b0ce8ed5134715'
assert baseline['signerSha256'] == expected, 'Baseline signer mismatch'
original_native = baseline['nativeSha256']
assert len(original_native) == 16, 'Baseline native library set changed'
receipt = {'source': source, 'baseline': '9d57019d9370dbe3f47061b6e8b0ce8ed5134715',
           'baselineApkSha256': baseline_sha, 'apps': []}

def tool(name, *args):
    return subprocess.check_output([str(tools/name), *map(str,args)], text=True)

# Hashes were extracted from the verified, signed v206 APK, then matched
# against both initial split APKs. This permanent source checkpoint does not
# depend on the retention period of a downloadable GitHub Actions artifact.

for body, package, label in [('wall','com.boop.alpha1','BOOP Wall'), ('shield','com.boop.shieldoverlay','BOOP Shield')]:
    version = 249 if body == 'shield' else 216
    apk = root / f'{body}-app/build/outputs/apk/debug/{body}-app-debug.apk'
    assert apk.is_file(), str(apk)
    badging = tool('aapt','dump','badging',apk)
    manifest = tool('aapt','dump','xmltree',apk,'AndroidManifest.xml')
    cert = tool('apksigner','verify','--print-certs',apk)
    assert f"package: name='{package}'" in badging
    assert f"versionCode='{version}'" in badging
    assert f"versionName='1.2.{version}-{body}'" in badging
    assert f"application-label:'{label}'" in badging
    assert "launchable-activity: name='com.boop.alpha1.UnifiedEntryActivity'" in badging
    digest = re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-fA-F]+)',cert).group(1).lower()
    assert digest == expected, 'Permanent signer changed'
    assert 'android.intent.category.HOME' in manifest
    assert 'boop_app_setup_v1' not in manifest
    assert 'com.boop.alpha1.BoopProfileActivity' in manifest
    assert 'com.boop.alpha1.UnifiedApplication' in manifest
    assert 'com.boop.shieldhome.ShieldLyricsActivity' in manifest
    assert re.search(r'android:allowBackup\([^\n]+\)=\(type 0x12\)0x0', manifest), 'Packaged app permits backup'
    if body == 'shield':
        assert 'com.boop.alpha1.johnny_states;com.boop.shieldoverlay.johnny_states' in manifest
        assert 'android.permission.INTERNET' in manifest, 'Shield weather cannot reach Open-Meteo'
    else:
        assert 'johnny_states' not in manifest
    with zipfile.ZipFile(apk) as archive:
        assert archive.testzip() is None
        dex = b''.join(archive.read(n) for n in archive.namelist() if re.fullmatch(r'classes\d*\.dex',n))
        for cls in ['BoopAppIdentity','BoopSetupState','BoopProfileActivity','UnifiedEntryActivity','BoopNaturalSpeechBackend','BoopSharedVoiceProfileRuntime','BoopVoiceSharingControls','BoopCanonicalFaceView','BoopAppearanceActivity','LocalPlayerCloseGate','BoopClosePlayerActivity','BoopDeezerHeartBackend','AdbCommandReceipt']:
            assert ('Lcom/boop/alpha1/'+cls+';').encode() in dex, cls
        for cls in ['ShieldHomeView','ShieldLyricsActivity','ShieldNowPlayingPuppetView','BassCaptureService','MusicBounceSource','BoopTvChrome','DeezerFavouritePolicy','DeezerFavouriteRequest','DeezerFavouriteController','DeezerFavouriteButton','DeezerFlowController','DeezerQueueController','ShieldQueueDialog']:
            assert ('Lcom/boop/shieldhome/'+cls+';').encode() in dex, cls
        for cls in ['RoomPanelController', 'RoomPanelSession']:
            assert ('Lcom/boop/shieldoverlay/'+cls+';').encode() in dex, cls
        for cls in ['ShieldRoomPanelView', 'RoomPanelLayout']:
            assert ('Lcom/boop/shieldhome/'+cls+';').encode() in dex, cls
        assert b'LOCAL_QUESTION' in dex and b'prepareMusicTurn' in dex and b'BOOP_MUSIC stage=' in dex
        assert b'smartHomePanelEnabled' in dex
        assert b'useTextOnlyFocus' in dex, 'Artist text-only focus helper missing from APK'
        assert b'applicationPackageName' in dex, 'Runtime close-marker owner missing from APK'
        assert b'Unsupported BOOP application' in dex, 'Close-marker owner validation missing from APK'
        assert b'Lcom/boop/launcher/MainActivity;' in dex, 'Built-in launcher lost'
        assert b'setup_intro_completed' in dex and b'Set up ' in dex
        for filename in ['boop-png-study.png','boop-hidden-felt.png','boop-felt-sign-blank.png','eyes.frag']:
            assert archive.read('assets/'+filename) == Path('unified/animation/assets',filename).read_bytes(), filename
        assert archive.read('assets/boopApprovedEyes.png') == Path('unified/assets/boop-eyes/boopApprovedEyes.png').read_bytes()
        for demo in Path('natural-voices/previews').glob('*.pcm'):
            assert archive.read('assets/boop-natural-voices/previews/' + demo.name) == demo.read_bytes(), demo.name
        natives = {n: hashlib.sha256(archive.read(n)).hexdigest()
                   for n in archive.namelist() if n.startswith('lib/') and n.endswith('.so')}
        assert natives == original_native, 'Packaged native runtime differs from accepted v206: ' + str(
            sorted(n for n in set(natives)|set(original_native) if natives.get(n)!=original_native.get(n)))
    name = f'BOOP-{body.title()}-v{version}.apk'
    shutil.copyfile(apk,out/name)
    sha = hashlib.sha256(apk.read_bytes()).hexdigest()
    (out/f'{body}-badging.txt').write_text(badging)
    (out/f'{body}-manifest.txt').write_text(manifest)
    (out/f'{body}-signer.txt').write_text(cert)
    receipt['apps'].append({'body':body,'package':package,'versionCode':version,'versionName':f'1.2.{version}-{body}',
                             'file':name,'sha256':sha,'signerSha256':digest,'bytes':apk.stat().st_size,
                             'identicalBaselineNativeLibraries': len(natives)})
(out/'built-commit.txt').write_text(source+'\n')
(out/'receipt.json').write_text(json.dumps(receipt,indent=2)+'\n')
for name in ['split-materialization.json','split-inherited-checks.txt','split-input-audit.txt']:
    shutil.copyfile(name,out/name)
print(json.dumps(receipt,indent=2))
