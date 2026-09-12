import pathlib, subprocess, tempfile, unittest, zipfile, importlib.util
ROOT=pathlib.Path(__file__).resolve().parents[1]
class PolicyTests(unittest.TestCase):
    def test_java_policy_on_real_bundles(self):
        source=ROOT/'java/com/boop/rally/GameBundle.java'
        self.assertTrue(source.exists(), 'ZIP import policy has not been implemented')
        spec=importlib.util.spec_from_file_location('pack',ROOT/'tools/prepare_games.py')
        m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
        with tempfile.TemporaryDirectory() as t:
            p=pathlib.Path(t);(p/'game').mkdir();(p/'game/RALLY.EXE').write_bytes(b'MZ-game')
            m.make_bundle(p/'game',p/'good.zip','rac93')
            with zipfile.ZipFile(p/'bad.zip','w') as z:z.writestr('../RALLY.EXE',b'MZ-game')
            subprocess.run(['javac','-d',t,str(source),str(ROOT/'java/com/boop/rally/GameSpec.java'),str(ROOT/'tests/BundleHarness.java')],check=True)
            subprocess.run(['java','-cp',t,'com.boop.rally.BundleHarness',str(p/'good.zip'),str(p/'bad.zip')],check=True)
