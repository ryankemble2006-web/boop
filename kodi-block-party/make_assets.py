"""Build-time vector-style arcade art. Kodi runtime does not need Pillow."""
import argparse
import hashlib
import math
import shutil
import struct
import wave
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parent
MEDIA = ROOT / 'script.boop.blockparty/resources/media'
MEDIA.mkdir(parents=True, exist_ok=True)
COLORS = {'I': '#49e5f2', 'O': '#ffd45d', 'T': '#b291ff', 'J': '#5c95fa',
          'L': '#ff9962', 'S': '#8ae0a2', 'Z': '#ff638b'}

def rgb(c):
    return tuple(bytes.fromhex(c.lstrip('#')))

def mix(c, factor):
    return tuple(min(255, int(v*factor)) for v in rgb(c))

def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('segoeuib.ttf' if bold else 'segoeui.ttf'), size)

def cube(color, ghost=False):
    im = Image.new('RGBA', (128,128))
    d = ImageDraw.Draw(im)
    if ghost:
        d.polygon([(8,24),(96,24),(96,112),(8,112)], fill=(50,185,208,12), outline=(99,226,247,150), width=3)
        d.line([(8,24),(23,9),(111,9),(111,97),(96,112)], fill=(99,226,247,90), width=3)
        d.line([(96,24),(111,9)], fill=(99,226,247,100),width=3)
    else:
        d.polygon([(8,24),(23,9),(111,9),(96,24)], fill=mix(color,1.23))
        d.polygon([(96,24),(111,9),(111,97),(96,112)], fill=mix(color,.49))
        for y in range(24,113):
            d.line((8,y,96,y), fill=mix(color,1.03-.3*(y-24)/88))
        d.line([(8,112),(8,24),(96,24),(111,9)],fill=mix(color,1.45),width=2)
        d.line([(96,24),(96,112),(8,112)],fill=mix(color,.64),width=2)
        d.rectangle((16,32,86,102),outline=(*mix(color,1.25),100),width=1)
        d.line((17,35,75,35),fill=(*mix(color,1.5),160),width=2)
        d.rectangle((18,94,31,97),fill=(*mix(color,.50),170))
        d.rectangle((36,94,42,97),fill=(*mix(color,.50),170))
    return im

for kind,color in COLORS.items():
    cube(color).save(MEDIA / (kind + '.png'))
cube('#49e5f2', True).save(MEDIA / 'ghost.png')
Image.new('RGBA',(2,2),(0,0,0,0)).save(MEDIA/'empty.png')

W,H=1280,720
im=Image.new('RGB',(W,H))
p=im.load()
for y in range(H):
    for x in range(W):
        glow=math.exp(-(((x-635)/420)**2+((y-240)/380)**2))
        amber=math.exp(-(((x-1150)/420)**2+((y-700)/300)**2))
        p[x,y]=(int(7+4*glow+5*amber),int(12+13*glow+2*amber),int(20+19*glow))
d=ImageDraw.Draw(im)
for x in range(-800,2100,95):
    d.line([(640+(x-640)*.15,462),(x,720)], fill=(17,34,44),width=1)
for y in (483,508,542,587,647,719):
    d.line((0,y,1280,y),fill=(17,34,44))

def text(x,y,s,size=16,color='#8cabb8',bold=False):
    d.text((x,y),s,font=font(size,bold),fill=color)

def panel(box, outline='#223846', fill='#0c1721', radius=14):
    d.rounded_rectangle(box,radius,fill=fill,outline=outline,width=1)

text(52,29,'B O O P   / /   A R C A D E',16,'#81e5e9',True)
text(957,29,'01     SOFA EDITION',15,'#90a9b4')
d.line((52,64,1228,64),fill='#29424c')
text(53,102,'BLOCK',65,'#f0f6f6',True)
text(53,172,'PARTY',65,'#60e5ed',True)
text(57,263,'A little order. A lot of falling things.',16)
panel((54,317,389,505))
text(73,334,'YOUR VERY QUALIFIED SUPERVISOR',12,'#7c9ca9',True)
text(73,468,'BOOP',14,'#66e5ed',True)
text(136,468,'/  eyes on the job',14)
panel((54,526,389,641))
text(74,541,'PERSONAL BEST',12,'#91a9b5',True)
text(74,606,'Stored here. Bragging rights included.',12,'#7896a3')

# Recessed arcade well, lit edges, side bevels and bolts.
d.polygon([(451,90),(469,76),(811,76),(829,90),(829,661),(811,675),(469,675),(451,661)],fill='#172b37')
d.rounded_rectangle((460,83,820,668),18,fill='#0a111b',outline='#43616f',width=2)
d.rounded_rectangle((473,92,807,657),12,fill='#112735',outline='#64d3db',width=1)
d.rectangle((493,105,787,644),fill='#030a11')
for y in range(21):
    d.line((495,106+y*26,781,106+y*26),fill='#142531')
for x in range(11):
    d.line((495+x*28.6,106,495+x*28.6,626),fill='#142531')
# Square grid is 28px; board coordinates are shared with the Kodi renderer.
d.rectangle((497,91,782,654),fill='#040c14')
for y in range(21):
    d.line((499,94+y*28,779,94+y*28),fill='#142531')
for x in range(11):
    d.line((499+x*28,94,499+x*28,654),fill='#142531')
for x in (465,814):
    for y in (94,654):
        d.ellipse((x-2,y-2,x+2,y+2),fill='#708591')
d.line((476,661,804,661),fill='#64e8eb',width=2)

panel((865,94,1226,217))
text(886,108,'SCORE',13,'#7f9fac',True)
panel((865,233,1038,327))
panel((1053,233,1226,327))
text(886,245,'LEVEL',12,'#7f9fac',True)
text(1074,245,'LINES',12,'#7f9fac',True)
panel((865,343,1226,450))
text(886,355,'UP NEXT',12,'#7f9fac',True)
text(865,473,'YOU HAVE THE REMOTE.',15,'#e3ecee',True)
instructions=[('ENTER / OK','Rotate'),('LEFT / RIGHT','Move'),('DOWN','Fall faster'),('UP','Drop'),('BACK','Pause / exit')]
for i,(key,value) in enumerate(instructions):
    y=506+i*28
    text(868,y,key,12,'#65dce6',True)
    text(1033,y,value,15,'#b3c5cd')
text(54,680,'NO COINS. NO CLOUD. JUST ONE MORE GO.',12,'#66838e',True)
text(936,680,'STACK  /  CLEAR  /  REPEAT',12,'#66838e',True)
im.save(MEDIA/'cabinet.png')

overlay=Image.new('RGBA',(460,245))
od=ImageDraw.Draw(overlay)
od.rounded_rectangle((0,0,459,244),18,fill=(7,18,28,248),outline=(100,230,240,255),width=2)
od.rounded_rectangle((8,8,451,236),12,outline=(36,70,83,255),width=1)
overlay.save(MEDIA/'overlay.png')
for name,title,body,hint in [
    ('ready','BLOCK PARTY','I supervise. You stack.','ENTER / OK to play'),
    ('paused','TAKE A BREATHER','Your blocks will wait.','ENTER resumes  /  BACK exits'),
    ('over','WELL. THAT PILED UP.','The stack won this round.','ENTER to replay  /  BACK exits')]:
    card=overlay.copy()
    cd=ImageDraw.Draw(card)
    cd.text((230,40),title,font=font(29,True),fill='#73e9ed',anchor='mt')
    cd.text((230,106),body,font=font(20),fill='#d6e4ea',anchor='mt')
    cd.text((230,174),hint,font=font(17,True),fill='#ffffff',anchor='mt')
    card.save(MEDIA/(name+'.png'))

# Keep the canonical eyes intact on disk. Runtime controls scale/blink them.
parser = argparse.ArgumentParser()
parser.add_argument('--eyes', type=Path, default=MEDIA/'boopApprovedEyes.png', help='Exact approved BOOP eye master')
source=parser.parse_args().eyes
assert hashlib.sha256(source.read_bytes()).hexdigest()=='ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22'
if source.resolve() != (MEDIA/'boopApprovedEyes.png').resolve():
    shutil.copyfile(source,MEDIA/'boopApprovedEyes.png')

icon=Image.new('RGB',(512,512),'#0b1723')
ic=ImageDraw.Draw(icon)
ic.rounded_rectangle((16,16,495,495),48,outline='#4ce4ee',width=4)
ic.text((53,40),'BOOP',font=font(66,True),fill='#f4f7f7')
for x,y,k in [(98,179,'T'),(189,179,'T'),(280,179,'T'),(189,88,'T'),(98,288,'I'),(189,288,'I'),(280,288,'I')]:
    tile=cube(COLORS[k]).resize((108,108),Image.Resampling.LANCZOS)
    icon.paste(tile,(x,y),tile)
ic.text((53,418),'BLOCK PARTY',font=font(42,True),fill='#60e5ed')
icon.save(MEDIA/'icon.png')

def sound(name, notes, duration=.065):
    samples=[]
    rate=22050
    for freq in notes:
        n=int(rate*duration)
        for i in range(n):
            t=i/rate
            envelope=math.sin(math.pi*i/n)**2
            samples.append(int(5500*envelope*(math.sin(2*math.pi*freq*t)+.15*math.sin(4*math.pi*freq*t))))
    with wave.open(str(MEDIA/(name+'.wav')),'wb') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(rate)
        f.writeframes(struct.pack('<'+'h'*len(samples),*samples))
sound('rotate',[520],.025)
sound('lock',[110,82],.035)
sound('clear',[523,659,784],.07)
sound('start',[392,523,784],.07)
sound('over',[330,262,196],.12)
print('Generated cabinet, 3D cube sprites, icon, sound effects; copied verified eyes unchanged.')
