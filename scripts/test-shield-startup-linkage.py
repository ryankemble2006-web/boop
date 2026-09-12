"""Reject Startup Manager bytecode that cannot link on Shield Android 11."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
java_home = Path(os.environ.get('JAVA_HOME', ''))

def tool(name):
    return shutil.which(name) or str(java_home / 'bin' / name)

sources = [
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupPreventionRecord.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupCleanupPolicy.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageState.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupRecoveryPolicy.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageController.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageCommands.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupPackageInventory.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupRestoreRecord.java',
    root/'unified/shield-home/src/main/java/com/boop/shieldhome/StartupRestoreStore.java',
]
with tempfile.TemporaryDirectory() as folder:
    subprocess.run([tool('javac'), '-d', folder, *map(str, sources)], check=True)
    classes = (
        'StartupCleanupPolicy',
        'StartupPackageCommands',
        'StartupPackageInventory',
    )
    forbidden = 'java/lang/String.lines:()Ljava/util/stream/Stream;'
    for name in classes:
        bytecode = subprocess.check_output([
            tool('javap'), '-p', '-c', '-classpath', folder,
            'com.boop.shieldhome.' + name,
        ], text=True)
        assert forbidden not in bytecode and 'java/util/stream/Stream.toList:' not in bytecode, (
            name + ' links java.lang.String.lines(), unavailable on Shield Android 11'
        )

print('Shield Startup Manager Android 11 linkage check passed')
