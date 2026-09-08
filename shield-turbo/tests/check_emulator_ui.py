"""Check the installed release APK through a real Android UI hierarchy dump."""
import sys
import xml.etree.ElementTree as ET

nodes = list(ET.parse(sys.argv[1]).iter('node'))
if '--home' in sys.argv:
    labels = {n.get('text', '') for n in nodes}
    expected = {'TURBO', 'PICTURE', 'APPS', 'NETWORK', 'SHIELD'}
    missing = sorted(expected - labels)
    assert not missing, f'Control-centre home cards missing: {missing}'
    focused = [n for n in nodes if n.get('focused') == 'true']
    assert focused, 'Control-centre home has no focused card'
    assert any(n.get('text') == 'TURBO' for n in focused), 'TURBO is not the initial focused card'
    print('EMULATOR_CONTROL_CENTRE_HOME=PASS')
elif '--focus' in sys.argv:
    focused = [n for n in nodes if n.get('focused') == 'true']
    assert focused, 'D-pad has no focused control'
    assert any(n.get('content-desc', '').startswith('Reading:') for n in focused), 'D-pad did not reach a result card'
    print('EMULATOR_DPAD_CARD_FOCUS=PASS')
else:
    status = next((n for n in nodes if n.get('resource-id', '').endswith('/analysis_status')), None)
    assert status is not None, 'Scan status is missing'
    assert status.get('text', '').startswith('Scan complete'), 'Centre button did not complete an analysis'
    assert any(n.get('content-desc', '').startswith('Reading:') for n in nodes), 'No measurement cards rendered'
    print('EMULATOR_RELEASE_ANALYSIS=PASS')
