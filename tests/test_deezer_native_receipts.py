# Reproduce the native voice/heart ADB reply race without accessing a device.
from pathlib import Path
import os, subprocess, hashlib, urllib.request
ROOT=Path(__file__).resolve().parents[1]
JSON_HASH='3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed'
def json_dependency(tmp_path):
    cache=Path(os.environ.get('GRADLE_USER_HOME',str(Path.home()/'.gradle')))
    jars=list((cache/'caches/modules-2/files-2.1/org.json/json/20240303').glob('*/*.jar'))
    jar=jars[0] if jars else tmp_path/'json.jar'
    if not jar.exists():
        with urllib.request.urlopen('https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar',timeout=30) as response:
            jar.write_bytes(response.read())
    assert hashlib.sha256(jar.read_bytes()).hexdigest()==JSON_HASH
    return jar

def test_music_receipt_survives_heart_response_and_never_replays(tmp_path):
    jar=json_dependency(tmp_path)
    stubs={
      'DeezerArtistClient.java': '''package com.boop.alpha1;
import org.json.JSONObject;
final class DeezerArtistClient {
 interface Http { String request(String url,String token,JSONObject body)throws Exception; }
 interface Delay { void sleep(long ms)throws InterruptedException; }
}''',
      'DeezerCatalogue.java': '''package com.boop.alpha1;
final class DeezerCatalogue {static final class Selection {boolean flow=true;String url="https://www.deezer.com/flow";}}''',
      'DeezerBridgePayload.java': '''package com.boop.alpha1;
final class DeezerBridgePayload {static final String BASE64="TEST_PAYLOAD",SHA256="TEST_SHA";}'''}
    for name,text in stubs.items(): (tmp_path/name).write_text(text,encoding='utf-8')
    production=[ROOT/'unified/DeezerNativeController.java',ROOT/'source/BoopRoom.java',ROOT/'source/BoopRoomSource.java',ROOT/'tests/java/DeezerReceiptRaceProbe.java']
    utility=ROOT/'unified/AdbCommandReceipt.java'
    if utility.exists(): production.append(utility)
    subprocess.run(['javac','-encoding','UTF-8','-cp',str(jar),'-d',str(tmp_path),*map(str,production),*map(str,tmp_path.glob('*.java'))],check=True)
    subprocess.run(['java','-cp',os.pathsep.join([str(tmp_path),str(jar)]),'com.boop.alpha1.DeezerReceiptRaceProbe'],check=True)
