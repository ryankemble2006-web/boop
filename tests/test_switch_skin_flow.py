import ast, types, sys
from pathlib import Path
import pytest

ROOT=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source/plugin.program.ParaNoia19wizard')

def function(name, env):
    fn=next(n for n in ast.parse((ROOT/'setup_flow.py').read_text()).body if isinstance(n,ast.FunctionDef) and n.name==name)
    exec(compile(ast.Module(body=[fn],type_ignores=[]),'<setup>','exec'),env)
    return env[name]

def test_switch_starts_saved_selection_without_overwriting_active_flow():
    for locked in (False,True):
        events=[]
        home=types.SimpleNamespace(getProperty=lambda key:'owner' if locked else '',setProperty=lambda *a:events.append(('resume',a)))
        env={'xbmc':types.SimpleNamespace(getSkinDir=lambda:'skin.Eden',executebuiltin=lambda value:events.append(('run',value))),
             'xbmcgui':types.SimpleNamespace(Window=lambda n:home),'LOCK':'lock','ADDON_ID':'wizard',
             'save_state':lambda s:events.append(('save',s)), 'os':__import__('os'),
             'xbmcvfs':types.SimpleNamespace(translatePath=lambda p:p),
             'xbmcaddon':types.SimpleNamespace(Addon=lambda n:types.SimpleNamespace(getAddonInfo=lambda k:'/wizard'))}
        function('launch',env)(switch_skin=True)
        if locked: assert [e[0] for e in events]==['resume']
        else:
            assert events[0][1]=={'stage':'choose','skin':'skin.Eden','purpose':'switch_skin'}
            assert events[1][0]=='run'

def test_switch_skin_is_third_root_item_and_provider_is_fourth():
    tree=ast.parse((ROOT/'modules/core.py').read_text(encoding='utf-8'))
    fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='main_menu')
    root=next(n.orelse for n in fn.body if isinstance(n,ast.If) and n.orelse and any(isinstance(x,ast.Import) and any(a.name=='setup_flow' for a in x.names) for x in n.orelse))
    items=[(n.targets[0].id,n.value.keywords[0].value.value) for n in root if isinstance(n,ast.Assign) and isinstance(n.value,ast.Call) and isinstance(n.value.func,ast.Attribute) and n.value.func.attr=='ListItem']
    labels=[label for var,label in items]
    assert labels[2]=='Switch Skin'
    assert 'Switch Content Provider' in labels[3]

@pytest.mark.parametrize('skin',['skin.Eden','skin.NTVaura','skin.aura.liquid'])
@pytest.mark.parametrize('touch',[False,True,None])
@pytest.mark.parametrize('succeed',[False,True])
def test_switch_applies_correct_menus_and_keeps_remote(monkeypatch,skin,touch,succeed):
    events=[]
    row={'name':'Seren Touch' if touch else 'Seren','url':'https://test/menus.zip'}
    def install(url,name,mode,**kw):
        events.append(('install',name,mode,kw['post_install']))
        if succeed: kw['on_success']()
        return succeed
    monkeypatch.setitem(sys.modules,'modules.core',types.SimpleNamespace(
        SKIN_PROVIDER_CATALOGUES={skin:('Skin','catalog')},parse_remote_data=lambda *a,**kw:[row],download_and_extract=install))
    env={'load_state':lambda:{'stage':'touch','purpose':'switch_skin'},
         'xbmc':types.SimpleNamespace(getSkinDir=lambda:skin),
         'wizard_gui':types.SimpleNamespace(Dialog=lambda:types.SimpleNamespace(yesno_required=lambda *a,**kw:touch)),
         'choose_provider':lambda rows,s,t:row if (s,t)==(skin,touch) else None,
         'clear_state':lambda:events.append(('clear',)), 'save_state':lambda s:events.append(('save',s)),
         'queue_switch_theme':lambda:events.append(('theme',))}
    function('finish_touch',env)()
    assert events==([] if touch is None else [('install',row['name'],'force_overwrite',True)]+([('theme',)] if succeed else []))
