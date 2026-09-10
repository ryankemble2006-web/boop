"""Dependency-free falling-block rules; rendering and Kodi live elsewhere."""
import random

SHAPES = {
    'I': ((0, 1), (1, 1), (2, 1), (3, 1)),
    'O': ((1, 0), (2, 0), (1, 1), (2, 1)),
    'T': ((1, 0), (0, 1), (1, 1), (2, 1)),
    'J': ((0, 0), (0, 1), (1, 1), (2, 1)),
    'L': ((2, 0), (0, 1), (1, 1), (2, 1)),
    'S': ((1, 0), (2, 0), (0, 1), (1, 1)),
    'Z': ((0, 0), (1, 0), (1, 1), (2, 1)),
}


def shape_cells(kind, rotation=0):
    cells = SHAPES[kind]
    size = 4 if kind == 'I' else 3
    if kind != 'O':
        for _ in range(rotation % 4):
            cells = tuple((size - 1 - y, x) for x, y in cells)
    return cells


class Game:
    width, height = 10, 20

    def __init__(self, rng=None):
        self.rng = rng or random.Random()
        self.board = [[None] * self.width for _ in range(self.height)]
        self.queue = []
        self.score = self.lines = self.pieces = 0
        self.over = False
        self.combo = -1
        self.last_clear = 0
        self.spawn()

    @property
    def level(self):
        return self.lines // 10 + 1

    @property
    def interval(self):
        return max(.075, .75 * .82 ** (self.level - 1))

    def spawn(self):
        while len(self.queue) < 8:
            bag = list(SHAPES)
            self.rng.shuffle(bag)
            self.queue.extend(bag)
        self.kind = self.queue.pop(0)
        self.x, self.y, self.rotation = 3, 0, 0
        self.fall_time = self.ground_time = 0.0
        self.lock_resets = 0
        if not self.valid():
            self.over = True

    def cells(self, x=None, y=None, rotation=None):
        x = self.x if x is None else x
        y = self.y if y is None else y
        r = self.rotation if rotation is None else rotation
        return tuple((x + cx, y + cy) for cx, cy in shape_cells(self.kind, r))

    def valid(self, x=None, y=None, rotation=None):
        return all(0 <= cx < self.width and 0 <= cy < self.height
                   and self.board[cy][cx] is None
                   for cx, cy in self.cells(x, y, rotation))

    def _reset_lock(self, was_grounded):
        if was_grounded and self.lock_resets < 15:
            self.ground_time = 0
            self.lock_resets += 1

    def move(self, dx):
        if self.over or not self.valid(x=self.x + dx):
            return False
        grounded = not self.valid(y=self.y + 1)
        self.x += dx
        self._reset_lock(grounded)
        return True

    def rotate(self):
        if self.over:
            return False
        r = (self.rotation + 1) % 4
        grounded = not self.valid(y=self.y + 1)
        # Simple wall/floor kicks. This game does not claim Guideline SRS.
        for dx, dy in ((0, 0), (-1, 0), (1, 0), (-2, 0), (2, 0), (0, -1), (0, -2)):
            if self.valid(self.x + dx, self.y + dy, r):
                self.x += dx
                self.y += dy
                self.rotation = r
                self._reset_lock(grounded)
                return True
        return False

    def soft_drop(self):
        if self.over or not self.valid(y=self.y + 1):
            return False
        self.y += 1
        self.score += 1
        self.fall_time = 0
        return True

    def ghost_y(self):
        y = self.y
        while self.valid(y=y + 1):
            y += 1
        return y

    def hard_drop(self):
        if self.over:
            return 0
        landing = self.ghost_y()
        self.score += 2 * (landing - self.y)
        self.y = landing
        return self.lock()

    def lock(self):
        for x, y in self.cells():
            self.board[y][x] = self.kind
        remaining = [row for row in self.board if not all(row)]
        cleared = self.height - len(remaining)
        self.combo = self.combo + 1 if cleared else -1
        self.score += (0, 100, 300, 500, 800)[cleared] * self.level
        if cleared:
            self.score += max(0, self.combo) * 50 * self.level
        self.lines += cleared
        self.board = [[None] * self.width for _ in range(cleared)] + remaining
        self.pieces += 1
        self.last_clear = cleared
        self.spawn()
        return cleared

    def tick(self, dt):
        if self.over:
            return
        dt = max(0, min(dt, 1.0))
        grounded = not self.valid(y=self.y + 1)
        if grounded:
            self.ground_time += dt
            if self.ground_time >= .45:
                self.lock()
            return
        self.ground_time = 0
        self.fall_time += dt
        while self.fall_time >= self.interval:
            self.fall_time -= self.interval
            if self.valid(y=self.y + 1):
                self.y += 1
            else:
                break
