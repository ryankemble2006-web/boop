import runpy
import sys
import types
import time
from pathlib import Path
import pytest

SKINS = Path('C:/Users/ryank/Desktop/codex workspace/2026-10-07/cod/work/optimization/source')

@pytest.mark.parametrize('skin', ['skin.NTVaura', 'skin.Eden', 'skin.aura.liquid'])
@pytest.mark.parametrize('case', ['absent', 'no_auth', 'sync', 'legacy', 'failed', 'abort'])
def test_loading_screen_handoff(monkeypatch, skin, case):
    clock = [0.0]; current = [12999]; actions = []; notices = []; props = {}
    monkeypatch.setattr(time, 'monotonic', lambda: clock[0])
    class Monitor:
        def abortRequested(self): return case == 'abort' and clock[0] >= .3
        def waitForAbort(self, delay):
            clock[0] += delay
            return self.abortRequested()
    def receipt(key):
        if key == 'Seren.StartupSkinManaged': return 'true'
        if case == 'legacy': return ''
        return ('failed' if case == 'failed' else 'done') if clock[0] >= .6 else 'running'
    def execute(action):
        actions.append((clock[0], action)); current[0] = 10000
    xbmc = types.SimpleNamespace(Monitor=Monitor, LOGINFO=1,
        getInfoLabel=lambda label: '10000', log=lambda *a: None,
        getCondVisibility=lambda condition: case != 'absent' if condition == 'System.HasAddon(plugin.video.seren)' else False,
        executebuiltin=execute)
    gui = types.SimpleNamespace(getCurrentWindowId=lambda: current[0],
        Window=lambda n: types.SimpleNamespace(getProperty=receipt,setProperty=lambda k,v:props.__setitem__(k,v)),
        Dialog=lambda: types.SimpleNamespace(notification=lambda *a, **kw: notices.append((clock[0], a, kw))))
    addon = types.SimpleNamespace(Addon=lambda name: types.SimpleNamespace(getSetting=lambda key: '' if case == 'no_auth' else 'token'))
    for name, value in [('xbmc', xbmc), ('xbmcgui', gui), ('xbmcaddon', addon)]:
        monkeypatch.setitem(sys.modules, name, value)
    namespace = runpy.run_path(str(SKINS / skin / 'startup_handoff.py'))
    namespace['run']()
    if case == 'abort':
        assert not actions
    else:
        assert len(actions) == 1
        assert props['Seren.StartupText'] == 'Loading Home'
        if case == 'absent': assert actions[0][0] == 0, 'absent Seren must add no delay'
        if case in ('sync', 'failed'): assert actions[0][0] >= .6, 'Home opened before sync completed'
        if case == 'legacy': assert 10 <= actions[0][0] < 11, 'old Seren must not hang loading'
        if case == 'no_auth':
            assert len(notices) == 1
            assert notices[0][1][1] == 'Not authed, continuing'
            assert notices[0][2]['time'] == 1000
            assert actions[0][0] == 1
