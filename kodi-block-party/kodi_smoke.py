"""Local-only Kodi smoke runner. Never shipped in the add-on."""
import json
import os
import sys
import time
import traceback
import xbmc
import xbmcgui
import xbmcvfs

HOME = xbmcvfs.translatePath('special://home/')
ADDON = os.path.join(HOME, 'addons', 'script.boop.blockparty')
sys.path.insert(0, ADDON)

def run():
    results = []
    xbmc.sleep(2500)
    response = xbmc.executeJSONRPC(json.dumps({'jsonrpc':'2.0','id':1,'method':'Addons.SetAddonEnabled', 'params':{'addonid':'script.boop.blockparty','enabled':True}}))
    results.append({'enable': response})
    import default
    win = default.Party('block-party.xml', ADDON, 'Default', '720p')
    win.show()
    monitor = xbmc.Monitor()
    started = time.monotonic()
    stage = 0
    try:
        while not monitor.abortRequested() and time.monotonic()-started < 18:
            elapsed = time.monotonic()-started
            if win.ready:
                while not win.events.empty():
                    win.controller.action(win.events.get_nowait())
                if stage == 0 and elapsed > 1:
                    results.append({'ready':win.controller.mode, 'window':xbmcgui.getCurrentWindowId(), 'controls':len(win.tiles)})
                    xbmc.executebuiltin('Action(Select)')
                    stage = 1
                elif stage == 1 and elapsed > 2:
                    results.append({'select_started':win.controller.mode == 'playing'})
                    xbmc.executebuiltin('Action(Select)')
                    stage = 2
                elif stage == 2 and elapsed > 3:
                    results.append({'select_rotated':win.controller.game.rotation == 1})
                    xbmc.executebuiltin('Action(Left)')
                    stage = 3
                elif stage == 3 and elapsed > 4:
                    results.append({'left_moved':win.controller.game.x == 2})
                    xbmc.executebuiltin('Action(Up)')
                    stage = 4
                elif stage == 4 and elapsed > 5:
                    results.append({'up_dropped':win.controller.game.pieces == 1})
                    # Deterministic presentation fixture, not a game acceptance test.
                    g = win.controller.game
                    for y in range(16,20):
                        for x in range(10):
                            if x != 7:
                                g.board[y][x] = ('J','T','L','S')[(x+y)%4]
                    g.kind,g.rotation,g.x,g.y='I',1,5,1
                    win.draw()
                    xbmc.executebuiltin('TakeScreenshot(' + os.path.join(HOME,'block-party-live.png') + ')')
                    stage = 5
                elif stage == 5 and elapsed > 7:
                    xbmc.executebuiltin('Action(Back)')
                    stage = 6
                elif stage == 6 and elapsed > 8:
                    results.append({'back_paused':win.controller.mode == 'paused'})
                    xbmc.executebuiltin('Action(Select)')
                    stage = 7
                elif stage == 7 and elapsed > 9:
                    results.append({'select_resumed':win.controller.mode == 'playing'})
                    xbmc.executebuiltin('Action(Up)')
                    stage = 8
                elif stage == 8 and elapsed > 10:
                    results.append({'four_line_clear':win.controller.game.lines == 4})
                    xbmc.executebuiltin('Action(Back)')
                    stage = 9
                elif stage == 9 and elapsed > 11:
                    xbmc.executebuiltin('Action(Back)')
                    stage = 10
                elif stage == 10 and elapsed > 12:
                    results.append({'second_back_exit':win.controller.mode == 'exit'})
                    break
                if win.controller.mode == 'playing':
                    win.controller.tick(.025)
                win.draw()
            xbmc.sleep(25)
    finally:
        win.save_best()
        win.close()
    with open(os.path.join(HOME,'block-party-smoke.json'),'w') as f:
        json.dump(results,f,indent=2)

try:
    run()
except Exception:
    with open(os.path.join(HOME,'block-party-smoke-error.txt'),'w') as f:
        f.write(traceback.format_exc())
finally:
    xbmc.executebuiltin('Quit')
