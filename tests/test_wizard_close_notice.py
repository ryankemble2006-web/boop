import ast
from pathlib import Path
from types import SimpleNamespace

ROOT = Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source/plugin.program.ParaNoia19wizard')

def test_close_notice_countdown_and_cleanup():
    tree = ast.parse((ROOT / 'wizard_dialogs.py').read_text(encoding='utf-8'))
    fn = next(n for n in tree.body if isinstance(n, ast.FunctionDef) and n.name == 'show_apply_close_notice')
    events = []
    class Progress:
        def create(self, *args): events.append(('open', args))
        def update(self, *args): events.append(('update', args))
        def close(self): events.append(('close',))
    env = {'InstallProgress': Progress, 'xbmc': SimpleNamespace(Monitor=lambda: SimpleNamespace(waitForAbort=lambda n: events.append(('wait', n)) or False))}
    exec(compile(ast.Module(body=[fn], type_ignores=[]), '<notice>', 'exec'), env)
    env['show_apply_close_notice']()
    assert [e[1] for e in events if e[0] == 'wait'] == [1, 1, 1]
    messages = [e[1][1] for e in events if e[0] == 'update']
    assert all('automatically' in m and 'Reopen' in m for m in messages)
    assert [m.split('automatically in ')[1][0] for m in messages] == ['3', '2', '1']
    assert events[-1] == ('close',)

def test_both_close_paths_explain_close_first():
    for filename, name in [('modules/core.py', 'post_install_check'), ('setup_flow.py', 'finish_remote')]:
        tree = ast.parse((ROOT / filename).read_text(encoding='utf-8'))
        fn = next(n for n in tree.body if isinstance(n, ast.FunctionDef) and n.name == name)
        calls = [n for n in ast.walk(fn) if isinstance(n, ast.Call)]
        notice = next(n for n in calls if isinstance(n.func, ast.Name) and n.func.id == 'show_apply_close_notice')
        close = next(n for n in calls if isinstance(n.func, ast.Attribute) and n.func.attr == '_exit')
        assert notice.lineno < close.lineno
