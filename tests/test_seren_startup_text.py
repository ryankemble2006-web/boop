import ast
from pathlib import Path
import types

BASE = Path('C:/Users/ryank/Desktop/codex workspace/shield-spectrum/source/integrations/seren')

def test_silent_sync_uses_existing_progress_text():
    path = BASE/'activities.py'
    tree = ast.parse(path.read_text(encoding='utf-8'))
    cls = next(n for n in tree.body if isinstance(n, ast.ClassDef) and n.name == 'TraktSyncDatabase')
    method = next(n for n in cls.body if isinstance(n, ast.FunctionDef) and n.name == '_update_progress')
    props = {'BOOP.TraktStartupSync':'running'}
    window = types.SimpleNamespace(getProperty=lambda k:props.get(k,''), setProperty=lambda k,v:props.__setitem__(k,v))
    env = {'xbmcgui':types.SimpleNamespace(Window=lambda n:window)}
    exec(compile(ast.Module(body=[method],type_ignores=[]), '<progress>', 'exec'), env)
    obj = types.SimpleNamespace(silent=True,current_dialog_text='Syncing watched episodes')
    env['_update_progress'](obj,25)
    assert props.get('Seren.StartupText') == obj.current_dialog_text
    env['_update_progress'](obj,50,'Syncing collected movies')
    assert props['Seren.StartupText'] == 'Syncing collected movies'
    props['BOOP.TraktStartupSync']='done'
    env['_update_progress'](obj,100,'Later manual sync')
    assert props['Seren.StartupText'] == 'Syncing collected movies'

def test_managed_skin_receives_exact_notification_without_duplicate_toast():
    tree = ast.parse((BASE/'service.py').read_text(encoding='utf-8'))
    fn = next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='startup_notification')
    props={'Seren.StartupSkinManaged':'true'}; notices=[]
    window=types.SimpleNamespace(getProperty=lambda k:props.get(k,''),setProperty=lambda k,v:props.__setitem__(k,v))
    env={'xbmcgui':types.SimpleNamespace(Window=lambda n:window,getCurrentWindowId=lambda:12999),
         'g':types.SimpleNamespace(notification=lambda *a,**k:notices.append(a))}
    exec(compile(ast.Module(body=[fn],type_ignores=[]),'<notice>','exec'),env)
    env['startup_notification']('Seren','Startup sync complete — already up to date')
    assert props.get('Seren.StartupText') == 'Startup sync complete — already up to date'
    assert not notices
    props['Seren.StartupText'] = 'Loading Home'
    env['startup_notification']('Seren','Startup sync complete — already up to date')
    assert props['Seren.StartupText'] == 'Loading Home'

def test_all_skin_loaders_read_live_text():
    import xml.etree.ElementTree as ET
    root=Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source')
    for skin in ('skin.NTVaura','skin.Eden','skin.aura.liquid'):
        startup=root/skin/'1080i/Startup.xml'
        ET.parse(startup)
        loader = startup if skin != 'skin.aura.liquid' else root/skin/'1080i/Includes_LiquidStartup.xml'
        ET.parse(loader)
        assert '$INFO[Window(Home).Property(Seren.StartupText)]' in loader.read_text(encoding='utf-8')
