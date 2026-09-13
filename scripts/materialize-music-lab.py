#!/usr/bin/env python3
"""Create a separate installable Music Lab from the prepared Unified source.

Only a fresh generated fork is changed. The parent source, assets and permanent
signing configuration are preserved. Run on GitHub after materialize-unified.sh.
"""
from pathlib import Path
import hashlib
import re
import shutil
import xml.etree.ElementTree as ET

SOURCE = Path('boop-build/BOOP-Alpha1')
TARGET = Path('boop-music-build/BOOP-Music-Lab')
OLD_PACKAGE = 'com.boop.alpha1'
PACKAGE = 'com.boop.musiclab'
LABEL = 'BOOP Music Lab'
ANDROID = '{http://schemas.android.com/apk/res/android}'
ET.register_namespace('android', ANDROID[1:-1])
ET.register_namespace('tools', 'http://schemas.android.com/tools')


def fingerprint(root: Path) -> dict:
    return {str(p.relative_to(root)): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in root.rglob('*') if p.is_file()}


def replace_once(text: str, old: str, new: str) -> str:
    if text.count(old) != 1:
        raise ValueError(f'Expected one fork anchor {old!r}, found {text.count(old)}')
    return text.replace(old, new, 1)


def main() -> None:
    gradle = SOURCE/'app/build.gradle'
    if not gradle.is_file() or "applicationId 'com.boop.alpha1'" not in gradle.read_text():
        raise SystemExit('Materialize the unchanged Unified parent before the Music Lab fork')
    if TARGET.exists() or TARGET.is_symlink():
        raise SystemExit('Refusing to overwrite an existing Music Lab build tree')
    before = fingerprint(SOURCE)
    shutil.copytree(SOURCE, TARGET, ignore=shutil.ignore_patterns('build', '.gradle', 'local.properties'))

    # Repackage only the owning application namespace. Library namespaces may
    # remain identical across separately sandboxed apps. In particular, retain
    # the com.boop.* startup protection and the independently built bridge DEX.
    for path in TARGET.rglob('*'):
        if not path.is_file() or path.suffix not in ('.java', '.xml', '.gradle', '.pro', '.properties'):
            continue
        if '/assets/' in path.as_posix():
            continue
        old = path.read_text(encoding='utf-8')
        text = old.replace(OLD_PACKAGE, PACKAGE)
        text = text.replace('boop://', 'boopmusiclab://')
        text = text.replace('.scheme("boop")', '.scheme("boopmusiclab")')
        # Change scheme comparisons only, never the spoken wake name BOOP.
        text = '\n'.join(line.replace('"boop"', '"boopmusiclab"') if 'getScheme()' in line else line
                         for line in text.split('\n'))
        if text != old:
            path.write_text(text, encoding='utf-8')

    for source_set in ('main', 'test', 'androidTest'):
        directory = TARGET/f'app/src/{source_set}/java/com/boop/alpha1'
        if directory.is_dir():
            directory.rename(directory.with_name('musiclab'))

    gradle = TARGET/'app/build.gradle'
    text = replace_once(gradle.read_text(), 'versionCode 161', 'versionCode 1')
    text = replace_once(text, 'versionName "1.2.161-lab-scale-independent"',
                        'versionName "0.1.1-v161-audio-prompt"')
    gradle.write_text(text, encoding='utf-8')

    # Lab is an ordinary launchable app, not a competing HOME/assistant or
    # automatic boot cleanup owner. No installed device setting is modified.
    blocked_intents = {'android.intent.category.HOME', 'android.intent.action.ASSIST',
                       'android.intent.action.BOOT_COMPLETED'}
    for path in TARGET.glob('*/src/main/AndroidManifest.xml'):
        tree = ET.parse(path)
        root = tree.getroot()
        if ANDROID+'sharedUserId' in root.attrib:
            raise SystemExit('Unexpected shared user ID; refuse a non-isolated fork')
        for permission in list(root.findall('uses-permission')):
            if permission.get(ANDROID+'name') == 'android.permission.RECEIVE_BOOT_COMPLETED':
                root.remove(permission)
        for parent in list(root.iter()):
            for child in list(parent):
                if child.tag == 'intent-filter' and any(
                        node.get(ANDROID+'name') in blocked_intents for node in child):
                    parent.remove(child)
                elif child.tag == 'service' and child.get(ANDROID+'name', '').endswith('.BoopHomeOverrideService'):
                    parent.remove(child)
            if parent.get(ANDROID+'scheme') == 'boop':
                parent.set(ANDROID+'scheme', 'boopmusiclab')
            if ANDROID+'taskAffinity' in parent.attrib:
                parent.set(ANDROID+'taskAffinity', PACKAGE)
            authority = parent.get(ANDROID+'authorities')
            if authority and not authority.startswith(('com.boop.musiclab', '${applicationId}')):
                raise SystemExit(f'Unexpected shared provider authority: {path}')
        if path == TARGET/'app/src/main/AndroidManifest.xml':
            application = root.find('application')
            if application is None:
                raise SystemExit('Application manifest is missing its application')
            application.set(ANDROID+'label', LABEL)
            application.set(ANDROID+'taskAffinity', PACKAGE)
        tree.write(path, encoding='utf-8', xml_declaration=True)

    if fingerprint(SOURCE) != before:
        raise SystemExit('Parent source changed unexpectedly; do not publish this fork')
    print(f'Materialized {LABEL}: {PACKAGE}, version 1 / 0.1.1-v161-audio-prompt')
    print('Parent tree unchanged; original assets retained; no HOME, ASSIST or boot filter')


if __name__ == '__main__':
    main()
