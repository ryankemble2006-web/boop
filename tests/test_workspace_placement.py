"""Exercise actual Home placement decisions without an Android runtime."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_widget_bounds_and_icon_alignment(tmp_path):
    stub = tmp_path / 'ComponentName.java'
    stub.write_text('package android.content; public class ComponentName { '
                    'public static ComponentName unflattenFromString(String s) { return null; } }')
    source = ROOT / 'launcher/app/src/main/java/com/boop/launcher'
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path), str(stub),
                    str(source / 'WorkspaceItem.java'), str(source / 'WorkspacePlacement.java'),
                    str(ROOT / 'tests/java/WorkspacePlacementProbe.java')], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.launcher.WorkspacePlacementProbe'], check=True)
