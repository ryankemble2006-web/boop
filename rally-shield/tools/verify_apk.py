"""Inspect the compiled package, not visual layout or screenshot appearance."""
from pathlib import Path
import hashlib
import os
import re
import subprocess
import sys
import zipfile

EXPECTED_CERT='f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde'
def main():
    apk=Path(sys.argv[1]); tools=Path(os.environ['ANDROID_HOME'])/'build-tools/36.0.0'
    badging=subprocess.check_output([str(tools/'aapt'),'dump','badging',str(apk)],text=True)
    for required in ["package: name='com.boop.rally'", "versionCode='1'", "launchable-activity: name='com.boop.rally.MainActivity'"]:
        if required not in badging:raise RuntimeError('Package identity mismatch: '+required)
    if 'uses-permission:' in badging:raise RuntimeError('Unexpected Android permission')
    manifest=subprocess.check_output([str(tools/'aapt'),'dump','xmltree',str(apk),'AndroidManifest.xml'],text=True)
    if 'android.intent.category.LEANBACK_LAUNCHER' not in manifest:raise RuntimeError('Missing TV entry')
    if 'android.intent.category.HOME' in manifest:raise RuntimeError('Must not replace HOME')
    cert=subprocess.check_output([str(tools/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True)
    m=re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)',cert)
    if not m or m.group(1)!=EXPECTED_CERT:raise RuntimeError('Permanent signer mismatch')
    with zipfile.ZipFile(apk) as z:
        for name in ['classes.dex','lib/arm64-v8a/libretro.so','lib/arm64-v8a/libbooprally.so','assets/licenses/LICENSE']:
            if name not in z.namelist():raise RuntimeError('Missing runtime component: '+name)
        for name in z.namelist():
            if name.lower().endswith(('.exe','.zip','.iso','.jks','.keystore')):raise RuntimeError('Forbidden bundled content: '+name)
    apk.with_name('apk-sha256.txt').write_text(hashlib.sha256(apk.read_bytes()).hexdigest()+'  '+apk.name+'\n')
    apk.with_name('signer.txt').write_text(cert)
    apk.with_name('package.txt').write_text(badging)
    print('Package, ABI, permanent signer, no-permission and no-game-content checks passed.')
if __name__=='__main__':main()
