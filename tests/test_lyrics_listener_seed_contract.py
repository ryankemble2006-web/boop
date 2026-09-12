from pathlib import Path

source = Path('unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingListenerService.java').read_text(encoding='utf-8')
connected = source.split('public void onListenerConnected()', 1)[1].split('@Override', 1)[0]
if 'getActiveNotifications()' not in connected or 'onNotificationPosted(existing)' not in connected:
    raise SystemExit('RED: listener does not seed already-active media notifications')
print('lyrics listener seed contract PASS')
