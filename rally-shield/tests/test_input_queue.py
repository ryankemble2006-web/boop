import pathlib
import tempfile
import subprocess
import unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]
class InputTests(unittest.TestCase):
    def test_short_taps_survive_between_frames(self):
        self.assertTrue((ROOT/'native/InputQueue.h').exists(),'Native input event queue is missing')
        with tempfile.TemporaryDirectory() as d:
            exe=pathlib.Path(d)/'input-test'
            subprocess.run(['g++','-std=c++17','-pthread','-I'+str(ROOT/'native'),str(ROOT/'tests/input_queue.cpp'),'-o',str(exe)],check=True)
            subprocess.run([str(exe)],check=True)
