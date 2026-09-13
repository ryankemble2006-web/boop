#!/usr/bin/env python3
"""Non-visual timing, ownership, five-device convergence, and notification policy checks."""
from pathlib import Path
import os, shutil, subprocess, tempfile
ROOT=Path(__file__).resolve().parents[1]
java_home=os.environ.get('JAVA_HOME')
suffix='.exe' if os.name=='nt' else ''
javac=str(Path(java_home)/'bin'/('javac'+suffix)) if java_home else 'javac'
java=str(Path(java_home)/'bin'/('java'+suffix)) if java_home else 'java'
files=[*map(lambda n:ROOT/('unified/animation/java/com/boop/eyes/'+n+'.java'),
            ['EyeMotion','EyeCatalogue','PuppetClock','PuppetCoverState','ProductionAnimationController','StyleState']),
       *map(lambda n:ROOT/('source/'+n+'.java'),['BoopVoiceTuning','BoopNotificationSettingsState','BoopNotificationSettingsCodec','BoopNotificationPolicy']),
       *sorted((ROOT/'tests/puppet').glob('*.java'))]
with tempfile.TemporaryDirectory(prefix='boop-puppet-tests-') as out:
    subprocess.run([javac,'-encoding','UTF-8','-d',out,*map(str,files)],check=True)
    for name in ['PuppetCoreTest','BlinkTimingTest','StyleStateTest','com.boop.alpha1.VoiceCharacterTest','com.boop.alpha1.NotificationAppDefaultTest']:
        subprocess.run([java,'-cp',out,name],check=True)
print('Puppet core passed. Physical appearance and device acceptance are separate.')
