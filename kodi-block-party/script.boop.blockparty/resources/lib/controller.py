"""Kodi action IDs mapped to explicit game states, without Kodi dependencies."""
from engine import Game
import time


class Controller:
    def __init__(self):
        self.game = Game()
        self.mode = 'ready'
        self.elapsed = 0
        self.event = ''
        self.down_started = self.down_last = None
        self.down_repeated = self.down_dropped = False

    def action(self, action, now=None):
        self.event = ''
        now = time.monotonic() if now is None else now
        if action != 4 or self.mode != 'playing':
            self.down_started = self.down_last = None
            self.down_repeated = self.down_dropped = False
        if action in (10, 92, 216, 13):
            self.mode = 'paused' if self.mode == 'playing' else 'exit'
            return
        if action in (12, 68, 79, 229):
            if self.mode == 'playing':
                self.mode = 'paused'
            elif self.mode == 'paused':
                self.mode = 'playing'
            return
        if action in (7, 100):
            if self.mode in ('ready', 'over'):
                self.game = Game()
                self.elapsed = 0
                self.mode = 'playing'
                self.event = 'start'
            elif self.mode == 'paused':
                self.mode = 'playing'
            elif self.mode == 'playing' and self.game.rotate():
                self.event = 'rotate'
            return
        if self.mode != 'playing':
            return
        if action in (1, 2):
            self.game.move(-1 if action == 1 else 1)
        elif action == 4:
            # Kodi Python exposes repeat actions, but no key-up callback.
            # Allow the initial keyboard repeat delay, then expire a hold
            # after a short repeat gap. Never drop multiple pieces per hold.
            gap = .3 if self.down_repeated else .7
            if self.down_last is None or now - self.down_last > gap:
                self.down_started = now
                self.down_repeated = self.down_dropped = False
                self.down_piece = self.game.pieces
            else:
                self.down_repeated = True
            self.down_last = now
            if self.game.pieces != self.down_piece:
                self.down_dropped = True
            if not self.down_dropped:
                if now - self.down_started >= .45:
                    self.game.hard_drop()
                    self.down_dropped = True
                else:
                    self.game.soft_drop()
        elif action == 3:
            self.game.hard_drop()

    def tick(self, dt):
        if self.mode == 'playing':
            self.elapsed += dt
            self.game.tick(dt)
            if self.game.over:
                self.mode = 'over'
