#!/usr/bin/env python3
"""Add two shells around the existing, fully materialized BOOP implementation.

Run only after materialize-unified.sh and its inherited checks. No voice or
artwork transformation is performed. The common assistant source is not copied.
"""
from pathlib import Path
import hashlib
import json
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path('boop-build/BOOP-Alpha1')
A = '{http://schemas.android.com/apk/res/android}'
ET.register_namespace('android', A[1:-1])
manifest = ROOT / 'app/src/main/AndroidManifest.xml'
assert manifest.is_file(), 'Materialize the current combined source first'
assert not (ROOT / 'assistant-lib/build.gradle').exists(), 'Split already materialized; rebuild cleanly'

# Keep byte-level evidence for EVERY generated Java file and every current asset.
def digest_tree():
    return {p.relative_to(ROOT).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in ROOT.rglob('*') if p.is_file()
            and ('/src/main/java/' in p.as_posix() or '/src/main/assets/' in p.as_posix())}
before = digest_tree()

common = ROOT / 'assistant-lib'
(common / 'src/main').mkdir(parents=True)
shutil.copyfile('split/assistant-lib.gradle', common / 'build.gradle')
shutil.copyfile('split/app-shell.gradle', ROOT / 'split-app-shell.gradle')

# Relative names refer to the unchanged Java namespace, NOT the shell app ID.
document = ET.parse(manifest)
app = document.getroot().find('application')
assert app is not None
for node in app.iter():
    for attr in ('name', 'targetActivity', 'parentActivityName', 'backupAgent', 'appComponentFactory'):
        value = node.get(A + attr)
        if value and value.startswith('.'):
            node.set(A + attr, 'com.boop.alpha1' + value)
app.attrib.pop(A + 'label', None)
entry = next(n for n in app.findall('activity') if n.get(A + 'name') == 'com.boop.alpha1.UnifiedEntryActivity')
app.remove(entry)
provider = next(n for n in app.findall('provider') if n.get(A + 'name') == 'com.boop.alpha1.JohnnyStateProvider')
assert provider.get(A + 'authorities') == 'com.boop.alpha1.johnny_states'
app.remove(provider)  # Shield owns Johnny's legacy URI; Wall cannot collide.
document.write(common / 'src/main/AndroidManifest.xml', encoding='utf-8', xml_declaration=True)

for body in ('wall', 'shield'):
    target = ROOT / (body + '-app')
    (target / 'src/main').mkdir(parents=True)
    shutil.copyfile('split/' + body + '/build.gradle', target / 'build.gradle')
    shutil.copyfile('split/' + body + '/AndroidManifest.xml', target / 'src/main/AndroidManifest.xml')

# AppRepository accepts PackageManager only. The application UID identifies self
# without changing its public API or assuming a nonexistent Context field.
repo = ROOT / 'launcher-lib/src/main/java/com/boop/launcher/AppRepository.java'
text = repo.read_text()
old = 'if("com.boop.alpha1".equals(r.activityInfo.packageName))continue;'
assert text.count(old) == 1, 'Unexpected launcher self-filter implementation'
repo.write_text(text.replace(old,
    'if(r.activityInfo.applicationInfo != null && r.activityInfo.applicationInfo.uid == android.os.Process.myUid())continue;', 1))
subprocess.run([sys.executable, 'split/test-repository-uid.py'], check=True)

# The pinned, hash-verified AAR remains byte-for-byte unchanged. A local Maven
# coordinate lets Gradle propagate it correctly through an Android library.
aar = ROOT / 'app/libs/sherpa-onnx-1.13.7.aar'
assert aar.is_file()
vendor = ROOT / 'local-maven/com/boop/vendor/sherpa-onnx/1.13.7'
vendor.mkdir(parents=True)
shutil.copyfile(aar, vendor / 'sherpa-onnx-1.13.7.aar')
(vendor / 'sherpa-onnx-1.13.7.pom').write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion><groupId>com.boop.vendor</groupId><artifactId>sherpa-onnx</artifactId><version>1.13.7</version><packaging>aar</packaging></project>''')
settings = ROOT / 'settings.gradle'
s = settings.read_text()
s, count = re.subn(r'^include\(":app"\)\s*$', '', s, flags=re.M)
assert count == 1, 'Expected one combined application module'
s += '''\ninclude ':assistant-lib', ':wall-app', ':shield-app'
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("$rootDir/local-maven")
            content { includeGroup 'com.boop.vendor' }
        }
    }
}
'''
settings.write_text(s)

after = digest_tree()
allowed = 'launcher-lib/src/main/java/com/boop/launcher/AppRepository.java'
assert before.keys() == after.keys(), 'Split added or removed shared Java/assets'
assert [p for p in before if before[p] != after[p]] == [allowed], 'Unexpected shared source/asset changes'
report = {'shared_java_asset_files': len(before), 'changed_shared_inputs': [allowed],
          'sherpa_aar_sha256': hashlib.sha256(aar.read_bytes()).hexdigest(),
          'source_sha256_before': before, 'source_sha256_after': after}
Path('split-materialization.json').write_text(json.dumps(report, indent=2) + '\n')
print('Split: one shared assistant library, two application shells; Voice/artwork bytes unchanged')
