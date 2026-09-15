"""Preserve the user-accepted default in source and the actual Unified build."""
from pathlib import Path
import argparse,hashlib,json
ROOT=Path(__file__).resolve().parents[1]
LOCK=ROOT/'unified/animation/accepted-felt-v189.json'
def blob(data):return hashlib.sha1(b'blob '+str(len(data)).encode()+b'\0'+data).hexdigest()
def verify_bytes(data,expected,label):
 if blob(data)!=expected:raise ValueError('Accepted felt default changed: '+label)
def main():
 parser=argparse.ArgumentParser()
 parser.add_argument('--materialized',type=Path)
 parser.add_argument('--self-test',action='store_true')
 args=parser.parse_args()
 manifest=json.loads(LOCK.read_text())
 assert manifest['default'] is True
 base=args.materialized/'animation-lib/src/main' if args.materialized else ROOT/'unified/animation'
 for relative,expected in manifest['files'].items():
  verify_bytes((base/relative).read_bytes(),expected,relative)
 if args.self_test:
  relative,expected=next(iter(manifest['files'].items()))
  original=(ROOT/'unified/animation'/relative).read_bytes()
  try:verify_bytes(original+b'\n',expected,relative)
  except ValueError:pass
  else:raise AssertionError('Changed artwork was not rejected')
 if args.materialized:
  app=args.materialized/'app/src/main/java/com/boop/alpha1'
  wall=(app/'MainActivity.java').read_text()
  assert 'face = new BoopCanonicalFaceView(this);' in wall
  assert 'face = new BoopFaceView(this);' not in wall
  surfaces=[app/name for name in ('BoopCanonicalFaceView.java','BoopCanonicalAnimationActivity.java','BoopAppearanceActivity.java','BoopNotificationPuppetView.java')]
  surfaces.append(args.materialized/'shield-home-lib/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java')
  for path in surfaces:
   text=path.read_text()
   assert 'CanonicalEyeRenderer' in text,(path,'default renderer missing')
   assert 'EyeColourBinding.install' in text,(path,'saved colour binding missing')
 print('PASS accepted v189 felt default: '+str(len(manifest['files']))+' exact files'+(' and all materialized live-eye routes' if args.materialized else ''))
if __name__=='__main__':main()
