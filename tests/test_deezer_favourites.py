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
    System.out.println("Favourite policy: 20 assertions passed");
  }
}
''', encoding='utf-8')
    subprocess.run(['javac', '-d', str(tmp_path), str(SOURCE), str(harness)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.FavouritePolicyProbe'], check=True)


def test_requests_need_confirmation_and_reject_duplicate_or_stale_owner(tmp_path):
    source = SOURCE.with_name('DeezerFavouriteRequest.java')
    assert source.exists(), 'Confirmed favourite request lifecycle is not implemented yet'
    harness = tmp_path / 'FavouriteRequestProbe.java'
    harness.write_text('''
package com.boop.shieldhome;
public final class FavouriteRequestProbe {
  static void check(boolean value, String message) {
    if (!value) throw new AssertionError(message);
  }
  public static void main(String[] args) {
    DeezerFavouriteRequest r = new DeezerFavouriteRequest();
    check(!r.pending(), "starts idle");
    check(!r.begin(0, "", true, 100), "reject empty owner");
    check(r.begin(9, "track-a", true, 100), "start explicit add");
    check(r.pending(), "not success merely because sent");
    check(!r.begin(9, "track-a", false, 110), "duplicate/opposite rejected");
    check(!r.confirm(9, "track-a", -1), "unknown cannot confirm");
    check(!r.confirm(9, "track-a", 0), "old state cannot confirm");
    check(!r.confirm(9, "track-b", 1), "other track cannot confirm");
    check(!r.confirm(10, "track-a", 1), "other session cannot confirm");
    check(!r.expired(3099), "before deadline");
    check(r.expired(3100), "bounded wait");
    check(r.confirm(9, "track-a", 1), "provider confirms add");
    check(!r.pending(), "idle after receipt");
    check(!r.confirm(9, "track-a", 1), "receipt consumed once");
    check(r.begin(9, "track-a", false, 4000), "start explicit remove");
    check(!r.confirm(9, "track-a", 1), "saved is not removed");
    check(r.confirm(9, "track-a", 0), "provider confirms removal");
    check(r.begin(9, "track-a", true, 5000), "next request");
    check(r.owns(9, "track-a"), "owns requested track");
    check(!r.owns(9, "track-b"), "track change detected");
    r.cancel();
    check(!r.pending(), "cancel clears pending");
    check(!r.confirm(9, "track-a", 1), "cancelled reply ignored");
    System.out.println("Favourite request: 22 assertions passed");
  }
}
''', encoding='utf-8')
    subprocess.run(['javac', '-d', str(tmp_path), str(SOURCE), str(source), str(harness)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.FavouriteRequestProbe'], check=True)


def test_actual_controller_dispatch_callbacks_timeout_and_screen_lifecycle(tmp_path):
    import runpy
    boundary = runpy.run_path(str(ROOT / 'tests/favourite_android_boundary.py'))
    for name, content in boundary['STUBS'].items():
        path = tmp_path / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding='utf-8')
    harness = tmp_path / 'com/boop/shieldhome/FavouriteControllerProbe.java'
    harness.write_text(boundary['PROBE'], encoding='utf-8')
    production = [SOURCE.with_name(name + '.java') for name in [
        'DeezerFavouritePolicy', 'DeezerFavouriteRequest', 'DeezerFavouriteController',
        'NowPlayingSnapshot', 'NowPlayingState', 'NowPlayingActionPolicy', 'NowPlayingSelectionPolicy']]
    subprocess.run(['javac', '-d', str(tmp_path), *map(str, production),
                    *map(str, tmp_path.rglob('*.java'))], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.shieldhome.FavouriteControllerProbe'], check=True)
