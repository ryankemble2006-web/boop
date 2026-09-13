#!/usr/bin/env bash
set -euo pipefail
python3 tests/test_unified_native_lyrics.py
mkdir -p work/unified-native-lyrics
curl --fail --silent --show-error --location --max-time 30 https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar -o work/unified-native-lyrics/json.jar
echo '3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed  work/unified-native-lyrics/json.jar' | sha256sum -c -
src=unified/shield-home/src/main/java/com/boop/shieldhome
javac -encoding UTF-8 -cp work/unified-native-lyrics/json.jar -d work/unified-native-lyrics \
  "$src/DeezerLyricsDocument.java" "$src/LyricsRequestGate.java" "$src/DeezerLyricsClient.java" \
  "$src/DeezerLyricsHttp.java" "$src/DeezerTimedLyricsClient.java" \
  tests/canonical/NativeLyricsCoreCheck.java tests/canonical/NativeLyricsTransportCheck.java tests/canonical/NativeLyricsIncompleteCheck.java
for check in NativeLyricsCoreCheck NativeLyricsTransportCheck NativeLyricsIncompleteCheck; do
  java -cp work/unified-native-lyrics:work/unified-native-lyrics/json.jar "com.boop.shieldhome.$check"
done
python3 tests/test_native_lyrics_entry.py
python3 tests/test_unified_lyrics_activity.py
