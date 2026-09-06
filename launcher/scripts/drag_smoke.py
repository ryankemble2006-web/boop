"""Regression: one uninterrupted hold/drag/release must remove a home shortcut."""
import json, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path('launcher-evidence'); OUT.mkdir(exist_ok=True)
PKG = 'com.boop.launcher'
checks = []
def adb(*args): return subprocess.check_output(['adb', *map(str, args)], text=True, timeout=40).strip()
def shell(*args): return adb('shell', *args)
def tree():
    shell('uiautomator', 'dump', '/sdcard/drag-ui.xml')
    xml = shell('cat', '/sdcard/drag-ui.xml')
    (OUT / 'drag-ui.xml').write_text(xml)
    return list(ET.fromstring(xml).iter('node'))
def find(label):
    for _ in range(4):
        nodes = tree()
        for n in nodes:
            if label.casefold() in ((n.get('text') or '').casefold(), (n.get('content-desc') or '').casefold()): return n
        tutorial = next((n for n in nodes if n.get('package') == 'com.android.systemui' and (n.get('text') or '').casefold() == 'got it'), None)
        if tutorial is not None: shell('input', 'tap', *center(tutorial))
        time.sleep(.5)
    raise AssertionError('Missing control: ' + label)
def center(n):
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
    return (x1+x2)//2, (y1+y2)//2
def tap(label, hold=False):
    x, y = center(find(label))
    shell('input', 'swipe', x, y, x, y, 900) if hold else shell('input', 'tap', x, y)
    time.sleep(.6)
def shot(name):
    with (OUT / (name+'.png')).open('wb') as f:
        subprocess.run(['adb', 'exec-out', 'screencap', '-p'], stdout=f, check=True)
def pin():
    shell('input', 'swipe', 720, 2400, 720, 900, 350)
    tap('Search apps'); shell('input', 'text', 'Settings'); shell('input', 'keyevent', 4)
    tap('Settings. Long press to add to home', True)
    tap('Done')
    return center(find('Settings'))

try:
    adb('install', '-r', 'launcher-delivery/BOOP-Launcher-Alpha1.apk')
    shell('pm', 'clear', PKG)
    shell('wm', 'size', '1440x3120'); shell('wm', 'density', 560)
    shell('settings', 'put', 'secure', 'immersive_mode_confirmations', 'confirmed')
    time.sleep(4)
    shell('input', 'keyevent', 82); shell('logcat', '-c')
    shell('am', 'start', '-W', '-n', PKG+'/.MainActivity'); time.sleep(2)
    try: tap('Start')
    except AssertionError: find('Home canvas')
    # Leave the system's existing default Home app unchanged. An unintended
    # Home intent must not be hidden by setting BOOP as the default in this test.
    x, y = pin()
    shell('input', 'motionevent', 'DOWN', x, y); time.sleep(.8)
    nodes = tree(); shot('drag-held')
    assert not any('Move: drag after closing this menu' == n.get('text') for n in nodes), 'Long press opened a modal menu before the finger was lifted'
    checks.append('Long press retains the continuous touch without opening a menu')
    for target_y in range(y-40, 79, -80): shell('input', 'motionevent', 'MOVE', x, target_y)
    shell('input', 'motionevent', 'MOVE', x, 80)
    shell('input', 'motionevent', 'UP', x, 80); time.sleep(.7)
    nodes = tree(); shot('drag-removed')
    assert any(n.get('content-desc') == 'Home canvas' for n in nodes), 'Releasing the drag left BOOP home'
    assert not any(n.get('content-desc') == 'Settings' for n in nodes), 'The dragged shortcut was not removed'
    checks.append('Upward drag removes shortcut and release stays in BOOP')
    shell('am', 'force-stop', PKG); shell('am', 'start', '-W', '-n', PKG+'/.MainActivity')
    time.sleep(.5)
    assert not any(n.get('content-desc') == 'Settings' for n in tree()), 'Removed shortcut returned after restart'
    checks.append('Removal survives a process restart')

    x, y = pin()
    shell('input', 'motionevent', 'DOWN', x, y); time.sleep(.8)
    shell('input', 'motionevent', 'UP', x, y); time.sleep(.5)
    find('Home canvas'); find('Done'); find('Settings')
    assert not any(n.get('text') == 'Small' for n in tree()), 'Stationary long release opened a menu'
    checks.append('Stationary hold enters editing only after release and keeps the shortcut')
    tap('Settings'); tap('Small'); tap('Done')
    checks.append('A separate short edit tap still provides icon size controls')

    x, y = center(find('Settings'))
    shell('input', 'touchscreen', 'draganddrop', x, y, 950, 1700, 650)
    time.sleep(.6); find('Home canvas')
    moved_x, moved_y = center(find('Settings'))
    assert moved_x > 700 and moved_y > 1200, 'Continuous drag did not move the shortcut'
    checks.append('Holding then dragging across home moves the shortcut')
    # Releasing at toolbar height must stay owned by the icon, including over
    # where Bail out normally sits. There is no second tap for removal.
    bailout_x, bailout_y = center(find('Bail out'))
    shell('input', 'swipe', moved_x, moved_y, bailout_x, bailout_y, 400); time.sleep(.5)
    find('Home canvas')
    assert not any(n.get('content-desc') == 'Settings' for n in tree()), 'Edit-mode drag did not remove shortcut'
    checks.append('An edit-mode drag across toolbar space removes without opening Home settings')

    tap('Done')
    x, y = pin()
    shell('input', 'motionevent', 'DOWN', x, y); time.sleep(.8)
    shell('am', 'start', '-a', 'android.settings.SETTINGS'); time.sleep(.5)
    shell('input', 'motionevent', 'UP', x, y)
    shell('input', 'keyevent', 4); time.sleep(.5)
    find('Home canvas'); find('Settings')
    assert not any((n.get('text') or '').casefold() in ('small', 'drag here to remove', 'release to remove') for n in tree()), 'Interrupted hold left a menu or drag target open'
    checks.append('Interrupted hold cancels without removing the shortcut or opening a menu')

    x, y = center(find('Settings'))
    shell('input', 'swipe', x, y, x, y, 900); time.sleep(.5)
    find('Done')
    x, y = center(find('Settings'))
    shell('input', 'motionevent', 'DOWN', x, y); time.sleep(.8)
    shell('input', 'keyevent', 4); shell('input', 'motionevent', 'UP', x, y); time.sleep(.5)
    find('Home canvas'); find('Settings')
    assert not any((n.get('text') or '').casefold() == 'done' for n in tree()), 'Detaching the dragged icon restored editing after Back'
    checks.append('Back during an edit-mode hold cancels and exits editing')
    shot('drag-regressions-passed')
    print('\n'.join('PASS '+c for c in checks), flush=True)
finally:
    shell('input', 'motionevent', 'UP', 720, 80)
    (OUT/'drag-checks.json').write_text(json.dumps(checks, indent=2))
    (OUT/'drag-runtime.txt').write_text(adb('logcat', '-d', '-s', 'AndroidRuntime:E'))
    (OUT/'drag-activity.txt').write_text(shell('dumpsys', 'activity', 'activities'))
    shot('drag-final')
    shell('wm', 'size', 'reset'); shell('wm', 'density', 'reset')
