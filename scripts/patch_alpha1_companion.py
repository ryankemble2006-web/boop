#!/usr/bin/env python3
from pathlib import Path
import sys


INTERNET_PERMISSION = '    <uses-permission android:name="android.permission.INTERNET" />\n'
ACTIVITY_BLOCK = '''\n        <activity\n            android:name=".ShieldPairingActivity"\n            android:exported="true"\n            android:excludeFromRecents="true"\n            android:launchMode="singleTask">\n            <intent-filter>\n                <action android:name="android.intent.action.VIEW" />\n                <category android:name="android.intent.category.DEFAULT" />\n                <category android:name="android.intent.category.BROWSABLE" />\n                <data android:scheme="boop" android:host="shield-pair" />\n                <data android:scheme="boop" android:host="shield-pair-return" />\n            </intent-filter>\n        </activity>\n'''


def patch_manifest(path: Path) -> None:
    text = path.read_text(encoding="utf-8")

    if 'android.permission.INTERNET' not in text:
        marker = '<manifest xmlns:android="http://schemas.android.com/apk/res/android">\n'
        if marker not in text:
            raise RuntimeError("Could not find manifest root")
        text = text.replace(marker, marker + '\n' + INTERNET_PERMISSION, 1)

    if 'android:name=".ShieldPairingActivity"' not in text:
        marker = '</application>'
        if marker not in text:
            raise RuntimeError("Could not find application end tag")
        text = text.replace(marker, ACTIVITY_BLOCK + '    ' + marker, 1)

    path.write_text(text, encoding="utf-8")


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("usage: patch_alpha1_companion.py <AndroidManifest.xml>")
    patch_manifest(Path(sys.argv[1]))


if __name__ == "__main__":
    main()
