"""Run production lookup/HTTP code against deterministic network responses."""
from pathlib import Path
import hashlib
import os
import subprocess
import tempfile
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'


def test_lookup_recovers_from_service_errors_and_preserves_recording_identity():
    jar = ROOT / 'work/lyrics-tests/json.jar'
    jar.parent.mkdir(parents=True, exist_ok=True)
    if not jar.exists():
        urllib.request.urlretrieve('https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar', jar)
    assert hashlib.sha256(jar.read_bytes()).hexdigest() == '3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed'
    stubs = {
        'android/os/Looper.java': 'package android.os; public class Looper {public static Looper getMainLooper(){return new Looper();}}',
        'android/os/Handler.java': '''package android.os; public class Handler {
            public Handler(Looper l){} public void post(Runnable r){r.run();}
            public void postDelayed(Runnable r,long delay){} public void removeCallbacks(Runnable r){}
        }''',
        'com/boop/shieldhome/NowPlayingSnapshot.java': '''package com.boop.shieldhome;
            final class NowPlayingSnapshot {
                String title(){return "Gonna Make You a Star";} String subtitle(){return "David Essex";}
                String album(){return "Best Of David Essex";} long durationMs(){return 218000L;}
            }''',
    }
    with tempfile.TemporaryDirectory(prefix='boop-lyrics-resilience-') as folder:
        target = Path(folder)
        files = []
        for name, content in stubs.items():
            path = target / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
            files.append(str(path))
        files += [str(SRC / (name + '.java')) for name in (
            'DeezerLyricsDocument', 'DeezerLyricsClient', 'DeezerLyricsHttp',
            'DeezerTimedLyricsClient', 'LrclibLyricsClient', 'LyricsRequestGate', 'NativeLyricsLoader')]
        files.append(str(ROOT / 'tests/canonical/LyricsResilienceCheck.java'))
        subprocess.run(['javac', '-encoding', 'UTF-8', '-cp', str(jar), '-d', folder, *files], check=True)
        subprocess.run(['java', '-cp', folder + os.pathsep + str(jar),
                        'com.boop.shieldhome.LyricsResilienceCheck'], check=True, timeout=30)
