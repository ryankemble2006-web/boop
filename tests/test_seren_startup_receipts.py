from pathlib import Path
import ast,sys,types

def test_startup_signals():
    service=(Path(__file__).resolve().parents[1]/'integrations/seren/service.py').read_text(encoding='utf-8')
    module=ast.parse(service)
    function=next(n for n in module.body if isinstance(n,ast.FunctionDef) and n.name=='sync_trakt_at_startup')
    for outcome in ('done','unchanged','failed','no_auth','exception'):
        properties={}; calls=[]; waits=[]
        class Globals:
            ADDON_NAME='Seren'
            def wait_for_abort(self,n): waits.append(n); return False
            def get_setting(self,*a): return '' if outcome=='no_auth' else 'auth'
            def notification(self,*a,**k): pass
            def log(self,*a): pass
        class DB:
            startup_sync_failed=outcome=='failed';startup_sync_skipped=False;startup_sync_changed=1 if outcome=='done' else 0
            def sync_activities(self,**kw):
                calls.append(kw)
                if outcome=='exception':raise RuntimeError('network')
                return outcome=='failed'
        setup=types.ModuleType('resources.lib.modules.setup_flow');setup.process_pending_rebuild=lambda:False
        activities=types.ModuleType('resources.lib.database.trakt_sync.activities');activities.TraktSyncDatabase=DB
        sys.modules[setup.__name__]=setup;sys.modules[activities.__name__]=activities
        window=types.SimpleNamespace(setProperty=lambda k,v:properties.__setitem__(k,v))
        env={'g':Globals(),'xbmcgui':types.SimpleNamespace(Window=lambda n:window), 'startup_notification':lambda *a,**kw:None}
        exec(compile(ast.Module(body=[function],type_ignores=[]),'<startup>','exec'),env)
        env['sync_trakt_at_startup']()
        expected='failed' if outcome=='exception' else outcome
        assert properties['BOOP.TraktStartupSync']==expected,(outcome,properties)
        assert len(calls)==(0 if outcome=='no_auth' else 1),'duplicate sync'
        assert waits == [], 'startup sync still waits for a fixed splash timer'
    print('Seren startup receipts: done, failure, no auth, exception; one sync per startup passed')
    
