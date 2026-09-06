"""Regression: one uninterrupted hold/drag/release must remove a home shortcut."""
import json, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path

OUT = Path('launcher-evidence'); OUT.mkdir(exist_ok=True)
PKG = 'com.boop.launcher'
checks = []
def adb(*args): return subprocess.check_output(['adb', *map(str, args)], text=True).strip()
def shell(*args): return adb('shell', *args)
def tree():
    shell('uiautomator', 'dump', '/sdcard/drag-ui.xml')
    xml = shell('cat', '/sdcard/drag-ui.xml')
    (OUT / 'drag-ui.xml').write_text(xml)
    return list(ET.fromstring(xml).iter('node'))
def find(label):
    for _ in range(4):
        for n in tree():
            if label in (n.get('text'), n.get('content-desc')): return n
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
    shell('input', 'keyevent', 82); shell('logcat', '-c')
    shell('am', 'start', '-W', '-n', PKG+'/.MainActivity'); time.sleep(2)
    tap('Start')
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
    print('\n'.join('PASS '+c for c in checks), flush=True)
finally:
    shell('input', 'motionevent', 'UP', 720, 80)
    (OUT/'drag-checks.json').write_text(json.dumps(checks, indent=2))
    (OUT/'drag-runtime.txt').write_text(adb('logcat', '-d', '-s', 'AndroidRuntime:E'))
    (OUT/'drag-activity.txt').write_text(shell('dumpsys', 'activity', 'activities'))
    shot('drag-final')
    shell('wm', 'size', 'reset'); shell('wm', 'density', 'reset')
