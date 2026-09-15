"""Numeric rounded-lid geometry against the actual photo mask; no image rendering."""
from pathlib import Path
import math,re,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
shader=(ROOT/'unified/animation/assets/eyes.frag').read_text()
mix=lambda a,b,t:a*(1-t)+b*t

def evaluate(expression,values):
 assert re.fullmatch(r'[A-Za-z0-9_ .,+*/()\-]+',expression),expression
 return eval(expression,{'__builtins__':{}},{'sqrt':math.sqrt,'min':min,'max':max,'mix':mix,**values})

helper=re.search(r'float lidClosedY[^{}]+[{]\s*float dx=([^;]+);\s*return ([^;]+);',shader)
if helper:
 centres=re.search(r'float lidCentre=left[?]([0-9.]+):([0-9.]+);',shader)
 assert centres,'Actual target centres must be explicit'
 centres=tuple(map(float,centres.groups()))
 assert 'float end=mix(edge,lidClosedY(p.x,lidCentre),closure);' in shader
 def closed(x,centre):
  dx=evaluate(helper.group(1),dict(x=x,centre=centre))
  return evaluate(helper.group(2),dict(dx=dx))
else:
 assert 'float end=mix(edge,642.0,closure);' in shader
 centres=(470.,1066.)
 def closed(x,centre):return 642.

for centre in centres:
 middle=closed(centre,centre)
 ends=[closed(centre+d,centre) for d in (-280,280)]
 print('Closed lower lip centre and ends:',middle,ends,flush=True)
 assert 60<min(middle-y for y in ends)<200,'Closed lid still has a flat bottom'
 assert 620<middle<639,'Oval bottom must cover the eye and stay above the hero crop'
 for distance in range(281):
  assert abs(closed(centre-distance,centre)-closed(centre+distance,centre))<1e-10
  if distance:assert closed(centre+distance,centre)<closed(centre+distance-1,centre)
 # The same target evolves continuously through a blink; no late shape switch.
 for edge in (170.,250.,410.):
  for dx in (-280.,0.,280.):
   target=closed(centre+dx,centre)
   sequence=[mix(edge,target,c/100) for c in range(101)]
   assert sequence==sorted(sequence)

with tempfile.TemporaryDirectory() as tmp:
 subprocess.run(['javac','-d',tmp,str(ROOT/'unified/animation/java/com/boop/eyes/PngPuppetRig.java'),str(ROOT/'tests/java/PngCoverageData.java')],check=True)
 raw=Path(tmp)/'mask.rgba'
 subprocess.run(['java','-Djava.awt.headless=true','-cp',tmp,'com.boop.eyes.PngCoverageData',str(ROOT/'unified/animation/assets/boop-png-study.png'),str(raw)],check=True)
 rig=raw.read_bytes()
# Full closure must cover every sustained opaque part of the original eye below
# its felt lip. A curved target alone must not expose a white crescent beneath it.
clearances=[]
for columns,centre in ((range(220,736),centres[0]),(range(800,1317),centres[1])):
 for x in columns:
  edge=(rig[x*4]*256+rig[x*4+1])/32
  if edge>=640:continue
  run=0;bottom=-1
  for y in range(max(400,math.ceil(edge+3)),640):
   run=run+1 if rig[(y*1536+x)*4+3]>=250 else 0
   if run>=3:bottom=y+.5
  if bottom>0:clearances.append((closed(x+.5,centre)-2-bottom,x,bottom))
assert len(clearances)>500,'Need real photographic eye fixtures'
print('Minimum full-close eye coverage margin:',min(clearances),flush=True)
assert min(clearances)[0]>=0,('Rounded lip leaves original eye showing at full closure',min(clearances))
print('PASS rounded lower lip, continuous motion, hero bounds and real-eye full-close coverage')
