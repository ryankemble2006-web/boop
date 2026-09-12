"""Check the actual APK archive/dependency boundary, without judging appearance."""
from pathlib import Path
import sys, zipfile
apk=Path(sys.argv[1])
with zipfile.ZipFile(apk) as archive:
    assert archive.testzip() is None, 'Damaged APK archive'
    dex=[archive.read(n) for n in archive.namelist() if n.endswith('.dex')]
    assert dex, 'No executable code in APK'
    for denied in (b'Lcom/boop/alpha1/', b'Lcom/boop/launcher/', b'Lcom/boop/shieldoverlay/', b'HomeAssistantSession', b'sherpa/onnx'):
        assert not any(denied in data for data in dex), 'Unexpected Unified dependency: '+repr(denied)
    for required in (b'Lcom/boop/shieldhome/ShieldLauncherActivity;', b'Lcom/boop/shieldhome/ShieldStartupManagerActivity;', b'Lcom/boop/shieldhome/StartupDefaultsCoordinator;', b'Lcom/boop/shieldhome/LauncherClosePlayerActivity;'):
        assert any(required in data for data in dex), 'Missing launcher component: '+repr(required)
    assert not any(n.startswith('lib/') for n in archive.namelist()), 'Unexpected native voice/runtime library'
print('APK integrity and standalone executable dependency checks PASS')
