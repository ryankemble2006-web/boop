import hashlib
import re
import sys
import zipfile
from pathlib import Path

def signature(n):
    return n.startswith('META-INF/') and (n.upper().endswith(('.SF', '.RSA', '.DSA', '.EC')) or n.upper() == 'META-INF/MANIFEST.MF')

unsigned = Path(sys.argv[1])
with zipfile.ZipFile(unsigned) as z:
    dex = [n for n in z.namelist() if re.fullmatch(r'classes\d*\.dex', n)]
    for name in dex:
        for needle in (b'wm size', b'wm density', b'display_size_forced', b'display_density_forced'):
            assert needle not in z.read(name), (name, needle)
    print(f'PASS: no global display command strings in {len(dex)} DEX files')
    if len(sys.argv) > 2:
        with zipfile.ZipFile(sys.argv[2]) as signed:
            a = {n for n in z.namelist() if not signature(n) and not n.endswith('/')}
            b = {n for n in signed.namelist() if not signature(n) and not n.endswith('/')}
            assert a == b, (a ^ b)
            assert all(z.read(n) == signed.read(n) for n in a)
            print(f'PASS: all {len(a)} signed APK payload entries match audited local build')
print('SHA256', hashlib.sha256(unsigned.read_bytes()).hexdigest())
