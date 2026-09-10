"""Build a clean install ZIP and source bundle, then verify all members."""
import ast
import hashlib
import shutil
import xml.etree.ElementTree as ET
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED

ROOT=Path(__file__).resolve().parent
ADDON=ROOT/'script.boop.blockparty'
OUT=ROOT.parents[1]/'outputs/BOOP-Block-Party'
OUT.mkdir(parents=True,exist_ok=True)

def include(path):
    return path.is_file() and '__pycache__' not in path.parts and path.suffix != '.pyc'

for path in ADDON.rglob('*.py'):
    ast.parse(path.read_text(encoding='utf-8'),feature_version=(3,8))
for path in ADDON.rglob('*.xml'):
    ET.parse(path)
assert ET.parse(ADDON/'addon.xml').getroot().attrib['id']=='script.boop.blockparty'
assert hashlib.sha256((ADDON/'resources/media/boopApprovedEyes.png').read_bytes()).hexdigest()=='ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22'

archive=OUT/'script.boop.blockparty-1.0.0.zip'
with ZipFile(archive,'w',ZIP_DEFLATED) as z:
    for path in sorted(ADDON.rglob('*')):
        if include(path):
            z.write(path,path.relative_to(ROOT).as_posix())
with ZipFile(archive) as z:
    assert z.testzip() is None
    for name in z.namelist():
        assert name.startswith('script.boop.blockparty/') and '..' not in Path(name).parts
        assert z.read(name)==(ROOT/name).read_bytes()
    print('Install ZIP: {} members; every member byte-verified'.format(len(z.namelist())))

with ZipFile(OUT/'BOOP-Block-Party-source.zip','w',ZIP_DEFLATED) as z:
    for path in sorted(ROOT.rglob('*')):
        if include(path) and path.suffix in ('.py','.md','.png','.wav','.xml','.txt') and path.name not in ('test-pid.txt','stage_release.py'):
            z.write(path,'BOOP-Block-Party/'+path.relative_to(ROOT).as_posix())
for name in ('README.md','VERIFICATION.md'):
    if (ROOT/name).exists():
        shutil.copyfile(ROOT/name,OUT/name)
hashes=[]
for path in sorted(OUT.glob('*.zip')):
    digest=hashlib.sha256(path.read_bytes()).hexdigest()
    hashes.append(digest+'  '+path.name)
    print(path.name, path.stat().st_size, 'bytes',digest)
(OUT/'SHA256SUMS.txt').write_text('\n'.join(hashes)+'\n',encoding='utf-8')
