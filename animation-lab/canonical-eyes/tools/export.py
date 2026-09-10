"""Create a self-contained source/runtime transplant bundle; no rendered frames needed."""
from pathlib import Path
import argparse,hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--out',type=Path,required=True);p.add_argument('--prepared',type=Path,required=True);args=p.parse_args()
out=args.out
if json.loads((args.prepared/'assets/catalogue.json').read_text()) != json.loads((ROOT/'catalogue.json').read_text()):
    raise SystemExit('Prepared catalogue is stale; rerun prepare.py')
if (args.prepared/'assets/boopApprovedEyes.png').read_bytes() != (ROOT/'assets/boopApprovedEyes.png').read_bytes():
    raise SystemExit('Prepared master differs from source')
if out.exists():raise SystemExit('Choose a fresh checkpoint directory')
out.mkdir(parents=True)
shutil.copytree(ROOT,out/'source')
shutil.copytree(args.prepared/'assets',out/'runtime/assets')
shutil.copytree(args.prepared/'java',out/'runtime/java')
shutil.copytree(ROOT/'java',out/'runtime/java',dirs_exist_ok=True)
shutil.copytree(ROOT/'android/com/boop/eyes',out/'runtime/java/com/boop/eyes',dirs_exist_ok=True)
catalogue=json.loads((ROOT/'catalogue.json').read_text())
for clip in catalogue['clips']:
    d=out/'clips'/clip['id'];d.mkdir(parents=True)
    (d/'clip.json').write_text(json.dumps(clip,indent=2)+'\n')
    (d/'README.md').write_text(f"# {clip['label']}\n\nFamily: {clip['family']}.\n\nUse the shared runtime; select clip `{clip['id']}`. Duration {clip['keys'][-1][0]} ms. Loop: {clip.get('loop',False)}. These are lid/gaze channels, not images. Original eye master and shared shader are in runtime/assets. Ryan review required.\n")
head=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
(out/'PROVENANCE.json').write_text(json.dumps({'source_head':head,'catalogue_version':catalogue['version'],'master_sha256':catalogue['master_sha256'],'clips':len(catalogue['clips']),'status':'implemented code; see runtime receipts for tested build; Ryan review pending'},indent=2)+'\n')
(out/'README.md').write_text('# Canonical BOOP eye and sign-show transplant\n\n26 code-driven eye animations share one state engine, original eye texture, and shader. Copy runtime/java plus runtime/assets into the target host; keep its existing renderer lifecycle/permissions. Integrate CanonicalEyeRenderer via GLSurfaceView and supply EyeMotion.Pose from EyeMotion.Controller. Source/README.md documents the contract. The demonstration Activity is optional lab-only source. No Android profile, voice, media authority or notification policy is included.\n\nSignMotion and NotificationSignView also provide four local sign-holder performances using the exact five-digit hand pair as separate wrist/prop layers. Source/SIGN_SHOW.md explains timing, layout and limitations. These are fixtures, not live notifications or articulated finger animation.\n\nThis does not replace accessory artwork. It has not been promoted into Unified. No extra bottom eyelid or whole-image jiggle. Default master bytes are locked.\n')
lines=[hashlib.sha256(f.read_bytes()).hexdigest()+'  '+f.relative_to(out).as_posix() for f in sorted(out.rglob('*')) if f.is_file()]
(out/'SHA256SUMS.txt').write_text('\n'.join(lines)+'\n')
shutil.make_archive(str(out),'zip',out)
print(f'Exported {len(catalogue["clips"])} code clips to {out}.zip')
