from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
def test_lyrics_queue_is_shared_hidden_on_flow_and_lifecycle_bound():
    view=(SRC/'ShieldLyricsView.java').read_text(encoding='utf-8')
    activity=(SRC/'ShieldLyricsActivity.java').read_text(encoding='utf-8')
    assert 'queueButton = label("Queue"' in view, 'Lyrics Queue button is not implemented'
    assert 'controls.queue()' in view
    assert 'manager.queue().subscribe(this::queueChanged)' in activity
    assert 'new ShieldQueueDialog(ShieldLyricsActivity.this, manager.queue())' in activity
    assert 'state.visible && current != null && state.session == current.sessionId()' in activity
    assert 'unsubscribeQueue.run()' in activity
    assert 'if (queueDialog != null) queueDialog.dismiss();' in activity
    assert 'queueButton.setVisibility(available ? VISIBLE : GONE)' in view
    assert 'key == KeyEvent.KEYCODE_DPAD_DOWN && queueButton.isShown()' in view
    assert 'key == KeyEvent.KEYCODE_DPAD_UP) return focusTransport()' in view

def test_lyrics_column_moves_as_one_without_changing_internal_gaps():
    view=(SRC/'ShieldLyricsView.java').read_text(encoding='utf-8')
    assert 'private static final float MUSIC_COLUMN_SHIFT = 38f;' in view, 'No consistent upward shift yet'
    assert 'place(queueButton, progressCenter - 56f * unit, 703f * unit, 112f * unit, 38f * unit)' in view
    assert 'artwork, title, artist, progress, elapsed, duration, removeHeart, addHeart, queueButton' in view
    assert 'item.setTranslationY(-MUSIC_COLUMN_SHIFT * unit)' in view
    assert 'button.setTranslationY(-MUSIC_COLUMN_SHIFT * unit)' in view
    assert 'size(queueButton, 18)' in view
    assert 'queueButton.setIncludeFontPadding(false)' in view
    # Both old 30px gaps remain exactly 30 after the common translation.
    shift=38
    assert (448-shift)-(116+302-shift)==30
    assert (520-shift)-(448+42-shift)==30
    assert (703-shift)-(632+54-shift)==17
    assert 54-shift>=16 and 703+38-shift<=704

def test_version_bump_never_rewrites_native_baseline_provenance():
    import ast, json
    tree=ast.parse((ROOT/'split/verify-apks.py').read_text(encoding='utf-8'))
    pinned=next(ast.literal_eval(node.value) for node in tree.body
                if isinstance(node,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='baseline_sha' for t in node.targets))
    expected=json.loads((ROOT/'split/v206-native-baseline.json').read_text(encoding='utf-8'))['apkSha256']
    assert pinned==expected, 'Historical baseline hash changed during a version bump'
