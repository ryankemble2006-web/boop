from pathlib import Path
import ast,types

PATH=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source/plugin.program.ParaNoia19wizard/wizard_dialogs.py')
def test_completion_countdown():
    tree=ast.parse(PATH.read_text(encoding='utf-8'))
    fn=next((n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='_completion_countdown'),None)
    assert fn is not None, 'Final setup countdown is missing'
    for early in (False,True):
        labels=[];waits=[];closed=[]
        window=types.SimpleNamespace(completion_closed=False,
            getControl=lambda n:types.SimpleNamespace(setLabel=lambda value:labels.append(value)),
            close=lambda:closed.append(True))
        class Monitor:
            def waitForAbort(self,seconds):
                waits.append(seconds)
                if early:window.completion_closed=True
                return False
        env={'xbmc':types.SimpleNamespace(Monitor=Monitor),'FOOTER_CONTROL':30}
        exec(compile(ast.Module(body=[fn],type_ignores=[]),'<countdown>','exec'),env)
        env['_completion_countdown'](window)
        assert labels==(['Closing in 5'] if early else ['Closing in '+str(n) for n in (5,4,3,2,1)])
        assert sum(waits)==(1 if early else 5)
        assert len(closed)==(0 if early else 1)

def test_completion_is_last_after_optional_backup(monkeypatch):
    import sys
    tree=ast.parse(PATH.with_name('setup_flow.py').read_text(encoding='utf-8'))
    fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='offer_backup')
    for choice in (False,True,None):
        events=[]
        monkeypatch.setitem(sys.modules,'wizard_dialogs',types.SimpleNamespace(show_setup_complete=lambda:events.append('complete')))
        monkeypatch.setitem(sys.modules,'backup_engine',types.SimpleNamespace(run_backup=lambda *a:events.append('backup')))
        env={'ready_to_resume':lambda:True,'offer_seren_setup':lambda:True,
             'clear_state':lambda:events.append('clear'),
             'wizard_gui':types.SimpleNamespace(Dialog=lambda:types.SimpleNamespace(yesno_required=lambda *a,**k:choice)),
             'xbmcvfs':types.SimpleNamespace(translatePath=lambda p:p),
             'xbmcaddon':types.SimpleNamespace(Addon=lambda n:types.SimpleNamespace(getAddonInfo=lambda n:'profile')),
             'ADDON_ID':'wizard'}
        exec(compile(ast.Module(body=[fn],type_ignores=[]),'<finish>','exec'),env)
        env['offer_backup']()
        assert events==([] if choice is None else ['clear','backup','complete'] if choice else ['clear','complete'])
