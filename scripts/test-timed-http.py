"""Exercise the production scheduling HTTP client with a local server (Java 17+)."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import urllib.request

root = Path(__file__).resolve().parents[1]
java_home = os.environ.get('JAVA_HOME')
suffix = '.exe' if os.name == 'nt' else ''
javac = str(Path(java_home) / 'bin' / ('javac' + suffix)) if java_home else shutil.which('javac')
java = str(Path(java_home) / 'bin' / ('java' + suffix)) if java_home else shutil.which('java')
if not javac or not java:
    raise SystemExit('Java 17+ is required')

with tempfile.TemporaryDirectory(prefix='boop-timed-http-') as tmp:
    tmp = Path(tmp)
    jar = Path(os.environ['BOOP_TEST_JSON_JAR']) if os.environ.get('BOOP_TEST_JSON_JAR') else tmp / 'json.jar'
    if not jar.exists():
        urllib.request.urlretrieve('https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar', jar)
    classes = tmp / 'classes'
    classes.mkdir()
    sources = [root / 'source' / (name + '.java') for name in (
        'HomeAssistantClient', 'HomeAssistantConversationRequest', 'HomeAssistantResponseParser',
        'HomeAssistantResponse', 'CommandOutcome', 'LocalReply', 'LightColourCommandParser')]
    sources.append(root / 'tests/timed-http/TimedResponseHttpTest.java')
    subprocess.run([javac, '-encoding', 'UTF-8', '-cp', str(jar), '-d', str(classes), *map(str, sources)], check=True)
    subprocess.run([java, '-cp', str(classes) + os.pathsep + str(jar), 'com.boop.alpha1.TimedResponseHttpTest'], check=True)
