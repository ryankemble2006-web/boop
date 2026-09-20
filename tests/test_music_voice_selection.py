"""Run the production catalogue/client with deterministic public-metadata fixtures."""
from pathlib import Path
import os
import subprocess
from test_deezer_native_receipts import json_dependency

ROOT = Path(__file__).resolve().parents[1]

def test_natural_music_selection_and_clarification(tmp_path):
    jar = json_dependency(tmp_path)
    stubs = {
        'HomeAssistantAuth.java': '''package com.boop.alpha1;
final class HomeAssistantAuth { static class AuthRejectedException extends Exception {
AuthRejectedException(String message) {super(message);} } }''',
        'HomeAssistantEntityDiscoveryClient.java': '''package com.boop.alpha1;
final class HomeAssistantEntityDiscoveryClient {
java.util.Set<String> allowedEntityIds(String base,String token) {return java.util.Collections.emptySet();} }''',
        'DeezerNativeController.java': '''package com.boop.alpha1;
final class DeezerNativeController {
static String played=""; static int plays; static boolean fail;
DeezerNativeController(String base,String token,String entity,DeezerArtistClient.Http http,
DeezerArtistClient.Delay delay,BoopRoom room,BoopRoomSource rooms) {}
void play(org.json.JSONArray macs,DeezerCatalogue.Selection selection) {
if(fail)throw new IllegalStateException("playback rejected");played=selection.url;plays++;} }''',
    }
    for name, content in stubs.items():
        (tmp_path/name).write_text(content, encoding='utf-8')
    sources = [ROOT/'unified/DeezerCatalogue.java', ROOT/'unified/DeezerArtistClient.java',
               ROOT/'unified/shared/MediaRequest.java', ROOT/'source/BoopRoom.java',
               ROOT/'source/BoopRoomSource.java', ROOT/'source/CommandOutcome.java',
               ROOT/'source/LocalReply.java', ROOT/'tests/java/MusicVoiceSelectionProbe.java']
    subprocess.run(['javac', '-encoding', 'UTF-8', '-cp', str(jar), '-d', str(tmp_path),
                    *map(str, sources), *map(str, tmp_path.glob('*.java'))], check=True)
    subprocess.run(['java', '-cp', os.pathsep.join([str(tmp_path), str(jar)]),
                    'com.boop.alpha1.MusicVoiceSelectionProbe'], check=True)
