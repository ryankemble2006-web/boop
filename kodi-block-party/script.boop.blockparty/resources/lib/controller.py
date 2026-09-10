"""Kodi action IDs mapped to explicit game states, without Kodi dependencies."""
from engine import Game


class Controller:
    def __init__(self):
        self.game = Game()
        self.mode = 'ready'
        self.elapsed = 0
        self.event = ''

    def action(self, action):
        self.event = ''
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
            self.game.soft_drop()
        elif action == 3:
            self.game.hard_drop()

    def tick(self, dt):
        if self.mode == 'playing':
            self.elapsed += dt
            self.game.tick(dt)
            if self.game.over:
                self.mode = 'over'
