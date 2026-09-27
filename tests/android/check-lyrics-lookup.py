"""Exercise real TV D-pad/IME editing in the isolated emulator fixture only."""
from pathlib import Path
import argparse, subprocess, time, xml.etree.ElementTree as ET

p=argparse.ArgumentParser()
p.add_argument('--adb',required=True);p.add_argument('--serial',default='emulator-5554')
p.add_argument('--output',required=True);args=p.parse_args()
assert args.serial.startswith('emulator-'), 'This fixture must never operate a physical device'
out=Path(args.output);out.mkdir(parents=True,exist_ok=True)
package='com.boop.seren.preview'
def adb(*cmd):return subprocess.check_output([args.adb,'-s',args.serial,*cmd])
def key(*codes):adb('shell','input','keyevent',*[str(c) for c in codes]);time.sleep(.25)
def tree(name):
    adb('shell','uiautomator','dump','/sdcard/lyrics-lookup-fixture.xml')
    data=adb('exec-out','cat','/sdcard/lyrics-lookup-fixture.xml');(out/(name+'.xml')).write_bytes(data)
    return ET.fromstring(data)
def focused(root,text):return any(n.get('text')==text and n.get('focused')=='true' for n in root.iter('node'))
def contains(root,text):return any(n.get('text')==text for n in root.iter('node'))
adb('shell','am','force-stop',package)
adb('shell','am','start','-W','-n',package+'/com.boop.shieldhome.LyricsLookupFixture')
time.sleep(.5)
assert contains(tree('screen'),'Lookup')
key(20);key(22)
assert focused(tree('lookup-focused'),'Lookup'), 'D-pad must reach Lookup from Queue'
key(23)
assert focused(tree('editor'),'Example Song (Remastered 2020)')
(out/'editor.png').write_bytes(adb('exec-out','screencap','-p'))
key(23)
key(*([67]*len(' (Remastered 2020)')))
key(4)
key(20);key(20)
assert focused(tree('search-focused'),'Search'), 'Down from text fields must reach Search'
key(23)
assert contains(tree('edited-result'),'Search: Example Song / Example Artist')
(out/'edited-result.png').write_bytes(adb('exec-out','screencap','-p'))
key(23)
assert contains(tree('reopened'),'Lyrics lookup')
key(*([67]*len('Example Song (Remastered 2020)')))
key(20);key(20);key(23)
empty=tree('empty-validation')
assert contains(empty,'Lyrics lookup') and focused(empty,''), 'Empty title must remain editable'
key(4)
assert contains(tree('cancelled'),'Search: Example Song / Example Artist'), 'Cancel preserves displayed result'
adb('shell','rm','/sdcard/lyrics-lookup-fixture.xml')
print('PASS: emulator D-pad, keyboard suffix removal, Search, empty validation and Cancel')
