"""Native Recents preload patch for v418. GitHub supplies permanent signing."""
import argparse
import shutil
import difflib
import copy
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile
import struct

BASE_SHA = '898d462b416c51c7ba7c2e2a3d615ded19f50b4c257a4940f8298813a3c5138a'
TOOL_SHA = '66cf4524a4a45a7f56567d08b2c9b6ec237bcdd78cee69fd4a59c8a0243aeafa'
CLASS = 'com/boop/shieldhome/BoopKeyActionExecutor.smali'
GLOBAL_GRANT = 'wm size 3839x2160 >/dev/null 2>&1; wm density 640 >/dev/null 2>&1; '

def digest(data):
    return hashlib.sha256(data).hexdigest()

def patch_display(text):
    marker = text.index('instance-of p2, p1, Landroid/accessibilityservice/AccessibilityService;')
    start = text.rfind('    :pswitch_', 0, marker)
    if start < 0:
        raise ValueError('Recents switch label missing')
    label = text[start:text.index('\n', start)]
    end = text.index('    const-string p1, "Dispatched: Recents / Task Switcher"', start)
    return text[:start] + label + '\n    invoke-static {p1}, Lcom/boop/shieldhome/RecentsPreload;->open(Landroid/content/Context;)V\n\n' + text[end:]

def patch_home_service(text):
    pattern = r'(?m)^    invoke-virtual \{v0, v12\}, Lcom/boop/shieldhome/ShieldHomeOverrideService;->performGlobalAction\(I\)Z\n\s*move-result v1'
    if len(re.findall(pattern, text)) != 1:
        raise ValueError('Unexpected Home double-tap dispatch')
    # Existing fallback branch already routes ACTION_RECENTS through the executor.
    return re.sub(pattern, '    const/4 v1, 0x0', text)

def patch_manifest(data):
    """Change only the binary XML version integer and same-length version name."""
    result = bytearray(data)
    strings = []
    positions = []
    offset = 8
    changed_code = changed_name = False
    while offset < len(data):
        kind, header, size = struct.unpack_from('<HHI', data, offset)
        if size < header or size == 0 or offset + size > len(data):
            raise ValueError('Invalid binary manifest chunk')
        if kind == 1:
            count = struct.unpack_from('<I', data, offset + 8)[0]
            flags, start = struct.unpack_from('<II', data, offset + 16)
            utf8 = bool(flags & 0x100)
            for index in range(count):
                entry = offset + start + struct.unpack_from('<I', data, offset + header + 4 * index)[0]
                if utf8:
                    def length(at):
                        first = data[at]
                        return (((first & 0x7f) << 8) | data[at + 1], at + 2) if first & 0x80 else (first, at + 1)
                    _, entry = length(entry)
                    length_bytes, entry = length(entry)
                    strings.append(data[entry:entry + length_bytes].decode('utf-8'))
                    positions.append((entry, length_bytes, 'utf-8'))
                else:
                    length_chars = struct.unpack_from('<H', data, entry)[0]
                    entry += 2
                    if length_chars & 0x8000:
                        length_chars = ((length_chars & 0x7fff) << 16) | struct.unpack_from('<H', data, entry)[0]
                        entry += 2
                    length_bytes = length_chars * 2
                    strings.append(data[entry:entry + length_bytes].decode('utf-16le'))
                    positions.append((entry, length_bytes, 'utf-16le'))
        elif kind == 0x102:
            node = offset + 16
            tag = struct.unpack_from('<I', data, node + 4)[0]
            if strings[tag] == 'manifest':
                start, stride, count = struct.unpack_from('<HHH', data, node + 8)
                for index in range(count):
                    attr = node + start + index * stride
                    name = strings[struct.unpack_from('<I', data, attr + 4)[0]]
                    value = struct.unpack_from('<I', data, attr + 16)[0]
                    if name == 'versionCode':
                        if value != 417:
                            raise ValueError('Baseline APK version is not 417')
                        struct.pack_into('<I', result, attr + 16, 418)
                        changed_code = True
                    elif name == 'versionName':
                        if strings[value] != '1.2.417-shield':
                            raise ValueError('Unexpected baseline version name')
                        at, length_bytes, encoding = positions[value]
                        replacement = '1.2.418-shield'.encode(encoding)
                        if len(replacement) != length_bytes:
                            raise ValueError('Version name length changed')
                        result[at:at + length_bytes] = replacement
                        changed_name = True
        offset += size
    if not (changed_code and changed_name):
        raise ValueError('Could not update both manifest version fields')
    return bytes(result)


def signature(name):
    return name.startswith('META-INF/') and (name.upper().endswith(('.SF', '.RSA', '.DSA', '.EC'))
                                            or name.upper() == 'META-INF/MANIFEST.MF')


def run(*args):
    subprocess.run([str(arg) for arg in args], check=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--tool', type=Path, required=True)
    parser.add_argument('--output', type=Path)
    parser.add_argument('--sdk', type=Path, required=True)
    args = parser.parse_args()
    snap = Path(__file__).resolve().parent
    base = snap.parent / 'shield-v417'
    apk = base / 'BOOP-Shield-v417.apk'
    output = args.output or snap / 'BOOP-Shield-v418.apk'
    if digest(apk.read_bytes()) != BASE_SHA or digest(args.tool.read_bytes()) != TOOL_SHA:
        raise ValueError('Baseline or tool checksum mismatch')
    java = Path('shield-home-lib/src/main/java/com/boop/shieldhome/BoopKeyActionExecutor.java')
    before_source = (base / java).read_text(encoding='utf-8')
    after_source = (snap / java).read_text(encoding='utf-8')
    start = before_source.index('                        if (context instanceof android.accessibilityservice.AccessibilityService) {')
    end = before_source.index('                        Log.i(TAG, "Dispatched: Recents / Task Switcher");', start)
    if after_source != before_source[:start] + '                        RecentsPreload.open(context);\n' + before_source[end:]:
        raise ValueError('Unrelated dispatcher source change')
    android = args.sdk / 'platforms/android-36/android.jar'
    d8 = args.sdk / 'build-tools/36.0.0/lib/d8.jar'
    if digest(android.read_bytes()) != 'd9eb9da824d9e247a352f570f01e1169e725b2954bca9e283a71786c59b59f9a':
        raise ValueError('Android SDK API checksum mismatch')
    if digest(d8.read_bytes()) != '4097ff9c46c185c6e7214da7fe9b1befb5adeea5cc9ca349270e0249904f9240':
        raise ValueError('D8 checksum mismatch')
    home_java = Path('shield-home-lib/src/main/java/com/boop/shieldhome/ShieldHomeOverrideService.java')
    old_home = (base / home_java).read_text(encoding='utf-8')
    old_branch = '''                            if (!performGlobalAction(GLOBAL_ACTION_RECENTS)) {
                                BoopKeyActionExecutor.execute(this, BoopKeyMappingStore.ACTION_RECENTS, null);
                            }'''
    new_branch = '                            BoopKeyActionExecutor.execute(this, BoopKeyMappingStore.ACTION_RECENTS, null);'
    if old_home.count(old_branch) != 1 or (snap / home_java).read_text(encoding='utf-8') != old_home.replace(old_branch, new_branch):
        raise ValueError('Unrelated Home service source change')
    for file in base.rglob('*.java'):
        relative = file.relative_to(base)
        if relative not in (java, home_java) and file.read_bytes() != (snap / relative).read_bytes():
            raise ValueError('Unrelated Java source changed: ' + str(relative))
    with tempfile.TemporaryDirectory(prefix='boop-display-') as temporary:
        work = Path(temporary)
        run('javac', '-cp', args.tool, '-d', work, snap / 'DexBridge.java')
        cp = str(work) + os.pathsep + str(args.tool)
        with zipfile.ZipFile(apk) as z:
            (work / 'before.dex').write_bytes(z.read('classes3.dex'))
            manifest = patch_manifest(z.read('AndroidManifest.xml'))
        run('java', '-cp', cp, 'DexBridge', 'decode', work / 'before.dex', work / 'smali')
        file = work / 'smali' / CLASS
        old = file.read_text(encoding='utf-8')
        file.write_text(patch_display(old), encoding='utf-8')
        home_smali = work / 'smali/com/boop/shieldhome/ShieldHomeOverrideService.smali'
        home_smali.write_text(patch_home_service(home_smali.read_text(encoding='utf-8')), encoding='utf-8')
        classes = work / 'helper-classes'
        classes.mkdir()
        helper_java = snap / 'shield-home-lib/src/main/java/com/boop/shieldhome/RecentsPreload.java'
        adb_java = snap / 'shield-home-lib/src/main/java/com/boop/shieldturbo/power/AdbWire.java'
        run('javac', '-source', '8', '-target', '8', '-classpath', android, '-d', classes, adb_java, helper_java)
        with zipfile.ZipFile(work / 'adb-api.jar', 'w') as z:
            for path in (classes / 'com/boop/shieldturbo').rglob('*.class'):
                z.write(path, path.relative_to(classes).as_posix())
        helper_classes = sorted((classes / 'com/boop/shieldhome').glob('RecentsPreload*.class'))
        if len(helper_classes) != 3:
            raise ValueError('Unexpected helper class inventory')
        (work / 'helper-dex').mkdir()
        run('java', '-cp', d8, 'com.android.tools.r8.D8', '--lib', android, '--classpath', work / 'adb-api.jar', '--min-api', '29', '--output', work / 'helper-dex', *helper_classes)
        run('java', '-cp', cp, 'DexBridge', 'decode', work / 'helper-dex/classes.dex', work / 'helper-smali')
        added = []
        for path in (work / 'helper-smali').rglob('*.smali'):
            relative = path.relative_to(work / 'helper-smali')
            if not relative.as_posix().startswith('com/boop/shieldhome/RecentsPreload') or (work / 'smali' / relative).exists():
                raise ValueError('Unexpected or colliding helper class')
            added.append(relative.as_posix())
            shutil.copyfile(path, work / 'smali' / relative)
        if len(added) != 3:
            raise ValueError('Unexpected added DEX inventory')
        run('java', '-cp', cp, 'DexBridge', 'compile', work / 'smali', work / 'after.dex')
        run('java', '-cp', cp, 'DexBridge', 'decode', work / 'after.dex', work / 'verified')
        originals = {p.relative_to(work / 'smali') for p in (work / 'smali').rglob('*.smali')}
        verified = {p.relative_to(work / 'verified') for p in (work / 'verified').rglob('*.smali')}
        if originals != verified:
            raise ValueError('DEX class inventory changed')
        def normalize(t):
            t = re.sub(r'(?m)^(\.field [^\n]+:[ZBSCIJFD]) = (?:0x0|0x0L|0\.0|0\.0f)$', r'\1', t)
            return '\n'.join(line for line in t.splitlines() if line.strip())
        def canonical_changed_method(t, method):
            def canonical(match):
                body = match[0]
                if (' ' + method + '(') not in body.splitlines()[0]:
                    return body
                labels = re.findall(r'(?m)^\s*(:[\w$]+)\s*$', body)
                names = {label: ':audit_' + str(i) for i, label in enumerate(labels)}
                body = re.sub(r':[\w$]+', lambda m: names.get(m[0], m[0]), body)
                # Instruction offsets change after an isolated case replacement.
                # Debug line coalescing and alignment NOPs have no behavior.
                return '\n'.join(line for line in body.splitlines()
                                 if not re.fullmatch(r'\s*(?:\.line \d+|nop)\s*', line))
            return re.sub(r'(?ms)^\.method [^\n]+\n.*?^\.end method', canonical, t)
        for relative in originals:
            expected = (work / 'smali' / relative).read_text(encoding='utf-8')
            actual = (work / 'verified' / relative).read_text(encoding='utf-8')
            if relative.as_posix() == CLASS:
                expected = canonical_changed_method(expected, 'lambda$execute$0')
                actual = canonical_changed_method(actual, 'lambda$execute$0')
            elif relative.as_posix() == 'com/boop/shieldhome/ShieldHomeOverrideService.smali':
                expected = canonical_changed_method(expected, 'onKeyEvent')
                actual = canonical_changed_method(actual, 'onKeyEvent')
            if normalize(expected) != normalize(actual):
                print('\n'.join(list(difflib.unified_diff(normalize(expected).splitlines(), normalize(actual).splitlines()))[:70]))
                raise ValueError('DEX round-trip mismatch: ' + str(relative))
        shipped = (work / 'verified' / CLASS).read_text(encoding='utf-8')
        if re.search(r'wm (?:size|density)|display_(?:size|density)_forced', shipped):
            raise ValueError('Global display write in final DEX')
        # Exact transformation is checked against expected text for all classes.
        # Only the Recents switch case changes in existing bytecode; helper classes are compiled from audited Java.
        dex = (work / 'after.dex').read_bytes()
        with zipfile.ZipFile(apk) as z, zipfile.ZipFile(output, 'w') as out:
            for info in z.infolist():
                if not signature(info.filename):
                    data = manifest if info.filename == 'AndroidManifest.xml' else dex if info.filename == 'classes3.dex' else z.read(info.filename)
                    out.writestr(copy.copy(info), data)
        with zipfile.ZipFile(apk) as z, zipfile.ZipFile(output) as out:
            changed = sorted(n for n in out.namelist() if out.read(n) != z.read(n))
            if changed != ['AndroidManifest.xml', 'classes3.dex']:
                raise ValueError('Unexpected APK changes: ' + str(changed))
        receipt = dict(version=418, baseline_sha256=BASE_SHA, unsigned_sha256=digest(output.read_bytes()), changed_entries=changed, verified_classes=len(originals), native_recents_preload=True, added_classes=added, helper_source_sha256=digest(helper_java.read_bytes()), signed=False)
        output.with_suffix('.build.json').write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
        (snap / 'recents-verified.smali').write_text(shipped, encoding='utf-8')
        (snap / 'home-recents-verified.smali').write_text((work / 'verified/com/boop/shieldhome/ShieldHomeOverrideService.smali').read_text(encoding='utf-8'), encoding='utf-8')
        print(json.dumps(receipt, indent=2))

if __name__ == '__main__':
    main()
