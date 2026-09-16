#!/usr/bin/env python3
"""Execute the existing v206 nonvisual verification commands, without copying them.

The new workflow supplies the same Java/Android/Gradle environment. This executes
all original run steps before signing, in their original order. Signing and APK
verification then run explicitly for both new application shells.
"""
from pathlib import Path
import subprocess
import yaml

path = Path('.github/workflows/build-boop-v191-hand-colour.yml')
workflow = yaml.safe_load(path.read_text())
ran = []
for step in workflow['jobs']['build']['steps']:
    if step.get('name') == 'Prepare existing permanent BOOP signer':
        break
    if 'run' not in step:
        continue  # Environment actions are provided by the enclosing workflow.
    title = step.get('name', 'Inherited verification')
    print('::group::' + title, flush=True)
    subprocess.run(['bash', '-euo', 'pipefail', '-c', step['run']], check=True)
    print('::endgroup::', flush=True)
    ran.append(title)
else:
    raise AssertionError('Original permanent-signer boundary was not found')
assert len(ran) >= 15, 'Inherited verification steps unexpectedly removed'
Path('split-inherited-checks.txt').write_text('\n'.join(ran) + '\n')
print(f'{len(ran)} existing v206 verification steps passed unchanged in order', flush=True)
