import ast, importlib.util, json
from pathlib import Path
import pytest

ROOT=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source')

@pytest.mark.parametrize('skin', ['skin.NTVaura', 'skin.Eden', 'skin.aura.liquid'])
def test_home_invokes_checker(skin):
    homes=list((ROOT/skin).rglob('Home.xml'))
    assert homes
    assert all('startup_message_service.py' in p.read_text(encoding='utf-8') for p in homes)

@pytest.mark.parametrize('skin', ['skin.NTVaura', 'skin.Eden', 'skin.aura.liquid'])
@pytest.mark.parametrize('touch', [False, True])
def test_existing_variant_gets_reason_and_install_choice(tmp_path,skin,touch):
    spec=importlib.util.spec_from_file_location('preset',ROOT/'plugin.program.ParaNoia19wizard/preset_updates.py')
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
    rows=[dict(id='remote',name='Seren',version='1.0.1',url='https://example/remote',reason='Menus moved to wizard'),dict(id='touch',name='Seren Touch',version='1.0.1',url='https://example/touch',reason='Menus moved to wizard')]
    current=rows[int(touch)]
    path=tmp_path/'state.json';path.write_text(json.dumps({skin:dict(current,version='1.0.0')}))
    offered=[];installed=[]
    class Dialog:
        def yesnocustom(self,heading,body,**kwargs):
            offered.append((heading,body,kwargs));return 1
    module.check(skin,path,lambda:rows,Dialog(),lambda row,on_success:installed.append(row))
    assert installed==[current]
    assert current['reason'] in offered[0][1]
    assert offered[0][2]['yeslabel']=='Install'

def test_worker_has_duplicate_guard():
    code=(ROOT/'plugin.program.ParaNoia19wizard/startup_message_service.py').read_text()
    assert 'Wizard.StartupCheck.' in code

def test_update_prompt_uses_roomy_dialog():
    code=(ROOT/'plugin.program.ParaNoia19wizard/preset_updates.py').read_text()
    assert 'wizard_gui.Dialog()' in code
    tree=ast.parse((ROOT/'plugin.program.ParaNoia19wizard/wizard_dialogs.py').read_text())
    dialog=next(n for n in tree.body if isinstance(n,ast.ClassDef) and n.name=='WizardDialog')
    assert any(isinstance(n,ast.FunctionDef) and n.name=='yesnocustom' for n in dialog.body)

def test_zero_start_delay_never_blocks_update_check(monkeypatch):
    import sys
    from types import SimpleNamespace
    tree=ast.parse((ROOT/'plugin.program.ParaNoia19wizard/startup_message_service.py').read_text())
    fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='_run')
    checks=[]
    class Monitor:
        def abortRequested(self): return False
        def waitForAbort(self,seconds):
            assert seconds>0, 'Zero timeout blocks forever in Kodi'
            return False
    ticks=iter(range(100))
    monkeypatch.setitem(sys.modules,'setup_flow',SimpleNamespace(load_state=lambda:None,invalidate_menu_cache=lambda:None))
    monkeypatch.setitem(sys.modules,'preset_updates',SimpleNamespace(startup_check=lambda:checks.append(True)))
    monkeypatch.setitem(sys.modules,'notifier',SimpleNamespace(check_for_updates=lambda *a:None))
    env={'time':SimpleNamespace(monotonic=lambda:next(ticks)), 'xbmc':SimpleNamespace(Monitor=Monitor,executebuiltin=lambda *a:None,log=lambda *a:None,LOGDEBUG=0,LOGINFO=1,LOGERROR=2,LOGWARNING=3), 'xbmcgui':SimpleNamespace(getCurrentWindowId=lambda:10000,getCurrentWindowDialogId=lambda:9999,Window=lambda n:SimpleNamespace(getProperty=lambda n:'')), 'HOME_WINDOW_ID':10000,'NO_DIALOG_WINDOW_ID':9999,'START_DELAY_SECONDS':0,'MAX_HOME_WAIT_SECONDS':120,'SETUP_SETTLE_SECONDS':2,'IDLE_WAIT_SECONDS':1,'ADDON_ID':'wizard','ADDON_NAME':'Wizard'}
    exec(compile(ast.Module(body=[fn],type_ignores=[]),'<worker>','exec'),env)
    env['_run']()
    assert checks==[True]

@pytest.mark.parametrize('control,choice',[(22,1),(21,0),(20,2)])
def test_roomy_dialog_button_results(control,choice):
    tree=ast.parse((ROOT/'plugin.program.ParaNoia19wizard/wizard_dialogs.py').read_text())
    cls=next(n for n in tree.body if isinstance(n,ast.ClassDef) and n.name=='_WizardThreeChoiceWindow')
    fn=next(n for n in cls.body if isinstance(n,ast.FunctionDef) and n.name=='onClick')
    from types import SimpleNamespace
    closed=[];window=SimpleNamespace(close=lambda:closed.append(True))
    env={'YES_CONTROL':22,'NO_CONTROL':21,'OK_CONTROL':20}
    exec(compile(ast.Module(body=[fn],type_ignores=[]),'<buttons>','exec'),env)
    env['onClick'](window,control)
    assert window.result==choice and closed==[True]
