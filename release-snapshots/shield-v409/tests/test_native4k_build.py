import importlib.util,unittest,zipfile,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
spec=importlib.util.spec_from_file_location('builder',ROOT/'build_native4k.py');b=importlib.util.module_from_spec(spec);spec.loader.exec_module(b)
class BuildSafetyTests(unittest.TestCase):
 def test_manifest_changes_only_two_version_bytes(self):
  with zipfile.ZipFile(ROOT.parent/'shield-v408/BOOP-Shield-v408.apk') as z:data=z.read('AndroidManifest.xml')
  result=b.patch_manifest(data);self.assertEqual(len(result),len(data));self.assertEqual(sum(x!=y for x,y in zip(result,data)),2)
  with self.assertRaises(ValueError):b.patch_manifest(result)
 def test_unknown_or_duplicate_launcher_methods_are_rejected(self):
  original=(ROOT/'tests/baseline-config-method.smali').read_text(encoding='utf-8')
  for text in ['',original+'\n'+original,original.replace('reapplyLayoutDimensions','otherMethod')]:
   with self.assertRaises(ValueError):b.patch_method(text)
 def test_only_configuration_method_changes_and_has_lifecycle_guards(self):
  original=(ROOT/'tests/baseline-config-method.smali').read_text(encoding='utf-8');before='.class public Lexample;\n'+original+'\n.method private sentinel()V\n return-void\n.end method\n';after=b.patch_method(before)
  self.assertEqual(re.sub(b.METHOD,'',before),re.sub(b.METHOD,'',after))
  method=re.search(b.METHOD,after).group();self.assertNotIn('->requestLayout()V',method)
  for expected in ['->root:', '->store:', '->currentView:', '->rememberHomeFocus()V','->rememberSettingsFocus()V','->showCurrentPage()V','->restoreHomeFocus()V']:self.assertIn(expected,method)
 def test_only_old_signatures_are_removed(self):
  for name in ['META-INF/CERT.RSA','META-INF/CERT.SF','META-INF/MANIFEST.MF']:self.assertTrue(b.signature(name))
  for name in ['assets/eyes.png','lib/arm64-v8a/library.so','META-INF/NOTICE','classes2.dex']:self.assertFalse(b.signature(name))
if __name__=='__main__':unittest.main()
