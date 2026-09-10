import sys
import unittest
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'script.boop.blockparty' / 'resources' / 'lib'))
from controller import Controller


class ControllerTests(unittest.TestCase):
    def test_hold_does_not_drop_next_piece_after_natural_lock(self):
        c = Controller()
        c.action(7)
        c.game.y = c.game.ghost_y()
        c.action(4, now=0)
        c.tick(.5)
        c.action(4, now=.5)
        self.assertEqual(c.game.pieces, 1)
        self.assertEqual(c.game.y, 0)

    def test_down_tap_moves_one_row(self):
        c = Controller()
        c.action(7)
        y = c.game.y
        c.action(4, now=0)
        self.assertEqual(c.game.y, y + 1)
        self.assertEqual(c.game.pieces, 0)

    def test_down_hold_drops_only_one_piece(self):
        c = Controller()
        c.action(7)
        for t in (0, .3, .4, .5, .6, .7, .8, .9):
            c.action(4, now=t)
        self.assertEqual(c.game.pieces, 1)
        self.assertEqual(c.game.y, 0)

    def test_down_new_press_after_release_can_drop_again(self):
        c = Controller()
        c.action(7)
        for t in (0, .5, .6, 1.0, 1.5):
            c.action(4, now=t)
        self.assertEqual(c.game.pieces, 2)

    def test_separate_down_taps_do_not_count_as_hold(self):
        c = Controller()
        c.action(7)
        for t in (0, 1, 2):
            c.action(4, now=t)
        self.assertEqual(c.game.pieces, 0)
        self.assertEqual(c.game.y, 3)

    def test_pause_cancels_pending_down_hold(self):
        c = Controller()
        c.action(7)
        c.action(4, now=0)
        c.action(92)
        c.action(7)
        c.action(4, now=.5)
        self.assertEqual(c.game.pieces, 0)

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
