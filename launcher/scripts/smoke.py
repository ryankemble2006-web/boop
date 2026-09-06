"""Exercise the installed release APK through real Android input and UI hierarchy."""
import json, re, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
OUT=Path('launcher-evidence'); OUT.mkdir(exist_ok=True)
PKG='com.boop.launcher'
def adb(*args):
    return subprocess.check_output(['adb',*args],text=True).strip()
def shell(*args): return adb('shell',*map(str,args))
def tree():
    for _ in range(3):
        try:
            shell('uiautomator','dump','/sdcard/launcher.xml')
            text=shell('cat','/sdcard/launcher.xml')
            (OUT/'latest-ui.xml').write_text(text)
            return ET.fromstring(text)
        except (ET.ParseError,subprocess.CalledProcessError): time.sleep(1)
    raise AssertionError('No Android UI hierarchy')
def find(label):
    for n in tree().iter('node'):
        if label in (n.get('text'),n.get('content-desc')): return n
    raise AssertionError('Missing control: '+label)
def tap(label,hold=False):
    n=find(label); x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
    x,y=(x1+x2)//2,(y1+y2)//2
    shell('input','swipe',x,y,x,y,900) if hold else shell('input','tap',x,y)
    time.sleep(.7)
def shot(name):
    with (OUT/(name+'.png')).open('wb') as f: subprocess.run(['adb','exec-out','screencap','-p'],stdout=f,check=True)
    (OUT/(name+'.xml')).write_text(ET.tostring(tree(),encoding='unicode'))
def alive(): assert shell('pidof',PKG), 'Launcher process died'
def swipe_up(): shell('input','swipe',720,2400,720,900,350); time.sleep(.5)
checks=[]
try:
    adb('install','-r','launcher-delivery/BOOP-Launcher-Alpha1.apk')
    shell('wm','size','1440x3120'); shell('wm','density','560')
    shell('settings','put','system','accelerometer_rotation','0')
    shell('settings','put','system','user_rotation','0')
    shell('am','start','-W','-n',PKG+'/.MainActivity'); time.sleep(1)
    tap('Start'); alive(); shot('01-home')
    home=shell('cmd','package','resolve-activity','--brief','-a','android.intent.action.MAIN','-c','android.intent.category.HOME','-p',PKG)
    assert PKG in home, home
    shell('cmd','package','set-home-activity',PKG+'/.MainActivity')
    shell('am','start','-a','android.settings.SETTINGS'); shell('input','keyevent','3'); time.sleep(.7)
    find('Home canvas')
    checks.append('Release installs, starts and receives real default HOME dispatch')
    swipe_up(); find('Search apps'); shot('02-drawer')
    tap('Search apps'); shell('input','text','Settings'); shell('input','keyevent','4'); time.sleep(.5)
    tap('Settings',hold=True); find('Done'); tap('Done'); find('Settings'); shot('03-pinned-app')
    checks.append('Drawer search and long-press pin create home shortcut')
    shell('am','force-stop',PKG); shell('am','start','-W','-n',PKG+'/.MainActivity'); time.sleep(.5)
    find('Settings'); checks.append('Pinned shortcut survives process death')
    shell('input','swipe',1250,1800,200,1800,350); time.sleep(.5); shot('04-widgets-page')
    shell('input','keyevent','4'); time.sleep(.5); find('Settings')
    checks.append('Swipe-left widget page and Back return')
    shell('settings','put','system','user_rotation','1'); time.sleep(1); alive(); find('Settings'); shot('05-landscape')
    shell('settings','put','system','user_rotation','0'); shell('wm','size','1344x2992'); shell('wm','density','520'); time.sleep(1)
    alive(); find('Settings'); shot('06-second-screen-size'); checks.append('Rotation and second phone-sized viewport preserve shortcut')
    tap('Home canvas',hold=True); find('Bail out'); shot('07-editor'); tap('Bail out'); time.sleep(.5)
    activity=shell('dumpsys','activity','activities'); (OUT/'bailout-activity.txt').write_text(activity)
    assert 'com.android.settings' in activity, 'Bail out did not open Settings'
    checks.append('Bail out opens Android Settings')
    logs=adb('logcat','-d','-s','AndroidRuntime:E'); (OUT/'runtime-log.txt').write_text(logs)
    assert 'Process: '+PKG not in logs, logs
    (OUT/'checks.json').write_text(json.dumps({'passed':checks},indent=2))
    print('\n'.join('PASS '+c for c in checks))
finally:
    (OUT/'completed-checks.json').write_text(json.dumps(checks,indent=2))
    try: shot('final-state')
    except Exception: pass
    shell('wm','size','reset'); shell('wm','density','reset')
