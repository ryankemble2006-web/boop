import pathlib, shutil, subprocess, tempfile, unittest
ROOT=pathlib.Path(__file__).resolve().parents[1]
class QueueTests(unittest.TestCase):
    def test_native_pcm_queue(self):
        source=ROOT/'native/PcmQueue.h'
        self.assertTrue(source.exists(),'Audio queue has not been implemented')
        with tempfile.TemporaryDirectory() as temp:
            exe=str(pathlib.Path(temp)/'audio-test')
            subprocess.run(['g++','-std=c++17','-pthread','-O2','-I'+str(ROOT/'native'),str(ROOT/'tests/audio_queue.cpp'),'-o',exe],check=True)
            subprocess.run([exe],check=True)
