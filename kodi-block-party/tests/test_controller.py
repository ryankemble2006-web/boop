import sys
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'script.boop.blockparty' / 'resources' / 'lib'))
from controller import Controller


class ControllerTests(unittest.TestCase):
    def test_enter_starts_then_rotates(self):
        c = Controller()
        c.action(7)
        self.assertEqual(c.mode, 'playing')
        c.action(7)
        self.assertEqual(c.game.rotation, 1)

    def test_back_pauses_then_exits(self):
        c = Controller()
        c.action(7)
        c.action(92)
        self.assertEqual(c.mode, 'paused')
        c.action(92)
        self.assertEqual(c.mode, 'exit')

    def test_paused_game_does_not_fall(self):
        c = Controller()
        c.action(7)
        c.action(12)
        y = c.game.y
        c.tick(1)
        self.assertEqual(c.game.y, y)
        c.action(7)
        self.assertEqual(c.mode, 'playing')

    def test_up_hard_drops(self):
        c = Controller()
        c.action(7)
        c.action(3)
        self.assertEqual(c.game.pieces, 1)

    def test_game_over_replay(self):
        c = Controller()
        c.action(7)
        c.game.over = True
        c.tick(0)
        self.assertEqual(c.mode, 'over')
        c.action(7)
        self.assertEqual(c.mode, 'playing')
        self.assertFalse(c.game.over)

    def test_unrelated_actions_do_not_move(self):
        c = Controller()
        c.action(7)
        before = c.game.cells()
        c.action(999)
        self.assertEqual(c.game.cells(), before)
