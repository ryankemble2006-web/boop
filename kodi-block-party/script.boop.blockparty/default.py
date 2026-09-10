"""BOOP Block Party: a native, offline Kodi program add-on."""
import json
import math
import os
import queue
import sys
import time
import traceback

import xbmc
import xbmcaddon
import xbmcgui
import xbmcvfs

ADDON = xbmcaddon.Addon('script.boop.blockparty')
ROOT = xbmcvfs.translatePath(ADDON.getAddonInfo('path'))
MEDIA = os.path.join(ROOT, 'resources', 'media')
sys.path.insert(0, os.path.join(ROOT, 'resources', 'lib'))
from controller import Controller
from engine import shape_cells


class Party(xbmcgui.WindowXML):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.controller = Controller()
        self.events = queue.Queue(maxsize=64)
        self.ready = False
        self.tiles = []
        self.tile_state = [None] * 200
        self.preview = []
        self.overlay = []
        self.best = 0
        self.stored_best = 0
        self.last_pieces = 0
        self.last_mode = ''
        self.message_until = 0
        self.labels = {}
        self.label_values = {}
        self.blink_height = 124
        self.profile = xbmcvfs.translatePath(ADDON.getAddonInfo('profile'))
        self.score_path = os.path.join(self.profile, 'best.json')
        try:
            with open(self.score_path, encoding='utf-8') as f:
                self.best = max(0, int(json.load(f).get('best', 0)))
            self.stored_best = self.best
        except (OSError, ValueError, TypeError, AttributeError):
            pass

    def image(self, x, y, w, h, filename):
        control = xbmcgui.ControlImage(x, y, w, h, os.path.join(MEDIA, filename))
        self.addControl(control)
        return control

    def label(self, key, x, y, w, h, font='font30', color='FFF0F6F6', align=0):
        control = xbmcgui.ControlLabel(x, y, w, h, '', font=font, textColor=color, alignment=align)
        self.addControl(control)
        self.labels[key] = control
        return control

    def text(self, key, value):
        if self.label_values.get(key) != value:
            self.labels[key].setLabel(value)
            self.label_values[key] = value

    def onInit(self):
        if self.ready:
            return
        self.image(0, 0, 1280, 720, 'cabinet.png')
        self.eyes = self.image(94, 354, 248, 124, 'boopApprovedEyes.png')
        for y in range(20):
            for x in range(10):
                self.tiles.append(xbmcgui.ControlImage(497 + x*28, 89 + y*28, 33, 33, os.path.join(MEDIA, 'empty.png')))
        self.addControls(self.tiles)
        for i in range(12):
            self.preview.append(self.image(900, 390, 27, 27, 'empty.png'))
        self.label('score', 886, 138, 314, 57)
        self.label('best', 74, 566, 280, 39)
        self.label('level', 886, 269, 130, 46)
        self.label('lines', 1074, 269, 130, 46)
        self.label('message', 475, 41, 340, 24, 'font13', 'FF73E9ED', 2)
        for mode in ('ready', 'paused', 'over'):
            card = self.image(410, 245, 460, 245, mode + '.png')
            card.setVisibleCondition('String.IsEqual(Window.Property(boop.blockparty.mode),' + mode + ')')
            self.overlay.append(card)
        self.setFocusId(9000)
        self.window_id = xbmcgui.getCurrentWindowId()
        self.ready = True

    def onAction(self, action):
        try:
            self.events.put_nowait(action.getId())
        except queue.Full:
            # Back must remain an escape even during a key-repeat storm.
            if action.getId() in (10, 92, 216, 13):
                try:
                    self.events.get_nowait()
                    self.events.put_nowait(action.getId())
                except queue.Empty:
                    pass

    def play(self, name):
        try:
            xbmc.playSFX(os.path.join(MEDIA, name + '.wav'), False)
        except Exception:
            pass  # Sound is decoration; never break the controls for it.

    def save_best(self):
        self.best = max(self.best, self.controller.game.score)
        if self.best <= self.stored_best:
            return
        try:
            os.makedirs(self.profile, exist_ok=True)
            tmp = self.score_path + '.tmp'
            with open(tmp, 'w', encoding='utf-8') as f:
                json.dump({'best': self.best}, f)
            os.replace(tmp, self.score_path)
            self.stored_best = self.best
        except OSError:
            xbmc.log('BOOP Block Party: best score could not be saved', xbmc.LOGWARNING)

    def draw(self):
        c, g = self.controller, self.controller.game
        self.best = max(self.best, g.score)
        self.text('score', '{:06,d}'.format(g.score))
        self.text('best', '{:,}'.format(self.best))
        self.text('level', '{:02}'.format(g.level))
        self.text('lines', '{:02}'.format(g.lines))
        state = [value or 'empty' for row in g.board for value in row]
        if not g.over:
            for x,y in g.cells(y=g.ghost_y()):
                if 0 <= y < 20:
                    state[y*10+x] = 'ghost'
            for x,y in g.cells():
                if 0 <= y < 20:
                    state[y*10+x] = g.kind
        for index,value in enumerate(state):
            if self.tile_state[index] != value:
                self.tiles[index].setImage(os.path.join(MEDIA,value+'.png'))
                self.tile_state[index] = value
        preview_key = tuple(g.queue[:3])
        if getattr(self, 'preview_key', None) != preview_key:
            for i,kind in enumerate(preview_key):
                cells = shape_cells(kind)
                minx, miny = min(x for x,y in cells), min(y for x,y in cells)
                maxx = max(x for x,y in cells)
                offset = (4 - (maxx-minx+1))*10
                for j,(x,y) in enumerate(cells):
                    tile = self.preview[i*4+j]
                    tile.setPosition(884+i*110+offset+(x-minx)*22, 386+(y-miny)*22)
                    tile.setImage(os.path.join(MEDIA,kind+'.png'))
            self.preview_key = preview_key
        if g.pieces != self.last_pieces:
            self.last_pieces = g.pieces
            if g.pieces:
                self.play('clear' if g.last_clear else 'lock')
                if g.last_clear:
                    self.text('message', ('', 'TIDY!', 'DOUBLE!', 'TRIPLE!', 'BOOP! FOUR LINES!')[g.last_clear])
                    self.message_until = time.monotonic()+1.7
        if time.monotonic() > self.message_until:
            self.text('message', '')
        if c.mode != self.last_mode:
            self.last_mode = c.mode
            if c.mode in ('ready','paused','over'):
                self.save_best()
                if c.mode == 'over':
                    self.play('over')
        # Non-destructive display-only blink of the approved original asset.
        phase = time.monotonic() % 5.7
        height = max(5, int(124 * abs(phase-.13)/.13)) if phase < .26 else 124
        if height != self.blink_height:
            self.eyes.setHeight(height)
            self.eyes.setPosition(94, 416-height//2)
            self.blink_height = height
        self.setProperty('boop.blockparty.mode', c.mode)
        self.setProperty('boop.blockparty.ready', 'true')

    def run(self):
        monitor = xbmc.Monitor()
        self.show()
        previous = time.monotonic()
        try:
            while not monitor.abortRequested() and self.controller.mode != 'exit':
                now = time.monotonic()
                dt, previous = min(.1, now-previous), now
                if self.ready:
                    # Another Kodi window taking over pauses play rather than losing it.
                    if xbmcgui.getCurrentWindowId() != self.window_id and self.controller.mode == 'playing':
                        self.controller.mode = 'paused'
                    for _ in range(32):
                        try:
                            self.controller.action(self.events.get_nowait())
                            if self.controller.event:
                                self.play(self.controller.event)
                        except queue.Empty:
                            break
                    self.controller.tick(dt)
                    if self.controller.mode != 'exit':
                        self.draw()
                if monitor.waitForAbort(1/30):
                    break
        finally:
            self.save_best()
            self.close()


def main():
    game = None
    try:
        game = Party('block-party.xml', ROOT, 'Default', '720p')
        game.run()
    except Exception:
        xbmc.log('BOOP Block Party: ' + traceback.format_exc(), xbmc.LOGERROR)
        xbmcgui.Dialog().ok('BOOP - Block Party', 'The game could not start. Details are in the Kodi log.')
    finally:
        if game is not None:
            game.close()
        del game


if __name__ == '__main__':
    main()
