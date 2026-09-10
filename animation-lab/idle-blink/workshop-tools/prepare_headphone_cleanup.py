"""Apply the existing Shield occlusion regions as a separate alpha mask candidate."""
from pathlib import Path
import hashlib,json,shutil,sys
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
src=ROOT/'headphones/recovered-source-v1/source/shield-overlay/app/src/main/res/drawable-nodpi/boop_headphones.png'
assert hashlib.sha256(src.read_bytes()).hexdigest()=='c86d8fa046d1d6b6c96c15d5a6e38f3c0b89533a65946fc6d59347e7679250e9'
dest=ROOT/'headphones/headphone_cleanup_v1_candidate'
if dest.exists(): raise SystemExit('Checkpoint exists; do not overwrite')
dest.mkdir(parents=True)
shutil.copy2(src,dest/'boop_headphones_legacy.png')
shutil.copy2(__file__,dest/'prepare_headphone_cleanup.py')
im=Image.open(src).convert('RGBA')
# Existing source regions, not new painted/reconstructed headphone pixels.
# First two: shield-clean-launcher drawable; second two: old eye slots in view.
regions=[(440,385,725,575),(800,435,1090,620),(395,465,711,790),(757,515,1075,850)]
mask=Image.new('L',(im.width*4,im.height*4),0)
d=ImageDraw.Draw(mask)
for box in regions:d.ellipse(tuple(round(v*4) for v in box),fill=255)
mask=mask.resize(im.size,Image.Resampling.LANCZOS)
a=np.array(im); removal=np.array(mask,dtype=np.uint16)
a[:,:,3]=((a[:,:,3].astype(np.uint16)*(255-removal)+127)//255).astype(np.uint8)
clean=Image.fromarray(a); clean.save(dest/'headphones-alpha-cleanup-candidate.png'); mask.save(dest/'legacy-eye-removal-mask.png')
master=Image.open(ROOT/'canonical/recovered/canonical-idle-blink-v1/assets/boopApprovedEyes_master.png').convert('RGBA')
pair=master.resize((770,385),Image.Resampling.LANCZOS)
combined=clean.copy(); combined.alpha_composite(pair,(350,462)); combined.save(dest/'layered-open-review.png')
(dest/'recipe.json').write_text(json.dumps({'source_sha256':hashlib.sha256(src.read_bytes()).hexdigest(),'regions':regions,'source_branch':'boop-shield-clean-launcher','source_head':'9888fbef444d1e0647fdd8ae90acb7197acfb813','pair_source_box':[349.5,462.5,1119.5,847.5],'review_preview_rounding':'PNG preview rounds half-pixel pair position; Android float placement remains source authority','status':'Candidate only; Ryan must review edge cuts and remaining legacy pixels'},indent=2)+'\n')
(dest/'README.md').write_text('# Transparent accessory cleanup candidate\n\nOriginal headphones PNG is unchanged. A separate alpha mask uses the existing Shield source regions for two obsolete upper arcs and two legacy eye ovals. RGB is unchanged. No hidden pixels were invented. The approved pair is a separate layer.\n\nThis extends the previous black-bay mask to a transparent accessory candidate, requiring Ryan review for retained earcup edges and any remaining old-eye pixels. Existing flattened art limits separation. If the mask cuts wanted artwork, stop and request original editable headphone layers; do not paint replacements. The PNG composite is a placement review with half-pixel rounding, not an approved renderer or APK.\n')
(dest/'SHA256SUMS.txt').write_text('\n'.join(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.name for p in sorted(dest.iterdir()) if p.is_file())+'\n')
shutil.make_archive(str(ROOT/'exports/headphone_cleanup_v1_candidate'),'zip',dest)
print('Saved separate alpha-mask candidate; original headphone and eye sources unchanged.')
