// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_WIND_RENDER_H
#define HA_WIND_RENDER_H
#include <stdlib.h>
#include "oi_render.h"
#include "wind_state.h"
#define HA_WIND_FRAME_PIXELS (240*240)
#define HA_WIND_FRAMES 102
static inline void ha_wind_pose(uint32_t *frame,const uint32_t *pose,int direction) {
 ha_argb_blit(frame,pose,240,240,451-(direction==1?120:119),91);
}
static inline void ha_wind_line(uint32_t *frame,int pitch,int x,int y,int x2,int y2,uint32_t color) {
 int dx=abs(x2-x),sx=x<x2?1:-1,dy=-abs(y2-y),sy=y<y2?1:-1,err=dx+dy;
 for(;;) {
  if(x>=0&&x<640&&y>=0&&y<480)frame[y*pitch+x]=color;
  if(x==x2&&y==y2)break;
  int e=2*err;
  if(e>=dy){err+=dy;x+=sx;}
  if(e<=dx){err+=dx;y+=sy;}
 }
}
static inline void ha_wind_gust_pixels(uint32_t *frame,int pitch,double power,double travel,int direction) {
 if(power<=0.04)return;
 int count=(int)(26*power);
 for(int i=0;i<count;i++){
  int x=ha_wind_wrap(i*173+travel*(1+(i%3)*0.18),740)-50;
  int y=82+(i*47)%235,len=14+i%5*6;
  ha_wind_line(frame,pitch,x,y,x+direction*len,y,i%3==0?0xff54a8a8u:0xffa8fcfcu);
  if(i%3==0){
   ha_wind_line(frame,pitch,x+direction*len,y,x+direction*(len+4),y-2,0xffa8fcfcu);
   ha_wind_line(frame,pitch,x+direction*(len+4),y-2,x+direction*(len+8),y-2,0xffa8fcfcu);
  }
 }
}
#endif
