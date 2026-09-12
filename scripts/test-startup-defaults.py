"""Nonvisual pure-Java defaults behavior and recovery tests. No device access."""
from pathlib import Path
import os, shutil, subprocess, tempfile
root=Path(__file__).resolve().parents[1]
src=root/'unified/shield-home/src/main/java/com/boop/shieldhome'
test=root/'unified/shield-home/src/test/java/com/boop/shieldhome'
names=['StartupPackageState','StartupRecoveryPolicy','StartupRestoreRecord','StartupRestoreStore','StartupPreventionRecord','StartupPreventionPolicy','StartupCleanupPolicy','StartupPackageController','StartupManagerUiModel','StartupDefaultsProfile','StartupDefaultsJournal','StartupDefaultsCoordinator','StartupDefaultsSafety']
files=[str(src/(n+'.java')) for n in names if (src/(n+'.java')).exists()]
files += [str(p) for p in sorted(test.glob('StartupDefaults*Test.java'))]
def tool(name):
    return shutil.which(name) or str(Path(os.environ['JAVA_HOME'])/'bin'/(name+('.exe' if os.name=='nt' else '')))
with tempfile.TemporaryDirectory() as out:
    subprocess.run([tool('javac'),'-encoding','UTF-8','-d',out,*files],check=True)
    for path in sorted(test.glob('StartupDefaults*Test.java')):
        subprocess.run([tool('java'),'-cp',out,'com.boop.shieldhome.'+path.stem],check=True)
print('BOOP defaults behavior suites passed')
