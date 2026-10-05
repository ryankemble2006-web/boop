"""Add the isolated TV sender to the already materialized signed build."""
from pathlib import Path
import shutil

root=Path('boop-build/BOOP-Alpha1')
assert (root/'split-app-shell.gradle').is_file(), 'Run split materialization first'
target=root/'notification-sender'
assert not target.exists(), 'Sender already materialized'
shutil.copytree('notification-lab',target)
java=target/'src/main/java/com/boop/alpha1'
for name in ('BoopPreviewProtocol.java','BoopPreviewClient.java','BoopPreviewSendGate.java',
             'BoopLabFocus.java','BoopLabAccent.java'):
    shutil.copyfile(Path('source')/name,java/name)
with (root/'settings.gradle').open('a') as handle:
    handle.write("\ninclude ':notification-sender'\n")
print('Notification sender uses the same bounded protocol; no art or app data copied')
