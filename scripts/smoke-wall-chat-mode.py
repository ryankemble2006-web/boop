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
    # UIAutomator can exit successfully without a file while a freshly restarted
    # Activity is settling. Retry only capture failures, never UI assertions.
    for attempt in range(4):
        try:
            adb('shell', 'rm', '-f', DUMP)
            report = adb('shell', 'uiautomator', 'dump', DUMP)
            text = adb('shell', 'cat', DUMP)
            root = ET.fromstring(text[text.index('<?xml'):])
            print('BOOP_CHAT_UI_' + label + ': ' + ET.tostring(root, encoding='unicode'), flush=True)
            return root
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired, ET.ParseError, ValueError) as error:
            print(f'BOOP_CHAT_CAPTURE_RETRY {attempt + 1}/4 {label}: {type(error).__name__}', flush=True)
            if attempt == 3:
                raise
            time.sleep(0.75)
    raise AssertionError('Unreachable capture state')


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


def dismiss_immersive_tutorial(root):
    """Dismiss only the observed first-run fullscreen tutorial, never permissions."""
    system = 'com.android.systemui'
    tutorial = any(n.get('package') == system
                   and n.get('resource-id') == system + ':id/immersive_cling_title'
                   and n.get('text') == 'Viewing full screen'
                   for n in root.iter('node'))
    if not tutorial:
        return False
    buttons = [n for n in root.iter('node')
               if n.get('package') == system
               and n.get('resource-id') == system + ':id/ok'
               and n.get('text') == 'Got it'
               and n.get('clickable') == 'true']
    assert len(buttons) == 1, 'Fullscreen tutorial has no uniquely recognized acknowledgement'
    x, y = center(buttons[0].get('bounds', ''))
    adb('shell', 'input', 'tap', str(x), str(y))
    print('BOOP_CHAT_IMMERSIVE_TUTORIAL_ACKNOWLEDGED=1', flush=True)
    return True


def tap_description(root, description):
    for node in root.iter('node'):
        if node.get('package') == PACKAGE and node.get('content-desc') == description:
            x, y = center(node.get('bounds', ''))
            adb('shell', 'input', 'tap', str(x), str(y))
            return
    raise AssertionError('Missing menu choice: ' + description)



def assert_selected_mode(root, label):
    # Android can transform a Button's displayed case; the app-owned accessible
    # description is stable. Still require this exact mode and its selected mark.
    matches = [node for node in root.iter('node')
               if node.get('package') == PACKAGE
               and node.get('class') == 'android.widget.Button'
               and node.get('content-desc') == 'Use ' + label]
    assert len(matches) == 1, 'Mode choice is missing or ambiguous: ' + label
    assert matches[0].get('text', '').rstrip().endswith('\u2713'), 'Mode is not selected: ' + label


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
    root = hierarchy('startup')
    if dismiss_immersive_tutorial(root):
        root = hierarchy('after_fullscreen_tutorial')
    face_center(root)  # Require the actual app, not a startup overlay.
    # The original playful hold must not become an early settings shortcut.
    hold(1600)
    assert not has_text(hierarchy('short_hold'), 'Chat mode'), 'Menu opened before three seconds'
    hold(3300)
    root = hierarchy('long_hold')
    assert has_text(root, 'Chat mode'), 'Three-second hold did not open the menu'
    assert_selected_mode(root, 'OpenCode')
    tap_description(root, 'Use Free Chat')
    assert_saved('free_chat')

    # Actual process death/restart, not just creating a second preferences wrapper.
    adb('shell', 'am', 'force-stop', PACKAGE)
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
    time.sleep(1)
    hold(3300)
    root = hierarchy('restart')
    assert_selected_mode(root, 'Free Chat')
    tap_description(root, 'Use OpenCode')
    assert_saved('opencode')

    # Native Chat is a third persistent, reversible mode, not a replacement.
    # Do not submit any provider question from CI even if private configuration exists.
    hold(3300)
    root = hierarchy('native_choice')
    tap_description(root, 'Use Native Chat')
    assert_saved('native_chat')
    adb('shell', 'am', 'force-stop', PACKAGE)
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/.MainActivity')
    time.sleep(1)
    hold(3300)
    root = hierarchy('native_restart')
    assert_selected_mode(root, 'Native Chat')
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
    print('PASS: short hold, three-second menu, default, Free Chat, Native Chat, process restart, revert, drag and pause cancellation')


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
