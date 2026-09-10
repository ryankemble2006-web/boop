"""Nonvisual packaging regression for the optional unified HOME workaround."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
A = '{http://schemas.android.com/apk/res/android}'

def test_home_override_is_registered_and_has_minimal_access():
    manifest = ET.parse(ROOT / 'unified/shield-home-manifest.xml')
    services = [s for s in manifest.findall('.//service')
                if s.get(A + 'name') == 'com.boop.alpha1.BoopHomeOverrideService']
    assert len(services) == 1, 'Unified HOME workaround must be registered exactly once'
    service = services[0]
    assert service.get(A + 'permission') == 'android.permission.BIND_ACCESSIBILITY_SERVICE'
    assert service.find('intent-filter/action').get(A + 'name') == 'android.accessibilityservice.AccessibilityService'
    resource = service.find('meta-data').get(A + 'resource').split('/')[-1]
    config = ET.parse(ROOT / 'shield-clean-launcher/app/src/main/res/xml' / (resource + '.xml')).getroot()
    assert config.get(A + 'canRetrieveWindowContent') == 'false'
    assert config.get(A + 'accessibilityEventTypes') == 'typeWindowStateChanged'
    assert config.get(A + 'canPerformGestures') in (None, 'false')

