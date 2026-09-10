"""Generate build inputs from the single catalogue and traced rig; never edit master."""
from pathlib import Path
import argparse, hashlib, json, shutil
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
P=argparse.ArgumentParser();P.add_argument('--out',type=Path,required=True);args=P.parse_args()
out=args.out;out.mkdir(parents=True,exist_ok=True)
data=json.loads((ROOT/'catalogue.json').read_text())
master=ROOT/'assets/boopApprovedEyes.png'
assert hashlib.sha256(master.read_bytes()).hexdigest()==data['master_sha256']
assert len({c['id'] for c in data['clips']})==len(data['clips'])
java=out/'java/com/boop/eyes';java.mkdir(parents=True,exist_ok=True)
assets=out/'assets';shutil.copytree(ROOT/'assets',assets,dirs_exist_ok=True)
shutil.copy2(ROOT/'catalogue.json',assets/'catalogue.json')
entries=[]
for c in data['clips']:
    keys=c['keys'];assert keys[0][0]==0 and len(keys)>1
    for i,key in enumerate(keys):
        assert len(key)==5 and (i==0 or key[0]>keys[i-1][0])
        assert all(0<=v<=1 for v in key[1:3]) and all(-1<=v<=1 for v in key[3:])
    if c.get('loop'):assert keys[0][1:]==keys[-1][1:]
    vals=','.join('{'+','.join(str(float(v))+'f' for v in key)+'}' for key in keys)
    entries.append('new EyeMotion.Clip('+','.join(json.dumps(c[k]) for k in ['id','label','family'])+','+str(c.get('loop',False)).lower()+','+str(c.get('ambient_blink',False)).lower()+',new float[][]{'+vals+'})')
(java/'EyeCatalogue.java').write_text('package com.boop.eyes;\npublic final class EyeCatalogue {\n public static final EyeMotion.Clip[] ALL={\n'+',\n'.join(entries)+'};\n public static EyeMotion.Clip find(String id){for(EyeMotion.Clip c:ALL)if(c.id.equals(id))return c;return ALL[0];}\n}\n')
# Smooth monotone cubic interpolation of the previously preserved manual lid trace.
points=[[(108,530),(165,537),(190,462),(230,397),(280,337),(330,290),(380,254),(430,229),(480,216),(530,213),(580,218),(630,235),(680,265),(720,299),(760,347),(797,405),(825,405)],[(947,405),(975,405),(1010,348),(1050,299),(1090,265),(1140,235),(1190,219),(1240,213),(1290,216),(1340,230),(1390,254),(1440,290),(1490,337),(1540,397),(1580,462),(1607,537),(1663,530)]]
def value(pts,x):
    slopes=[(b[1]-a[1])/(b[0]-a[0]) for a,b in zip(pts,pts[1:])]
    tangent=[slopes[0]]+[0 if a*b<=0 else 2*a*b/(a+b) for a,b in zip(slopes,slopes[1:])]+[slopes[-1]]
    if x<=pts[0][0]:return pts[0][1]
    if x>=pts[-1][0]:return pts[-1][1]
    i=next(i for i in range(len(pts)-1) if x<=pts[i+1][0]);a,b=pts[i:i+2]
    h=b[0]-a[0];t=(x-a[0])/h
    return (2*t**3-3*t*t+1)*a[1]+(t**3-2*t*t+t)*h*tangent[i]+(-2*t**3+3*t*t)*b[1]+(t**3-t*t)*h*tangent[i+1]
# The source trace ends on the original cap; it remains the material sampler.
# A separate destination trace continues its incoming tangent past that cap.
# Do not stretch skin alpha outside the original face or sample invented skin.
contours=[points[0][:-1]+[(825,449),(887,546)],[(887,546),(947,449)]+points[1][1:]]
rig=Image.new('RGBA',(1774,2));rig.putdata([(int(round((value(trace[0 if x<887 else 1],x)-24)*32))>>8,int(round((value(trace[0 if x<887 else 1],x)-24)*32))&255,0,255) for trace in [points,contours] for x in range(1774)])
rig.save(assets/'lid-rig.png')
(out/'rig.json').write_text(json.dumps({'manual_lid_trace':points,'destination_contour':contours,'texture_rows':['source skin edge','continued destination edge'],'inset':24,'skin_band':64,'closure_destination':887,'feather_source_pixels':1.5,'interpolation':'monotone cubic; packed fixed-point 1/32px'},indent=2)+'\n')
print(f'Prepared {len(entries)} clips; master hash verified. No visual acceptance.')
