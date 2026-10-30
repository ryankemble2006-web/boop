"""Build the v410 launcher method patch; signing remains in GitHub Actions."""
import argparse
import copy
import hashlib
import json
import os
from pathlib import Path
import re
import struct
import subprocess
import tempfile
import urllib.request
import zipfile

TOOL_URL = 'https://github.com/iBotPeaches/Apktool/releases/download/v2.12.1/apktool_2.12.1.jar'
TOOL_SHA = '66cf4524a4a45a7f56567d08b2c9b6ec237bcdd78cee69fd4a59c8a0243aeafa'
BASE_SHA = '0a9bbc4d1bb7a94b172f12c88092aa7a15c09714a9b46ca90b647d06257ff8f7'
CLASS = 'com/boop/shieldhome/ShieldLauncherActivity'
METHOD = r'(?ms)^\.method public onConfigurationChanged\(Landroid/content/res/Configuration;\)V\n.*?^\.end method'


def digest(data):
    return hashlib.sha256(data).hexdigest()


def patch_method(text):
    matches = list(re.finditer(METHOD, text))
    if len(matches) != 1:
        raise ValueError('Expected one launcher configuration method')
    match = matches[0]
    original = match.group()
    if '->reapplyLayoutDimensions()V' not in original or '->requestLayout()V' not in original:
        raise ValueError('Unexpected baseline configuration method')
    marker = 'invoke-direct {p0}, L' + CLASS + ';->restoreOpaqueWindow()V'
    if original.count(marker) != 1:
        raise ValueError('Unexpected launcher method prefix')
    prefix = original[:original.index(marker) + len(marker)]
    tail = '''

    iget-object v0, p0, Lcom/boop/shieldhome/ShieldLauncherActivity;->root:Landroid/widget/FrameLayout;
    if-eqz v0, :native4k_done
    iget-object v0, p0, Lcom/boop/shieldhome/ShieldLauncherActivity;->store:Lcom/boop/shieldhome/ShieldHomeStore;
    if-eqz v0, :native4k_done
    iget-object v0, p0, Lcom/boop/shieldhome/ShieldLauncherActivity;->currentView:Landroid/view/View;
    if-eqz v0, :native4k_done
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldLauncherActivity;->rememberHomeFocus()V
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldLauncherActivity;->rememberSettingsFocus()V
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldLauncherActivity;->showCurrentPage()V
    invoke-direct {p0}, Lcom/boop/shieldhome/ShieldLauncherActivity;->restoreHomeFocus()V
    :native4k_done
    return-void
.end method'''
    return text[:match.start()] + prefix + tail + text[match.end():]


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
                        if value != 408:
                            raise ValueError('Baseline APK version is not 408')
                        struct.pack_into('<I', result, attr + 16, 410)
                        changed_code = True
                    elif name == 'versionName':
                        if strings[value] != '1.2.408-shield':
                            raise ValueError('Unexpected baseline version name')
                        at, length_bytes, encoding = positions[value]
                        replacement = '1.2.410-shield'.encode(encoding)
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
    parser.add_argument('--output', type=Path)
    parser.add_argument('--tool', type=Path)
    args = parser.parse_args()
    snapshot = Path(__file__).resolve().parent
    baseline = snapshot.parent / 'shield-v408'
    apk = baseline / 'BOOP-Shield-v408.apk'
    if digest(apk.read_bytes()) != BASE_SHA:
        raise ValueError('Baseline APK changed')
    java_path = Path('shield-home-lib/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java')
    before = (baseline / java_path).read_text(encoding='utf-8')
    after = (snapshot / java_path).read_text(encoding='utf-8')
    old = '''        if (currentView instanceof ShieldHomeView) {
            ((ShieldHomeView) currentView).reapplyLayoutDimensions();
        } else if (currentView != null) {
            currentView.requestLayout();
        }
    }

    @Override protected void onResume()'''
    new = '''        // Text sizes, artwork dimensions and custom drawing caches are set in
        // view constructors. A layout pass alone retains their old pixel sizes.
        if (root != null && store != null && currentView != null) {
            rememberHomeFocus();
            rememberSettingsFocus();
            showCurrentPage();
            restoreHomeFocus();
        }
    }

    @Override protected void onResume()'''
    if before.count(old) != 1 or after != before.replace(old, new, 1):
        raise ValueError('Java source does not match the compiled method patch')
    for source in baseline.rglob('*.java'):
        relative = source.relative_to(baseline)
        if relative != java_path and source.read_bytes() != (snapshot / relative).read_bytes():
            raise ValueError('Unrelated Java source changed: ' + str(relative))
    output = args.output or snapshot / 'BOOP-Shield-v410.apk'
    with tempfile.TemporaryDirectory(prefix='boop-native4k-') as temporary:
        work = Path(temporary)
        tool = args.tool or work / 'apktool.jar'
        if not tool.exists():
            with urllib.request.urlopen(TOOL_URL, timeout=120) as response:
                tool.write_bytes(response.read())
        if digest(tool.read_bytes()) != TOOL_SHA:
            raise ValueError('Build tool checksum mismatch')
        run('javac', '-cp', tool, '-d', work, snapshot / 'DexBridge.java')
        classpath = str(work) + os.pathsep + str(tool)
        with zipfile.ZipFile(apk) as archive:
            (work / 'before.dex').write_bytes(archive.read('classes3.dex'))
            manifest = patch_manifest(archive.read('AndroidManifest.xml'))
        run('java', '-cp', classpath, 'DexBridge', 'decode', work / 'before.dex', work / 'smali')
        smali = work / 'smali' / (CLASS + '.smali')
        text = smali.read_text(encoding='utf-8')
        smali.write_text(patch_method(text), encoding='utf-8')
        run('java', '-cp', classpath, 'DexBridge', 'compile', work / 'smali', work / 'after.dex')
        run('java', '-cp', classpath, 'DexBridge', 'decode', work / 'after.dex', work / 'verified-smali')
        original_smali = {file.relative_to(work / 'smali') for file in (work / 'smali').rglob('*.smali')}
        rebuilt_smali = {file.relative_to(work / 'verified-smali') for file in (work / 'verified-smali').rglob('*.smali')}
        if original_smali != rebuilt_smali:
            raise ValueError('Compiled DEX class inventory changed')
        for relative in original_smali:
            before_text = (work / 'smali' / relative).read_text(encoding='utf-8')
            after_text = (work / 'verified-smali' / relative).read_text(encoding='utf-8')
            if relative.as_posix() == CLASS + '.smali':
                before_text = re.sub(METHOD, '', before_text)
                after_text = re.sub(METHOD, '', after_text)
            # DEX assemblers omit explicit false static defaults; VM value is identical.
            before_text = before_text.replace('.field public static volatile isBound:Z = false\n', '.field public static volatile isBound:Z\n')
            if before_text != after_text:
                raise ValueError('Unrelated compiled class or method changed: ' + str(relative))
        dex = (work / 'after.dex').read_bytes()
        if dex == (work / 'before.dex').read_bytes():
            raise ValueError('No launcher patch was compiled')
        with zipfile.ZipFile(apk) as original, zipfile.ZipFile(output, 'w') as patched:
            for info in original.infolist():
                if signature(info.filename):
                    continue
                data = manifest if info.filename == 'AndroidManifest.xml' else dex if info.filename == 'classes3.dex' else original.read(info.filename)
                patched.writestr(copy.copy(info), data)
        with zipfile.ZipFile(apk) as original, zipfile.ZipFile(output) as patched:
            changed = [name for name in patched.namelist() if patched.read(name) != original.read(name)]
            if sorted(changed) != ['AndroidManifest.xml', 'classes3.dex']:
                raise ValueError('Unexpected changed APK entries: ' + str(changed))
        receipt = {'version': 410, 'baseline_sha256': BASE_SHA, 'unsigned_sha256': digest(output.read_bytes()),
                   'changed_entries': changed, 'tool_sha256': TOOL_SHA, 'verified_classes': len(original_smali),
                   'other_methods_unchanged': True, 'signed': False}
        output.with_suffix('.build.json').write_text(json.dumps(receipt, indent=2) + '\n', encoding='utf-8')
        print(json.dumps(receipt, indent=2))


if __name__ == '__main__':
    main()
