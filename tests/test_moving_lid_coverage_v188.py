"""Numeric evaluation of actual PNG mask and shader compositing; no image rendering."""
from pathlib import Path
import math,re,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1]
shader=(ROOT/'unified/animation/assets/eyes.frag').read_text()
mix=lambda a,b,t:a*(1-t)+b*t

def evaluate(expression, values):
 assert re.fullmatch(r'[A-Za-z0-9_ .,+*/()\-]+',expression),expression
 return eval(expression,{'__builtins__':{}},{'mix':mix,'min':min,'max':max,**values})

def split_args(expression):
 level=0
 for i,c in enumerate(expression):
  if c=='(':level+=1
  elif c==')':level-=1
  elif c==',' and level==0:return expression[:i],expression[i+1:]
 raise AssertionError('Expected premultiplied RGB and alpha outputs')

# Read actual output arithmetic. The old pipeline is recognized exactly so this
# regression first reproduces its transparent destination at moving cap ends.
composite=re.search(r'vec4 puppetComposite[^{}]+[{]\s*return vec4[(]([^;]+)[)];',shader)
if composite:
 rgb_expr,alpha_expr=split_args(composite.group(1))
 assert 'gl_FragColor=puppetComposite(rgb,alpha,fabric(lid),lidAlpha,cover);' in shader
 assert 'float lidAlpha=texture2D(uRig,vec2(p.x,sampleY)/vec2(1536.0,640.0)).a;' in shader
 assert 'if(alpha<' not in shader,'Old destination-alpha early return clips moved ends'
else:
 assert 'rgb=mix(rgb,fabric(lid),cover);' in shader
 assert 'gl_FragColor=vec4(rgb*alpha,alpha);' in shader
 rgb_expr='mix(baseRgb,lidRgb,cover)*baseAlpha'
 alpha_expr='baseAlpha'
warp=re.search(r'float lidSampleY[^{}]+[{]\s*return ([^;]+);',shader).group(1)
with tempfile.TemporaryDirectory() as tmp:
 subprocess.run(['javac','-d',tmp,str(ROOT/'unified/animation/java/com/boop/eyes/PngPuppetRig.java'),str(ROOT/'tests/java/PngCoverageData.java')],check=True)
 raw=Path(tmp)/'mask.rgba'
 subprocess.run(['java','-Djava.awt.headless=true','-cp',tmp,'com.boop.eyes.PngCoverageData',str(ROOT/'unified/animation/assets/boop-png-study.png'),str(raw)],check=True)
 rig=raw.read_bytes()

def alpha(x,y):
 # Numeric GL_LINEAR sampling along a texel-centred source column.
 v=max(0,min(639,y-.5));lo=int(math.floor(v));hi=min(639,lo+1)
 return mix(rig[(lo*1536+x)*4+3]/255,rig[(hi*1536+x)*4+3]/255,v-lo)

corners={'outer left':range(180,231),'inner left':range(728,745),
         'inner right':range(790,807),'outer right':range(1330,1371)}
for closure in (.5,.77,1.):
 for name,columns in corners.items():
  points=[]
  for x in columns:
   edge=(rig[x*4]*256+rig[x*4+1])/32
   top=min(rig[x*4+2]*640/255,edge-3)
   if edge>=640 or top<=0:continue
   end=mix(edge,642,closure)
   for sy in range(math.ceil(top+4),math.floor(edge-3)):
    target=top+(sy+.5-top)*(end-top)/(edge-top)
    if not 0<target<640 or target>=end-2:continue
    sampled=evaluate(warp,dict(y=target,top=top,edge=edge,end=end))
    fixed=alpha(x,target);moving=alpha(x,sampled)
    if fixed<.02 and moving>.9:points.append((x,target,fixed,moving))
  assert len(points)>=5,(name,closure,'needs actual photographic fixtures',len(points))
  x,y,fixed,moving=max(points,key=lambda p:p[3]-p[2])
  result=evaluate(alpha_expr,dict(baseAlpha=fixed,lidAlpha=moving,cover=1))
  print(name,closure,'destination',x,round(y,2),'fixed',round(fixed,3),'warped',round(moving,3),'output',round(result,3),flush=True)
  assert result>.9,(name,closure,'moving photographic outline is still clipped to open footprint',x,y,result)
# Open-pose arithmetic uses the exact existing output branch.
assert 'if(closure<=0.000001)' in shader
assert 'rgb=mix(rgb,fabric(lid),cover);' in shader
assert 'gl_FragColor=vec4(rgb*alpha,alpha);' in shader
for a in (0,.05,.5,1):
 for cover in (0,.2,.75,1):
  for base,lid in ((0,.7),(.3,.9),(.8,.1)):
   values=dict(baseRgb=base,lidRgb=lid,baseAlpha=a,lidAlpha=a,cover=cover)
   assert abs(evaluate(rgb_expr,values)-mix(base,lid,cover)*a)<1e-12
   assert abs(evaluate(alpha_expr,values)-a)<1e-12
# Opaque centre preserves the existing colour; alpha replacement clears old-only
# silhouette instead of leaving a second layer beneath a transparent warped edge.
for cover in (0,.25,.5,1):
 values=dict(baseRgb=.4,lidRgb=.8,baseAlpha=1,lidAlpha=1,cover=cover)
 assert abs(evaluate(rgb_expr,values)-mix(.4,.8,cover))<1e-12
assert evaluate(alpha_expr,dict(baseAlpha=1,lidAlpha=0,cover=1))==0
assert evaluate(rgb_expr,dict(baseRgb=1,lidRgb=.8,baseAlpha=0,lidAlpha=.5,cover=1))==.4
for closure in (0,.5,.77,1):
 for y in range(20,640):
  edge=640;end=mix(edge,642,closure)
  sy=y+.5 if closure==0 else evaluate(warp,dict(y=y+.5,top=0,edge=edge,end=end))
  assert alpha(768,y+.5)==0 and alpha(768,sy)==0,'Empty eye gap must stay transparent'
print('PASS actual four-corner coverage, open/central identity, premultiplied edges and transparent gap')
