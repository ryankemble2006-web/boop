import ast,types
from pathlib import Path
import pytest
PATH=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source/plugin.program.ParaNoia19wizard/setup_flow.py')

@pytest.mark.parametrize('skin',['skin.NTVaura','skin.aura.liquid','skin.Eden'])
def test_switch_queues_only_supported_theme(skin):
    tree=ast.parse(PATH.read_text())
    fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='queue_switch_theme')
    events=[]
    env={'xbmc':types.SimpleNamespace(getSkinDir=lambda:skin),'os':types.SimpleNamespace(getpid=lambda:123),'save_state':lambda s:events.append(s),'clear_state':lambda:events.append('clear')}
    exec(compile(ast.Module(body=[fn],type_ignores=[]),'<queue>','exec'),env)
    env['queue_switch_theme']()
    assert events==(['clear'] if skin=='skin.Eden' else [{'stage':'theme','skin':skin,'purpose':'switch_skin','after_close_pid':123}])

def test_switch_theme_finishes_without_build_onboarding():
    tree=ast.parse(PATH.read_text())
    run=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='run')
    body=ast.get_source_segment(PATH.read_text(),run)
    assert "if state.get('purpose') != 'switch_skin':" in body
    theme=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='finish_theme')
    assert 'clear_state()' in ast.get_source_segment(PATH.read_text(),theme)
