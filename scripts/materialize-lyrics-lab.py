#!/usr/bin/env python3
"""Copy approved shared sources into the isolated package; never patch the Unified app."""
from pathlib import Path
import argparse
import shutil

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
SHARED = ('ShieldLyricsView', 'LyricsLinesView', 'DeezerLyricsDocument',
          'DeezerLyricsClient', 'DeezerLyricsHttp', 'DeezerTimedLyricsClient',
          'NativeLyricsLoader', 'LyricsRequestGate', 'NowPlayingSnapshot',
          'NowPlayingActionPolicy', 'NowPlayingArtworkResolver',
          'NowPlayingArtworkSourcePolicy', 'NowPlayingAccessSettingsPlan')

def materialize(output: Path) -> None:
    java = output / 'java/com/boop/shieldhome'
    java.mkdir(parents=True, exist_ok=True)
    for name in SHARED:
        shutil.copy2(SOURCE / (name + '.java'), java / (name + '.java'))
    drawable = output / 'res/drawable-nodpi'
    drawable.mkdir(parents=True, exist_ok=True)
    shutil.copy2(ROOT / 'unified/assets/boop-eyes/boopApprovedEyes.png', drawable / 'boop_eyes.png')

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, default=ROOT / 'lyrics-lab/app/build/lyrics-shared')
    args = parser.parse_args()
    materialize(args.output)
    print('Copied unchanged production lyrics sources into com.boop.lyricslab. Unified is untouched.')
