"""Minimal v416 display-isolation patch. GitHub supplies permanent signing."""
import argparse
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

BASE_SHA = '1a9c06292b404ac2af10b4d601d5a855d634773d138c7bfb0a5b77e3bb191ec5'
TOOL_SHA = '66cf4524a4a45a7f56567d08b2c9b6ec237bcdd78cee69fd4a59c8a0243aeafa'
CLASS = 'com/boop/shieldhome/ShieldSelfGrant.smali'
GLOBAL_GRANT = 'wm size 3839x2160 >/dev/null 2>&1; wm density 640 >/dev/null 2>&1; '

def digest(data):
    return hashlib.sha256(data).hexdigest()

def patch_display(text):
    for name in ('ensureNative4kDisplayAsync', 'lambda$ensureNative4kDisplayAsync$1'):
        pattern = r'(?ms)^(\.method [^\n]* ' + re.escape(name) + r'\(Landroid/content/Context;\)V)\n.*?^\.end method'
        matches = list(re.finditer(pattern, text))
        if len(matches) != 1:
            raise ValueError('Unexpected display method: ' + name)
        text = re.sub(pattern, lambda m: m[1] + '\n    .locals 0\n    return-void\n.end method', text)
    if text.count(GLOBAL_GRANT) != 1:
        raise ValueError('Unexpected permission recovery command')
    text = text.replace(GLOBAL_GRANT, '')
    if re.search(r'wm (?:size|density)|display_(?:size|density)_forced', text):
        raise ValueError('Global display writer remains')
    return text

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
                        if value != 415:
                            raise ValueError('Baseline APK version is not 415')
                        struct.pack_into('<I', result, attr + 16, 416)
                        changed_code = True
                    elif name == 'versionName':
                        if strings[value] != '1.2.415-shield':
                            raise ValueError('Unexpected baseline version name')
                        at, length_bytes, encoding = positions[value]
                        replacement = '1.2.416-shield'.encode(encoding)
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
    args = parser.parse_args()
    snap = Path(__file__).resolve().parent
    base = snap.parent / 'shield-v415'
    apk = base / 'BOOP-Shield-v415.apk'
    output = args.output or snap / 'BOOP-Shield-v416.apk'
    if digest(apk.read_bytes()) != BASE_SHA or digest(args.tool.read_bytes()) != TOOL_SHA:
        raise ValueError('Baseline or tool checksum mismatch')
    java = Path('shield-home-lib/src/main/java/com/boop/shieldhome/ShieldSelfGrant.java')
    before_source = (base / java).read_text(encoding='utf-8')
    after_source = (snap / java).read_text(encoding='utf-8')
    start = before_source.index('    /**\n     * Self-heals Native 4K')
    end = before_source.index('    public static boolean isSofabatonConnected', start)
    replacement = after_source[start:after_source.index('    public static boolean isSofabatonConnected', start)]
    if 'AdbWire' in replacement or 'EXECUTOR' in replacement or 'wm ' in replacement:
        raise ValueError('Source display hook still performs work')
    expected_source = before_source[:start] + replacement + before_source[end:]
    expected_source = expected_source.replace('                        + "' + GLOBAL_GRANT + '"\n', '')
    if after_source != expected_source:
        raise ValueError('Unrelated source change')
    for file in base.rglob('*.java'):
        relative = file.relative_to(base)
        if relative != java and file.read_bytes() != (snap / relative).read_bytes():
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
        run('java', '-cp', cp, 'DexBridge', 'compile', work / 'smali', work / 'after.dex')
        run('java', '-cp', cp, 'DexBridge', 'decode', work / 'after.dex', work / 'verified')
        originals = {p.relative_to(work / 'smali') for p in (work / 'smali').rglob('*.smali')}
        verified = {p.relative_to(work / 'verified') for p in (work / 'verified').rglob('*.smali')}
        if originals != verified:
            raise ValueError('DEX class inventory changed')
        def normalize(t):
            t = re.sub(r'(?m)^(\.field [^\n]+:[ZBSCIJFD]) = (?:0x0|0x0L|0\.0|0\.0f)$', r'\1', t)
            return '\n'.join(line for line in t.splitlines() if line.strip())
        for relative in originals:
            if normalize((work / 'smali' / relative).read_text(encoding='utf-8')) != normalize((work / 'verified' / relative).read_text(encoding='utf-8')):
                raise ValueError('DEX round-trip mismatch: ' + str(relative))
        shipped = (work / 'verified' / CLASS).read_text(encoding='utf-8')
        if re.search(r'wm (?:size|density)|display_(?:size|density)_forced', shipped):
            raise ValueError('Global display write in final DEX')
        # Exact transformation is checked against expected text for all classes.
        # patch_display only replaces two complete method bodies and one literal.
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
        receipt = dict(version=416, baseline_sha256=BASE_SHA, unsigned_sha256=digest(output.read_bytes()), changed_entries=changed, verified_classes=len(originals), global_display_writes=False, signed=False)
        output.with_suffix('.build.json').write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
        (snap / 'display-verified.smali').write_text(shipped, encoding='utf-8')
        print(json.dumps(receipt, indent=2))

if __name__ == '__main__':
    main()
