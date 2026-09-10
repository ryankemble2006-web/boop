"""Non-destructive source-pixel puppetry. Never writes the approved master.

Outputs are review candidates, never automated appearance acceptance.
Lid-edge control points are editable manual rig data, not replacement artwork.
"""
from pathlib import Path
import hashlib, json, shutil, subprocess, sys
import numpy as np
from PIL import Image

ROOT = Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'tools/python-deps'))
import imageio_ffmpeg

MASTER = ROOT / 'canonical/recovered/canonical-idle-blink-v1/assets/boopApprovedEyes_master.png'
EXPECTED = 'ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22'
# Traced lower boundary of the existing glossy lid, in original source coordinates.
# Both eyes retain their own geometry; no mirrored or normalized replacement eye.
EDGES = [
    [(108,530),(165,537),(190,462),(230,397),(280,337),(330,290),(380,254),(430,229),(480,216),(530,213),(580,218),(630,235),(680,265),(720,299),(760,347),(797,405),(825,405)],
    [(947,405),(975,405),(1010,348),(1050,299),(1090,265),(1140,235),(1190,219),(1240,213),(1290,216),(1340,230),(1390,254),(1440,290),(1490,337),(1540,397),(1580,462),(1607,537),(1663,530)],
]

def smooth(x):
    x = max(0., min(1., x))
    return x*x*(3-2*x)

def closure_at(ms):
    # Preserve 183ms total and the Shield close turning point (40%).
    # Normalize the legacy 5% slit to full closure; 8ms plateau is explicit.
    if ms <= 0 or ms >= 183:
        return 0.
    if ms < 73.2:
        return smooth(ms / 73.2)
    if ms <= 81.2:
        return 1.
    return 1 - smooth((ms-81.2)/101.8)

def deform(master, amount):
    if amount == 0:
        return master.copy()
    src = np.array(master)
    out = src.copy()
    for points in EDGES:
        for x in range(points[0][0], points[-1][0]+1):
            occupied = np.flatnonzero(src[:,x,3])
            if not occupied.size:
                continue
            bottom = int(occupied[-1])
            edge = float(np.interp(x, *zip(*points))) - 24
            # Anchor a band inside the existing lid. Faint source alpha specks
            # must not determine the root and stretch a transparent edge row.
            root = int(edge - 64)
            if edge <= root or edge >= bottom:
                continue
            end = edge + amount*(bottom+1-edge)
            ys = np.arange(root, min(bottom+1, int(end)+1))
            source_y = root + (ys-root)*(edge-root)/(end-root)
            lo = np.floor(source_y).astype(int)
            hi = np.minimum(lo+1, src.shape[0]-1)
            f = (source_y-lo)[:,None]
            # Sample only source lid RGB. Keep exact destination master alpha.
            out[ys,x,:3] = np.rint(src[lo,x,:3]*(1-f)+src[hi,x,:3]*f).astype(np.uint8)
    return Image.fromarray(out)

def main():
    assert hashlib.sha256(MASTER.read_bytes()).hexdigest() == EXPECTED
    dest = ROOT / 'blink/blink_v3_anchored_skin_candidate'
    if dest.exists():
        raise SystemExit('Checkpoint exists; choose a new version, never overwrite.')
    (dest/'frames').mkdir(parents=True)
    shutil.copy2(MASTER, dest/'boopApprovedEyes_master.png')
    shutil.copy2(__file__, dest/'build_blink_candidate.py')
    master = Image.open(MASTER).convert('RGBA')
    schedule = sorted(set([round(i*183/22,3) for i in range(23)] + [73.2,81.2]))
    entries=[]
    for i, ms in enumerate(schedule):
        fn = dest/'frames'/f'{i:03d}.png'
        a=closure_at(ms)
        if a == 0:
            shutil.copy2(MASTER, fn)
        else:
            deform(master,a).save(fn)
        entries.append({'file':f'frames/{i:03d}.png','time_ms':ms,'closure':a})
    config={'master_sha256':EXPECTED,'duration_ms':183,'close_end_ms':73.2,
            'closed_hold_ms':8,'reopen_ms':101.8,'idle_delay_ms':[3000,7000],
            'double_probability':0.18,'double_gap_ms':110,'lid_edges':EDGES,
            'status':'CANDIDATE — Ryan visual review required','frames':entries}
    (dest/'animation.json').write_text(json.dumps(config,indent=2)+'\n')
    # Frame/state previews have a brief open lead-in/out. PNG sequence is authority.
    previews=[master.copy()]+[Image.open(dest/e['file']).convert('RGBA') for e in entries]+[master.copy()]
    gifframes=[]
    for frame in previews:
        bg=Image.new('RGB',frame.size,'black'); bg.paste(frame,mask=frame.getchannel('A'))
        bg.thumbnail((887,444)); gifframes.append(bg)
    gifframes[0].save(dest/'preview.gif',save_all=True,append_images=gifframes[1:],duration=[1000]+[10]*len(entries)+[1600],loop=0)
    ffmpeg=imageio_ffmpeg.get_ffmpeg_exe()
    proc=subprocess.Popen([ffmpeg,'-y','-loglevel','error','-f','rawvideo','-pix_fmt','rgb24','-s','888x444','-r','120','-i','-', '-an','-c:v','libx264','-crf','16','-pix_fmt','yuv420p',str(dest/'preview.mp4')],stdin=subprocess.PIPE,stdout=subprocess.DEVNULL,stderr=subprocess.PIPE)
    cached={}
    for i in range(360):
        t=i*1000/120-1000
        key=round(t,4) if 0<t<183 else 0
        if key not in cached:
            frame=deform(master,closure_at(t)) if key else master
            bg=Image.new('RGB',master.size,'black'); bg.paste(frame,mask=frame.getchannel('A'))
            cached[key]=bg.resize((888,444),Image.Resampling.LANCZOS).tobytes()
        proc.stdin.write(cached[key])
    proc.stdin.close(); err=proc.stderr.read(); code=proc.wait()
    if code: raise RuntimeError(err.decode())
    (dest/'NOTES.md').write_text('# Anchored source-lid candidate\n\nExact master preserved. Runtime-derived frames stretch original lid RGB downward, with per-column roots fixed and original footprint alpha retained. No replacement art. Open endpoints are byte-identical master copies. Manual edge rig needs Ryan review. 183ms total, 73.2ms close, 8ms hold, 101.8ms reopen; interval/double settings copied from Shield. GIF quantization rounds timing; MP4 is 120fps. Use JSON timing for transplant.\n\nHeadphone cleanup is separate and not included in this face-only candidate. No Android integration or visual acceptance is claimed.\n')
    receipts=[]
    for p in sorted(dest.rglob('*')):
        if p.is_file(): receipts.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(dest).as_posix())
    (dest/'SHA256SUMS.txt').write_text('\n'.join(receipts)+'\n')
    assert (dest/'frames/000.png').read_bytes()==MASTER.read_bytes()
    assert (dest/entries[-1]['file']).read_bytes()==MASTER.read_bytes()
    assert closure_at(73.2)==closure_at(81.2)==1
    assert hashlib.sha256(MASTER.read_bytes()).hexdigest()==EXPECTED
    (ROOT/'exports').mkdir(exist_ok=True)
    shutil.make_archive(str(ROOT/'exports/blink_v3_anchored_skin_candidate'),'zip',dest)
    print(f'Created {dest}; {len(entries)} source frames. Integrity/timing checks passed; visual review pending.')

if __name__=='__main__': main()
