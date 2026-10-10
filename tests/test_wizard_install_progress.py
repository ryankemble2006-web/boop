import ast
from pathlib import Path
import types

ROOT = Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source/plugin.program.ParaNoia19wizard')

def test_install_window_cannot_be_dismissed_by_input():
    tree = ast.parse((ROOT / 'wizard_dialogs.py').read_text(encoding='utf-8'))
    cls = next(n for n in tree.body if isinstance(n, ast.ClassDef) and n.name == '_InstallProgressWindow')
    class Base:
        def close(self):
            raise AssertionError('Input dismissed the installation')
    env = {'_WizardMessageWindow': Base}
    exec(compile(ast.Module(body=[cls], type_ignores=[]), '<progress>', 'exec'), env)
    window = object.__new__(env['_InstallProgressWindow'])
    for action in (7, 9, 10, 13, 92, 216):
        window.onAction(types.SimpleNamespace(getId=lambda: action))
    for control in (20, 21, 22):
        window.onClick(control)

def test_wipe_uses_foreground_and_handoff_message():
    source = (ROOT / 'modules/core.py').read_text(encoding='utf-8')
    assert 'dialog = InstallProgress()' in source
    assert 'dialog = xbmcgui.DialogProgressBG()' not in source
    assert source.index('Hold still for the next screen') < source.index('if on_success is not None:')

def test_progress_lifecycle_and_updates():
    tree = ast.parse((ROOT / 'wizard_dialogs.py').read_text(encoding='utf-8'))
    classes = [n for n in tree.body if isinstance(n, ast.ClassDef) and n.name in ('_InstallProgressWindow', 'InstallProgress')]
    events = []
    class Control:
        def setLabel(self, text): events.append(('label', text))
        def setText(self, text): events.append(('text', text))
        def setVisible(self, visible): events.append(('visible', visible))
    class Base:
        def __init__(self, *args): pass
        def configure(self, heading, message): self.heading, self.message = heading, message
        def getControl(self, control): return Control()
        def show(self): pending.append(self.onInit); events.append(('show', True))
        def close(self): events.append(('close', True))
    pending = []
    class Monitor:
        def waitForAbort(self, seconds):
            for callback in pending[:]: callback()
            pending.clear()
            return False
    env = {'xbmc': types.SimpleNamespace(Monitor=Monitor), '_WizardMessageWindow': Base, 'WINDOW_XML': '', '_addon_path': lambda: '',
           'SKIN_NAME': '', 'SKIN_RESOLUTION': '', 'BODY_CONTROL': 10, 'FOOTER_CONTROL': 30,
           'OK_CONTROL': 20, 'NO_CONTROL': 21, 'YES_CONTROL': 22, '_textbox_text': str}
    exec(compile(ast.Module(body=classes, type_ignores=[]), '<progress>', 'exec'), env)
    dialog = env['InstallProgress']()
    dialog.create('Installing', 'Wiping')
    assert events.count(('visible', False)) == 3
    dialog.update(25, message='Extracting')
    dialog.update(100, message='Hold still for the next screen')
    assert ('text', 'Extracting') in events
    assert events[-2] == ('text', 'Hold still for the next screen')
    assert not any(event[0] == 'close' for event in events)
    dialog.close()
    assert events[-1] == ('close', True)
