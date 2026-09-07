#!/usr/bin/env python3
"""Exercise the signed candidate's real menu on a disposable CI emulator only."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = 'com.boop.alpha1'
DUMP = '/sdcard/boop-chat-menu.xml'


def adb(*args):
    result = subprocess.run(['adb', *args], check=True, text=True, capture_output=True, timeout=45)
    return result.stdout.strip()


def hierarchy(label='screen'):
    # A failed dump must not let the next assertion inspect a stale screen.
    adb('shell', 'rm', '-f', DUMP)
    adb('shell', 'uiautomator', 'dump', DUMP)
    text = adb('shell', 'cat', DUMP)
    root = ET.fromstring(text[text.index('<?xml'):])
    print('BOOP_CHAT_UI_' + label + ': ' + ET.tostring(root, encoding='unicode'), flush=True)
    return root


def has_text(root, text):
    return any(n.get('text') == text for n in root.iter('node'))


def center(bounds):
    match = re.fullmatch(r'\[(-?\d+),(-?\d+)\]\[(-?\d+),(-?\d+)\]', bounds)
    assert match is not None, 'Missing or malformed screen bounds: ' + bounds
    x1, y1, x2, y2 = map(int, match.groups())
    assert x2 > x1 and y2 > y1, 'Empty or inverted screen bounds: ' + bounds
    return (x1 + x2) // 2, (y1 + y2) // 2


def face_center(root):
    for node in root.iter('node'):
        if (node.get('package') == PACKAGE
                and node.get('content-desc', '').startswith('BOOP face.')):
            return center(node.get('bounds', ''))
    raise AssertionError('BOOP face is not the visible input surface; inspect BOOP_CHAT_UI output')


def tap_description(root, description):
    for node in root.iter('node'):
        if node.get('package') == PACKAGE and node.get('content-desc') == description:
            x, y = center(node.get('bounds', ''))
            adb('shell', 'input', 'tap', str(x), str(y))
            return
    raise AssertionError('Missing menu choice: ' + description)


def assert_saved(expected):
    xml = adb('shell', 'run-as', PACKAGE, 'cat', 'shared_prefs/boop_chat_mode.xml')
    value = ET.fromstring(xml).find("string[@name='mode']")
    assert value is not None and value.text == expected, 'Chat mode was not saved'


def hold(milliseconds):
    x, y = face_center(hierarchy('before_hold_' + str(milliseconds)))
    print(f'BOOP_CHAT_PRESS: {x},{y} for {milliseconds}ms', flush=True)
    adb('shell', 'input', 'swipe', str(x), str(y), str(x), str(y), str(milliseconds))


def diagnose_failure():
    # This function runs only after main has checked the emulator serial.
    try:
        hierarchy('failure')
    except Exception as error:
        print('BOOP_CHAT_UI_CAPTURE_FAILED: ' + repr(error), flush=True)
    for args in [('shell', 'wm', 'size'),
                 ('shell', 'dumpsys', 'window', 'windows'),
                 ('logcat', '-d', '-v', 'brief', 'AndroidRuntime:E', 'BOOP-Wake:I', '*:S')]:
        try:
            output = adb(*args)
            if 'windows' in args:
                output = '\n'.join(line for line in output.splitlines()
                                   if any(key in line for key in ('mCurrentFocus', 'mFocusedApp', 'mHasSurface', 'Window #')))
            print('BOOP_CHAT_DIAGNOSTIC ' + ' '.join(args) + '\n' + output, flush=True)
        except Exception as error:
            print('BOOP_CHAT_DIAGNOSTIC_FAILED: ' + repr(error), flush=True)


def exercise():
    print('BOOP_CHAT_PANEL: ' + adb('shell', 'wm', 'size'), flush=True)
    # The original playful hold must not become an early settings shortcut.
    hold(1600)
    assert not has_text(hierarchy('short_hold'), 'Chat mode'), 'Menu opened before three seconds'
    hold(3300)
    root = hierarchy('long_hold')
    assert has_text(root, 'Chat mode'), 'Three-second hold did not open the menu'
    assert has_text(root, 'OpenCode  \u2713'), 'Fresh install did not default to OpenCode'
    tap_description(root, 'Use Free Chat')
    assert_saved('free_chat')

    # Actual process death/restart, not just creating a second preferences wrapper.
    adb('shell', 'am', 'force-stop', PACKAGE)
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
    time.sleep(1)
    hold(3300)
    root = hierarchy('restart')
    assert has_text(root, 'Free Chat  \u2713'), 'Free Chat choice did not survive restart'
    tap_description(root, 'Use OpenCode')
    assert_saved('opencode')

    # A mostly vertical drag must neither open the menu nor be interpreted as a tap.
    x, y = face_center(hierarchy('before_drag'))
    adb('shell', 'input', 'swipe', str(x), str(y), str(x), str(max(1, y - 200)), '3300')
    assert not has_text(hierarchy('after_drag'), 'Chat mode'), 'A drag opened the chat menu'

    # Cancelling while leaving the Activity must not leave a timer armed.
    x, y = face_center(hierarchy('before_background'))
    press = subprocess.Popen(['adb', 'shell', 'input', 'swipe', str(x), str(y), str(x), str(y), '4300'],
                             stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
    time.sleep(1.2)
    adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
    press.communicate(timeout=15)
    assert press.returncode == 0, 'Injected background-cancellation gesture failed'
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
    assert not has_text(hierarchy('after_background'), 'Chat mode'), 'Backgrounded hold later opened a menu'
    assert adb('shell', 'pidof', PACKAGE), 'BOOP stopped running'
    adb('shell', 'rm', '-f', DUMP)
    print('PASS: short hold, three-second menu, default, Free Chat, process restart, revert, drag and pause cancellation')


def main():
    serial = adb('get-serialno')
    assert serial.startswith('emulator-'), 'Refusing to operate on a physical device'
    try:
        exercise()
    except Exception:
        diagnose_failure()
        raise


if __name__ == '__main__':
    main()
