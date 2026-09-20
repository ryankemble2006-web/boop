"""Behavioural tests for provider-confirmed favourite actions, not visual snapshots."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/DeezerFavouritePolicy.java'

def test_provider_state_actions_and_stale_track_guard(tmp_path):
    assert SOURCE.exists(), 'Deezer favourite action policy is not implemented yet'
    harness = tmp_path / 'FavouritePolicyProbe.java'
    harness.write_text('''
package com.boop.shieldhome;
public final class FavouritePolicyProbe {
  static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }
  public static void main(String[] args) {
    check(DeezerFavouritePolicy.action("Add to favourites") == 1, "add favourite");
    check(DeezerFavouritePolicy.action("Remove from favorites") == -1, "remove favourite");
    check(DeezerFavouritePolicy.action("Don't recommend this track") == 0, "never dislike");
    check(DeezerFavouritePolicy.action("Dislike") == 0, "never dislike alias");
    check(DeezerFavouritePolicy.action("Add to playlist") == 0, "never edit playlist");
    check(DeezerFavouritePolicy.action("Like artist") == 0, "never like artist");
    check(DeezerFavouritePolicy.action(null) == 0, "null action");
    check(DeezerFavouritePolicy.state(null, true, false) == 0, "offered add means unsaved");
    check(DeezerFavouritePolicy.state(null, false, true) == 1, "offered remove means saved");
    check(DeezerFavouritePolicy.state(null, true, true) == -1, "both do not reveal state");
    check(DeezerFavouritePolicy.state(null, false, false) == -1, "unknown is not unsaved");
    check(DeezerFavouritePolicy.state(Boolean.TRUE, true, false) == -1, "contradiction fails closed");
    check(DeezerFavouritePolicy.state(Boolean.FALSE, false, true) == -1, "inverse contradiction");
    check(DeezerFavouritePolicy.state(Boolean.TRUE, false, false) == 1, "heart metadata saved");
    check(DeezerFavouritePolicy.state(Boolean.FALSE, false, false) == 0, "heart metadata unsaved");
    check(DeezerFavouritePolicy.sameTrack(7, "song:a", 7, "song:a"), "same owner");
    check(!DeezerFavouritePolicy.sameTrack(7, "song:a", 8, "song:a"), "changed session");
    check(!DeezerFavouritePolicy.sameTrack(7, "song:a", 7, "song:b"), "changed song");
    check(!DeezerFavouritePolicy.sameTrack(0, "", 0, ""), "no empty identity");
    check(!DeezerFavouritePolicy.sameTrack(7, null, 7, null), "no null identity");
    System.out.println("Favourite policy: 19 assertions passed");
  }
}
''', encoding='utf-8')
    subprocess.run(['javac', '-d', str(tmp_path), str(SOURCE), str(harness)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.FavouritePolicyProbe'], check=True)
