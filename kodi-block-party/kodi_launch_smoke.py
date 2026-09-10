"""Exercise the actual shipped entry point, separately from the rules fixture."""
import json
import os
import traceback
import time
import xbmc
import xbmcgui
import xbmcvfs

home = xbmcvfs.translatePath('special://home/')
results = {}

def wait_mode(expected, timeout=20):
    deadline = time.monotonic()+timeout
    while time.monotonic() < deadline:
        current = xbmcgui.getCurrentWindowId()
        if current >= 13000:
            window = xbmcgui.Window(current)
            if window.getProperty('boop.blockparty.ready') == 'true' and window.getProperty('boop.blockparty.mode') == expected:
                return True
        xbmc.sleep(100)
    return False

try:
    xbmc.executebuiltin('RunScript(script.boop.blockparty)')
    results['program_launch'] = wait_mode('ready')
    xbmc.sleep(2500)
    xbmc.executebuiltin('TakeScreenshot(' + os.path.join(home,'block-party-title.png') + ')')
    xbmc.sleep(500)
    xbmc.executebuiltin('Action(Select)')
    results['started'] = wait_mode('playing')
    xbmc.executebuiltin('Action(Select)')
    xbmc.sleep(300)
    xbmc.executebuiltin('Action(Right)')
    xbmc.sleep(300)
    xbmc.executebuiltin('Action(Up)')
    xbmc.sleep(500)
    results['playing_survived'] = xbmcgui.getCurrentWindowId() >= 13000
    xbmc.executebuiltin('Action(Back)')
    results['paused'] = wait_mode('paused')
    xbmc.sleep(2500)
    xbmc.executebuiltin('TakeScreenshot(' + os.path.join(home,'block-party-paused.png') + ')')
    xbmc.sleep(500)
    xbmc.executebuiltin('Action(Select)')
    results['resumed'] = wait_mode('playing')
    xbmc.executebuiltin('Action(Back)')
    results['paused_again'] = wait_mode('paused')
    xbmc.executebuiltin('Action(Back)')
    xbmc.sleep(800)
    results['returned_to_kodi'] = xbmcgui.getCurrentWindowId() == 10000
except Exception:
    results['error'] = traceback.format_exc()
finally:
    with open(os.path.join(home,'block-party-launch-smoke.json'),'w') as f:
        json.dump(results,f,indent=2)
    xbmc.executebuiltin('Quit')
