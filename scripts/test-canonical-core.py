from pathlib import Path
import os, shutil, subprocess, tempfile

root = Path(__file__).resolve().parents[1]
java_home = os.environ.get('JAVA_HOME', '')
javac = shutil.which('javac') or str(Path(java_home) / 'bin/javac')
java = shutil.which('java') or str(Path(java_home) / 'bin/java')
sources = list((root / 'unified/shared').glob('*.java'))
with tempfile.TemporaryDirectory() as output:
    subprocess.run([javac, '-d', output, *map(str, sources), str(root/'tests/canonical/SharedStateCheck.java')], check=True)
    subprocess.run([java, '-cp', output, 'SharedStateCheck'], check=True)
    subprocess.run([javac, '-d', output,
        str(root/'unified/shield-home/src/main/java/com/boop/shieldhome/HomeOverridePolicy.java'),
        str(root/'tests/canonical/HomeOverrideCheck.java')], check=True)
    subprocess.run([java, '-cp', output, 'com.boop.shieldhome.HomeOverrideCheck'], check=True)
