#!/usr/bin/env python3
"""Exercise the signed candidate's real menu on a disposable CI emulator only."""
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = 'com.boop.alpha1'


def adb(*args):
    result = subprocess.run(['adb', *args], check=True, text=True, capture_output=True, timeout=45)
    return result.stdout.strip()


def hierarchy():
    adb('shell', 'uiautomator', 'dump', '/sdcard/boop-chat-menu.xml')
    text = adb('shell', 'cat', '/sdcard/boop-chat-menu.xml')
    return ET.fromstring(text[text.index('<?xml'):])


def has_text(root, text):
    return any(n.get('text') == text for n in root.iter('node'))


def tap_description(root, description):
    for node in root.iter('node'):
        if node.get('content-desc') == description:
            x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.get('bounds', '')))
            adb('shell', 'input', 'tap', str((x1 + x2) // 2), str((y1 + y2) // 2))
            return
    raise AssertionError('Missing menu choice: ' + description)


def assert_saved(expected):
    xml = adb('shell', 'run-as', PACKAGE, 'cat', 'shared_prefs/boop_chat_mode.xml')
    value = ET.fromstring(xml).find("string[@name='mode']")
    assert value is not None and value.text == expected, 'Chat mode was not saved'


serial = adb('get-serialno')
assert serial.startswith('emulator-'), 'Refusing to operate on a physical device'
size = re.findall(r'(\d+)x(\d+)', adb('shell', 'wm', 'size'))[-1]
width, height = map(int, size)
x, y = width // 2, height // 2


def hold(milliseconds):
    adb('shell', 'input', 'swipe', str(x), str(y), str(x), str(y), str(milliseconds))


# The original playful hold must not become an early settings shortcut.
hold(1600)
assert not has_text(hierarchy(), 'Chat mode'), 'Menu opened before three seconds'
hold(3300)
root = hierarchy()
assert has_text(root, 'Chat mode'), 'Three-second hold did not open the menu'
assert has_text(root, 'OpenCode  \u2713'), 'Fresh install did not default to OpenCode'
tap_description(root, 'Use Free Chat')
assert_saved('free_chat')

# Actual process death/restart, not just creating a second preferences wrapper.
adb('shell', 'am', 'force-stop', PACKAGE)
adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
time.sleep(1)
hold(3300)
root = hierarchy()
assert has_text(root, 'Free Chat  \u2713'), 'Free Chat choice did not survive restart'
tap_description(root, 'Use OpenCode')
assert_saved('opencode')

# A mostly vertical drag must neither open the menu nor be interpreted as a tap.
adb('shell', 'input', 'swipe', str(x), str(y), str(x), str(y - 200), '3300')
assert not has_text(hierarchy(), 'Chat mode'), 'A drag opened the chat menu'

# Cancelling while leaving the Activity must not leave a timer armed.
press = subprocess.Popen(['adb', 'shell', 'input', 'swipe', str(x), str(y), str(x), str(y), '4300'],
                         stdout=subprocess.DEVNULL, stderr=subprocess.PIPE)
time.sleep(1.2)
adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
press.communicate(timeout=15)
adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
assert not has_text(hierarchy(), 'Chat mode'), 'Backgrounded hold later opened a menu'
assert adb('shell', 'pidof', PACKAGE), 'BOOP stopped running'
adb('shell', 'rm', '-f', '/sdcard/boop-chat-menu.xml')
print('PASS: short hold, three-second menu, default, Free Chat, process restart, revert, drag and pause cancellation')
