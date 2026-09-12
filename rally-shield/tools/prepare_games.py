#!/usr/bin/env python3
"""Build private DOS bundles from existing game folders. Never upload the output."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import zipfile

COMMON = '[dosbox]\nmachine=svga_s3\nmemsize=16\n[cpu]\ncore=auto\n'
CONFIGS = {
    'rac93': COMMON + 'cycles=12000\n[dos]\nems=true\n[sblaster]\nsbtype=sbpro2\nsbbase=220\nirq=7\ndma=1\n[joystick]\njoysticktype=none\n[mixer]\nrate=48000\n',
    'rac96': COMMON + 'cycles=max\n[dos]\nems=true\n[sblaster]\nsbtype=sb16\nsbbase=220\nirq=7\ndma=1\nhdma=5\n[joystick]\njoysticktype=none\n[mixer]\nrate=48000\n',
}
EXECUTABLES = {'rac93': 'RALLY.EXE', 'rac96': 'RAL.EXE'}
RESERVED = {'DOSBOX.CONF', 'DOSBOX.BAT'}

def snapshot(source: Path) -> dict:
    return {p.relative_to(source).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
            for p in sorted(source.rglob('*')) if p.is_file() and not p.is_symlink()}

def make_bundle(source: Path, output: Path, game: str) -> dict:
    source, output = Path(source).resolve(), Path(output).resolve()
    if game not in CONFIGS:
        raise ValueError('Unknown game')
    if not source.is_dir():
        raise ValueError('Game folder does not exist')
    if output == source or source in output.parents:
        raise ValueError('Bundle output must be outside the original game folder')
    files, names = [], set()
    for path in sorted(source.rglob('*'), key=lambda p: p.relative_to(source).as_posix().upper()):
        if path.is_symlink():
            raise ValueError('Symbolic links are not permitted in a game bundle')
        if not path.is_file():
            continue
        name = path.relative_to(source).as_posix()
        if any(part in ('..', '.') for part in name.split('/')) or ':' in name or '\\' in name:
            raise ValueError('Unsafe game path')
        upper = name.upper()
        if upper in names:
            raise ValueError('Case-insensitive duplicate game path: ' + name)
        names.add(upper)
        if upper not in RESERVED:
            files.append((path, name))
    if EXECUTABLES[game] not in names:
        raise ValueError('Required executable is not in the root: ' + EXECUTABLES[game])
    if len(files) > 10000 or sum(p.stat().st_size for p, _ in files) > 1024**3:
        raise ValueError('Game bundle is larger than the supported limit')
    original = snapshot(source)
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_name(output.name + '.part')
    if temporary.exists():
        raise ValueError('Temporary output exists; refusing to overwrite it')
    def put(z, name, content):
        entry = zipfile.ZipInfo(name, (2000, 1, 1, 0, 0, 0))
        entry.compress_type = zipfile.ZIP_DEFLATED
        entry.create_system = 3
        entry.external_attr = 0o100644 << 16
        z.writestr(entry, content, compresslevel=6)
    try:
        with zipfile.ZipFile(temporary, 'w') as z:
            for path, name in files:
                put(z, name, path.read_bytes())
            put(z, 'DOSBOX.CONF', CONFIGS[game].replace('\n', '\r\n').encode('ascii'))
            boot = '@echo off\r\nc:\r\n' + EXECUTABLES[game].lower() + '\r\nexit\r\n'
            put(z, 'DOSBOX.BAT', boot.encode('ascii'))
        if snapshot(source) != original:
            raise ValueError('Original game files changed during preparation; bundle was not accepted')
        os.replace(temporary, output)
    finally:
        temporary.unlink(missing_ok=True)
    return {'game': game, 'files': len(files), 'bytes': output.stat().st_size,
            'sha256': hashlib.sha256(output.read_bytes()).hexdigest(), 'originals_unchanged': True}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--rac93', type=Path, required=True)
    parser.add_argument('--rac96', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    receipts = [make_bundle(args.rac93, args.output / 'rac93.zip', 'rac93'),
                make_bundle(args.rac96, args.output / 'rac96.zip', 'rac96')]
    text = json.dumps(receipts, indent=2)
    (args.output / 'bundle-receipt.json').write_text(text + '\n', encoding='utf-8')
    print(text)

if __name__ == '__main__':
    main()
