from pathlib import Path
import subprocess
import pytest
ROOT=Path(__file__).resolve().parents[1]
OVERLAY=ROOT/'shield-overlay/app/src/main/java/com/boop/shieldoverlay'
HOME=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'

@pytest.fixture(scope='module')
def classes(tmp_path_factory):
    assert (OVERLAY/'RoomPanelController.java').is_file(), 'Launcher room state controller is not implemented'
    target=tmp_path_factory.mktemp('room-panel-java')
    files=[OVERLAY/(n+'.java') for n in ['RoomPanelController','AreaInfo','EntityCard','DashboardSnapshot','RoomScopedEntities','RoomDeviceControls']]
    files += [ROOT/'tests/room-panel/RoomPanelControllerProbe.java']
    run=subprocess.run(['javac','-d',str(target),*map(str,files)],capture_output=True,text=True)
    assert run.returncode==0,run.stderr
    return target

@pytest.mark.parametrize('case', ['no-room','confirmed-toggle','no-optimistic-success','duplicate-click','external-state','unknown-entity','unavailable','stop-stale-click','room-change','room-change-before-click','entity-moved-before-click','wrong-room-snapshot','state-race','offline-retry','auth-no-retry','load-timeout','action-timeout','cancel-validation','hidden-device','late-confirmation'])
def test_room_lifecycle(classes,case):
    run=subprocess.run(['java','-cp',str(classes),'com.boop.shieldoverlay.RoomPanelControllerProbe',case],capture_output=True,text=True,timeout=5)
    assert run.returncode==0,run.stdout+run.stderr

def test_source_wiring():
    home=(HOME/'ShieldHomeView.java').read_text()
    activity=(HOME/'ShieldLauncherActivity.java').read_text()
    settings=(HOME/'ShieldHomeSettingsView.java').read_text()
    assert 'ShieldRoomPanelView' in home
    assert 'setVisibilityListener' in home
    assert 'RoomPanelLayout' in home
    assert 'roomPanelSession' in activity
    assert 'roomPanelSession.stop()' in activity
    assert 'smartHomePanelEnabled' in activity
    assert 'Smart home panel: ' in settings

def test_layout_arithmetic(tmp_path):
    path=HOME/'RoomPanelLayout.java'
    assert path.exists(), 'Adaptive panel geometry is not implemented'
    run=subprocess.run(['javac','-d',str(tmp_path),str(path),str(ROOT/'tests/room-panel/RoomPanelLayoutProbe.java')],capture_output=True,text=True)
    assert run.returncode==0,run.stderr
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.shieldhome.RoomPanelLayoutProbe'],check=True)
