// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_OI_RENDER_H
#define HA_OI_RENDER_H
#include <stdint.h>
enum { HA_OI_PIXELS=5*80*90+92*64 };
static inline void ha_argb_blit(uint32_t *out,const uint32_t *in,int w,int h,int x,int y) {
 for(int sy=0;sy<h;sy++) for(int sx=0;sx<w;sx++) {
  int dx=x+sx,dy=y+sy;
  if(dx<0 || dx>=640 || dy<0 || dy>=480) continue;
  uint32_t s=in[sy*w+sx],a=s>>24;
  if(!a) continue;
  uint32_t *d=&out[dy*640+dx];
  if(a==255) {*d=s;continue;}
  uint32_t r=(((s>>16)&255)*a+((*d>>16)&255)*(255-a)+127)/255;
  uint32_t g=(((s>>8)&255)*a+((*d>>8)&255)*(255-a)+127)/255;
  uint32_t b=((s&255)*a+(*d&255)*(255-a)+127)/255;
  *d=0xff000000u|(r<<16)|(g<<8)|b;
 }
}
static inline int ha_edge(int ax,int ay,int bx,int by,int px2,int py2) {
 return (bx-ax)*(py2-2*ay)-(by-ay)*(px2-2*ax);
}
static inline void ha_fill_triangle(uint32_t *out,int ax,int ay,int bx,int by,int cx,int cy,uint32_t color) {
 if(ha_edge(ax,ay,bx,by,cx*2,cy*2)<0) {
  int t=bx;bx=cx;cx=t;t=by;by=cy;cy=t;
 }
 int minx=ax<bx?ax:bx; if(cx<minx)minx=cx;
 int maxx=ax>bx?ax:bx; if(cx>maxx)maxx=cx;
 int miny=ay<by?ay:by; if(cy<miny)miny=cy;
 int maxy=ay>by?ay:by; if(cy>maxy)maxy=cy;
 if(minx<0)minx=0;
 if(miny<0)miny=0;
 if(maxx>639)maxx=639;
 if(maxy>479)maxy=479;
 for(int y=miny;y<=maxy;y++)for(int x=minx;x<=maxx;x++)
  if(ha_edge(ax,ay,bx,by,2*x+1,2*y+1)>=0 &&
     ha_edge(bx,by,cx,cy,2*x+1,2*y+1)>=0 &&
     ha_edge(cx,cy,ax,ay,2*x+1,2*y+1)>=0)out[y*640+x]=color;
}
static inline void ha_oi_compose(uint32_t *frame,const uint32_t *assets,int pose) {
 if(pose<0 || pose>=5)return;
 ha_argb_blit(frame,assets+pose*80*90,80,90,341,218);
 ha_fill_triangle(frame,347,214,379,245,359,215,0xff000000u);
 ha_fill_triangle(frame,350,214,376,241,357,215,0xfffcfcfcu);
 ha_argb_blit(frame,assets+5*80*90,92,64,284,160);
}
#endif
