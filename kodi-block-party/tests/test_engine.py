import random
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'script.boop.blockparty' / 'resources' / 'lib'))
from engine import Game, SHAPES


class EngineTests(unittest.TestCase):
    def game(self):
        return Game(random.Random(42))

    def test_board_and_active_piece(self):
        g = self.game()
        self.assertEqual((len(g.board), len(g.board[0])), (20, 10))
        self.assertEqual(len(set(g.cells())), 4)
        self.assertTrue(g.valid())

    def test_seven_bag(self):
        g = self.game()
        self.assertEqual(len(set([g.kind] + g.queue[:6])), 7)

    def test_walls_block_moves(self):
        g = self.game()
        for _ in range(20):
            g.move(-1)
        cells = g.cells()
        self.assertEqual(min(x for x, _ in cells), 0)
        self.assertFalse(g.move(-1))
        self.assertEqual(g.cells(), cells)

    def test_four_rotations_return_to_same_shape(self):
        for kind in SHAPES:
            g = self.game()
            g.kind, g.rotation, g.x, g.y = kind, 0, 3, 4
            cells = g.cells()
            for _ in range(4):
                self.assertTrue(g.rotate())
            self.assertEqual(g.cells(), cells, kind)

    def test_rotation_at_wall(self):
        g = self.game()
        g.kind, g.rotation, g.x, g.y = 'I', 1, -2, 5
        self.assertTrue(g.valid())
        self.assertTrue(g.rotate())
        self.assertTrue(g.valid())

    def test_rotation_cannot_overlap_board(self):
        g = self.game()
        g.kind, g.rotation, g.x, g.y = 'T', 0, 3, 5
        active = set(g.cells())
        g.board = [['Z' if (x,y) not in active else None for x in range(10)] for y in range(20)]
        self.assertFalse(g.rotate())

    def test_hard_drop_matches_ghost(self):
        g = self.game()
        expected = set(g.cells(y=g.ghost_y()))
        g.hard_drop()
        occupied = {(x,y) for y,row in enumerate(g.board) for x,v in enumerate(row) if v}
        self.assertEqual(occupied, expected)
        self.assertGreater(g.score, 0)

    def test_one_line(self):
        g = self.game()
        g.board[19] = ['J'] * 6 + [None] * 4
        g.kind, g.rotation, g.x, g.y = 'I', 0, 6, 18
        self.assertEqual(g.hard_drop(), 1)
        self.assertEqual(g.lines, 1)
        self.assertEqual(g.score, 100)
        self.assertTrue(all(v is None for row in g.board for v in row))

    def test_four_lines(self):
        g = self.game()
        for y in range(16,20):
            g.board[y] = ['J']*9 + [None]
        g.kind, g.rotation, g.x, g.y = 'I', 1, 7, 16
        self.assertEqual(g.hard_drop(), 4)
        self.assertEqual((g.lines, g.score), (4, 800))

    def test_top_out(self):
        g = self.game()
        g.board[0] = ['J']*10
        g.spawn()
        self.assertTrue(g.over)

    def test_delayed_lock(self):
        g = self.game()
        g.y = g.ghost_y()
        g.tick(.1)
        self.assertEqual(g.pieces, 0)
        g.tick(.5)
        self.assertEqual(g.pieces, 1)

    def test_gravity(self):
        g = self.game()
        y = g.y
        g.tick(g.interval + .01)
        self.assertEqual(g.y, y + 1)

    def test_no_actions_after_game_over(self):
        g = self.game()
        g.over = True
        old = g.cells()
        self.assertFalse(g.move(1))
        self.assertFalse(g.rotate())
        g.hard_drop()
        g.tick(10)
        self.assertEqual(g.cells(), old)

    def test_level_progression(self):
        g = self.game()
        old = g.interval
        g.lines = 10
        self.assertEqual(g.level, 2)
        self.assertLess(g.interval, old)

    def test_long_random_play_preserves_board(self):
        g = self.game()
        rng = random.Random(12)
        for _ in range(600):
            if g.over:
                g = self.game()
            rng.choice([lambda: g.move(-1),lambda:g.move(1),g.rotate,g.hard_drop,lambda:g.tick(.15)])()
            self.assertEqual(len(g.board), 20)
            self.assertTrue(all(len(row)==10 for row in g.board))
            if not g.over:
                self.assertTrue(g.valid())


if __name__ == '__main__':
    unittest.main()
