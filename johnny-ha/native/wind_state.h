// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_WIND_STATE_H
#define HA_WIND_STATE_H
#include <stdint.h>
#include <math.h>
typedef struct { uint32_t changed; double initial; int target; } HaWind;
static inline void ha_wind_init(HaWind *w,uint32_t now) { w->changed=now;w->initial=0;w->target=0; }
static inline double ha_wind_strength(const HaWind *w,uint32_t now) {
 double t=(uint32_t)(now-w->changed)/850.0;
 if(t>1)t=1;
 t=t*t*(3-2*t);
 return w->initial+(w->target-w->initial)*t;
}
static inline void ha_wind_set(HaWind *w,int on,uint32_t now) {
 on=!!on;
 if(on==w->target)return;
 w->initial=ha_wind_strength(w,now);w->changed=now;w->target=on;
}
/* C# Math.Round uses ties-to-even, including negative leaf sway. */
static inline int ha_wind_round(double value) {
 double base=floor(value),fraction=value-base;
 return (int)(base+(fraction>0.5 || (fraction==0.5 && fmod(base,2)!=0)));
}
static inline int ha_wind_frame(double power,double seconds) {
 int frame=ha_wind_round(power*(14.7+1.2*sin(seconds*9)));
 return frame<0?0:(frame>16?16:frame);
}
static inline int ha_wind_flap(double power,double seconds) {
 const int frames[]={0,1,2,1};return power<0.2?1:frames[(unsigned)(seconds*9)%4];
}
static inline int ha_wind_wrap(double x,int width) {return (int)fmod(fmod(x,width)+width,width);}
#endif
