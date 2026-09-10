"""Reject URL encoder bytecode that cannot link on Shield Android 11."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
java_home = Path(os.environ.get('JAVA_HOME', ''))
def tool(name):
    return shutil.which(name) or str(java_home / 'bin' / name)

with tempfile.TemporaryDirectory() as folder:
    subprocess.run([tool('javac'), '-d', folder,
        str(root/'source/HomeAssistantAuthUrls.java'),
        str(root/'source/HomeAssistantSavedConnection.java')], check=True)
    for name in ('HomeAssistantAuthUrls', 'HomeAssistantSavedConnection'):
        bytecode = subprocess.check_output([tool('javap'), '-p', '-c', '-classpath', folder,
            'com.boop.alpha1.' + name], text=True)
        forbidden = 'java/net/URLEncoder.encode:(Ljava/lang/String;Ljava/nio/charset/Charset;)'
        assert forbidden not in bytecode, name + ' links unsupported Android 11 encoder overload'
print('Shield URL encoding linkage check passed')
