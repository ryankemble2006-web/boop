#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml')
text = path.read_text(encoding='utf-8')
marker = '<action android:name="android.intent.action.ASSIST" />'
if marker in text:
    print('ACTION_ASSIST eligibility already present')
    raise SystemExit(0)
anchor = '''            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="boop" android:host="auth-callback" />
            </intent-filter>
'''
addition = anchor + '''            <intent-filter>
                <action android:name="android.intent.action.ASSIST" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
'''
if text.count(anchor) != 1:
    raise SystemExit(f'assistant eligibility: expected one MainActivity auth filter, found {text.count(anchor)}')
path.write_text(text.replace(anchor, addition, 1), encoding='utf-8')
print('MainActivity now also qualifies through the official ACTION_ASSIST route')
