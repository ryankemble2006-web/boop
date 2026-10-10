import ast,types
from pathlib import Path

ROOT=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source')

def test_background_slideshow_never_opens():
    for skin in ('skin.NTVaura','skin.Eden'):
        tree=ast.parse((ROOT/skin/'idle_slideshow.py').read_text(encoding='utf-8'))
        fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='show_slideshow')
        env={'animation_allowed':lambda:False,'kodi_setup_blocked':lambda *a:False}
        exec(compile(ast.Module(body=[fn],type_ignores=[]),'<slideshow>','exec'),env)
        # No GUI, storage or player API should be touched when Android is in background.
        env['show_slideshow'](None,None,None,None)
        source=ast.unparse(fn)
        assert source.count('animation_allowed()')>=2, 'must dismiss an already-open slideshow on loss of foreground'
