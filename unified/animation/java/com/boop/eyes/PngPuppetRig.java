package com.boop.eyes;
/** Builds raw (not premultiplied) RG edge / A silhouette data from the photographic hero.
 * Black exterior is flood-filled; enclosed black pupils remain opaque for full blinks. */
public final class PngPuppetRig {
 private PngPuppetRig(){}
 public static byte[] create(int[] pixels,int w,int h){
  if(w<3||h<3||pixels==null||pixels.length!=w*h)throw new IllegalArgumentException("Complete hero required");
  boolean[] exterior=new boolean[w*h];int[] queue=new int[w*h];int tail=0;
  for(int x=0;x<w;x++){tail=seed(x,pixels,exterior,queue,tail);tail=seed((h-1)*w+x,pixels,exterior,queue,tail);}
  for(int y=1;y<h-1;y++){tail=seed(y*w,pixels,exterior,queue,tail);tail=seed(y*w+w-1,pixels,exterior,queue,tail);}
  for(int head=0;head<tail;head++){
   int i=queue[head],x=i%w,y=i/w;
   if(x>0)tail=seed(i-1,pixels,exterior,queue,tail);
   if(x<w-1)tail=seed(i+1,pixels,exterior,queue,tail);
   if(y>0)tail=seed(i-w,pixels,exterior,queue,tail);
   if(y<h-1)tail=seed(i+w,pixels,exterior,queue,tail);
  }
  int[] edges=new int[w],tops=new int[w];
  for(int x=0;x<w;x++){
   edges[x]=h;int run=0;
   for(int y=0;y<h;y++){if(!exterior[y*w+x]){tops[x]=y;break;}}
   for(int y=Math.min(140,h-1);y<h;y++){
    int p=pixels[y*w+x],r=(p>>>16)&255,g=(p>>>8)&255,b=p&255;
    boolean white=Math.min(r,Math.min(g,b))>88 && Math.max(r,Math.max(g,b))-Math.min(r,Math.min(g,b))<58;
    run=white?run+1:0;
    if(run==12){edges[x]=Math.max(140,y-11-3);break;}
   }
  }
  // Thin bright fibre runs are not eye whites. Reject narrow boundary spikes
  // against nearby columns, retaining the photographed curve everywhere else.
  int[] measured=edges.clone();
  for(int x=10;x<w-10;x++){
   int[] nearby=new int[21];
   for(int n=0;n<21;n++)nearby[n]=measured[x+n-10];
   java.util.Arrays.sort(nearby);
   if(measured[x]<h&&nearby[10]<h&&Math.abs(measured[x]-nearby[10])>30)edges[x]=nearby[10];
  }
  // White onset locates the eye, but its soft grey shadow is NOT felt.
  // Find the last sustained dark lip before that rise, never an isolated fibre.
  for(int x=0;x<w;x++){
   if(edges[x]>=h)continue;
   int darkRun=0;
   for(int y=edges[x]-1;y>=Math.max(140,edges[x]-32);y--){
    darkRun=peak(pixels[y*w+x])<=24?darkRun+1:0;
    if(darkRun==3){edges[x]=y+3;break;}
   }
  }
  // A dark felt seam can connect to the black backdrop through the cap ends.
  // It is still solid material. Bound each column's body by sustained visible
  // runs; leave isolated outer wisps and their soft exterior alpha untouched.
  int[] bodyTop=new int[w],bodyBottom=new int[w];
  java.util.Arrays.fill(bodyTop,h);
  java.util.Arrays.fill(bodyBottom,-1);
  for(int x=0;x<w;x++){
   int run=0;
   for(int y=0;y<h;y++){
    run=peak(pixels[y*w+x])>16?run+1:0;
    if(run>=12){
     if(bodyTop[x]==h)bodyTop[x]=y-11;
     bodyBottom[x]=y;
    }
   }
  }
  // Follow the first sustained cap component, allowing short dark texture gaps.
  // Detached reflections below a corner are not part of that felt component.
  int[] capTop=new int[w],capBottom=new int[w];
  boolean[] corner=new boolean[w];
  java.util.Arrays.fill(capTop,h);
  java.util.Arrays.fill(capBottom,-1);
  for(int x=0;x<w;x++){
   int run=0,dark=0;
   for(int y=0;y<h;y++){
    boolean visible=peak(pixels[y*w+x])>2;
    if(capTop[x]==h){
     run=visible?run+1:0;
     if(run==12){capTop[x]=y-11;capBottom[x]=y;}
    }else{
     if(visible){capBottom[x]=y;dark=0;}
     else if(++dark==12)break;
    }
   }
   if(capBottom[x]-capTop[x]>=11&&(edges[x]>=h||capBottom[x]+1<edges[x]-4)){
    corner[x]=true;
    edges[x]=Math.min(edges[x],capBottom[x]+1);
   }
  }
  // The photo's lower lip is a broad curve. Reject isolated corner reflections
  // using valid neighbouring cap boundaries, without crossing an empty eye gap.
  int[] joined=edges.clone();
  for(int x=10;x<w-10;x++){
   if(!corner[x])continue;
   int[] nearby=new int[21];int n=0;
   for(int k=x-10;k<=x+10;k++)if(joined[k]<h)nearby[n++]=joined[k];
   if(n>=5){java.util.Arrays.sort(nearby,0,n);edges[x]=Math.min(nearby[n/2],capBottom[x]+1);}
  }
  byte[] out=new byte[w*h*4];
  for(int x=0;x<w;x++){
   int edge=edges[x]*32;
   for(int y=0;y<h;y++){
    int i=y*w+x,at=i*4;
    out[at]=(byte)(edge>>>8);out[at+1]=(byte)edge;
    out[at+2]=(byte)Math.min(255,Math.round(tops[x]*255f/h));
    boolean solid=(y>=bodyTop[x]&&y<=bodyBottom[x])
     ||(corner[x]&&y>=capTop[x]&&y<=capBottom[x]);
    out[at+3]=(byte)(solid||!exterior[i]?255:Math.min(255,peak(pixels[i])*255/16));
   }
  }
  return out;
 }
 private static int peak(int p){return Math.max((p>>>16)&255,Math.max((p>>>8)&255,p&255));}
 private static int seed(int i,int[] p,boolean[] seen,int[] q,int tail){
  if(!seen[i]&&peak(p[i])<=16){seen[i]=true;q[tail++]=i;}return tail;
 }
}
