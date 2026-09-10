from pathlib import Path
import hashlib,json,shutil,subprocess,sys
from PIL import Image
ROOT=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'tools/python-deps'))
import imageio_ffmpeg
for dest in sorted((ROOT/'blink').glob('blink_v*')):
    config=json.loads((dest/'animation.json').read_text())
    frames=config['frames']
    pixels={}
    for e in frames:
        im=Image.open(dest/e['file']).convert('RGBA')
        bg=Image.new('RGB',im.size,'black'); bg.paste(im,mask=im.getchannel('A'))
        pixels[e['file']]=bg.resize((888,444),Image.Resampling.LANCZOS).tobytes()
    video=dest/'preview.mp4'
    if not video.exists():
        with (dest/'ffmpeg-export.log').open('wb') as log:
            p=subprocess.Popen([imageio_ffmpeg.get_ffmpeg_exe(),'-y','-loglevel','error','-f','rawvideo','-pix_fmt','rgb24','-s','888x444','-r','120','-i','-','-an','-c:v','libx264','-crf','16','-pix_fmt','yuv420p',str(video)],stdin=subprocess.PIPE,stderr=log)
            for i in range(360):
                t=i*1000/120-1000
                e=min(frames,key=lambda e:abs(e['time_ms']-t)) if 0<t<183 else frames[0]
                p.stdin.write(pixels[e['file']])
            p.stdin.close()
            if p.wait(): raise SystemExit('FFmpeg failed; see log')
    master=dest/'boopApprovedEyes_master.png'
    assert hashlib.sha256(master.read_bytes()).hexdigest()==config['master_sha256']
    assert (dest/frames[0]['file']).read_bytes()==master.read_bytes()==(dest/frames[-1]['file']).read_bytes()
    assert max(e['closure'] for e in frames)==1
    notes=dest/'NOTES.md'
    if not notes.exists():
        notes.write_text('# Saved candidate; Ryan review pending\n\n25 PNG states, 183ms timing; 73.2ms close, 8ms hold, 101.8ms reopen. Source cadence uses 3–7s intervals, 18% doubles, 110ms gap. The 8ms plateau is a requested candidate refinement, not previously accepted timing. Master and open endpoint bytes are unchanged.\n\nGIF timing is quantized and illustrative; JSON is timing authority. MP4 uses 120fps sampling. No new APK contains these frames yet. Root-anchored source pixel deformation uses manual edge rig data; no image generation or replacement eye artwork. Headphone arcs require separate cleanup.\n\nInitial export failed because sandbox could not import the escalated-installed FFmpeg module; finish_previews.py resumed from saved PNGs without rerendering. v2 is diagnostic history, v3 is the latest review candidate.\n')
    receipts=[hashlib.sha256(p.read_bytes()).hexdigest()+'  '+p.relative_to(dest).as_posix() for p in sorted(dest.rglob('*')) if p.is_file() and p.name!='SHA256SUMS.txt']
    (dest/'SHA256SUMS.txt').write_text('\n'.join(receipts)+'\n')
    (ROOT/'exports').mkdir(exist_ok=True)
    shutil.make_archive(str(ROOT/'exports'/dest.name),'zip',dest)
    print('Exported and integrity-checked',dest.name)
