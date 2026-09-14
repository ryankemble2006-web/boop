// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <math.h>
#include <stdio.h>
#include "../native/wind_state.h"
#include "../native/control.h"
static int near(double a,double b){return fabs(a-b)<0.000001;}
int main(void){
 HaWind w; ha_wind_init(&w,0);
 assert(near(ha_wind_strength(&w,1000),0));
 ha_wind_set(&w,1,1000);
 assert(near(ha_wind_strength(&w,1000),0));
 assert(near(ha_wind_strength(&w,1425),0.5));
 ha_wind_set(&w,1,1425); // repeated observations cannot restart the lift
 assert(near(ha_wind_strength(&w,1850),1));
 ha_wind_set(&w,0,2000);
 assert(near(ha_wind_strength(&w,2425),0.5));
 ha_wind_set(&w,1,2425);
 assert(near(ha_wind_strength(&w,2425),0.5));
 assert(near(ha_wind_strength(&w,2850),0.75));
 assert(near(ha_wind_strength(&w,3275),1));
 ha_wind_set(&w,0,3300); assert(near(ha_wind_strength(&w,4150),0));
 ha_wind_init(&w,UINT32_MAX-400); ha_wind_set(&w,1,UINT32_MAX-400);
 assert(near(ha_wind_strength(&w,449),1));
 for(unsigned t=0;t<20000;t+=7){
  int frame=ha_wind_frame(1,t/1000.0);
  assert(frame>=0&&frame<=16);
  assert(ha_wind_flap(1,t/1000.0)>=0&&ha_wind_flap(1,t/1000.0)<=2);
 }
 assert(ha_wind_flap(0.19,1)==1);
 assert(ha_wind_round(2.5)==2 && ha_wind_round(3.5)==4 && ha_wind_round(-2.5)==-2);
 HaControl c; ha_control_init(&c);
 ha_set_wind_level(&c,1); assert(!ha_preempt_normal(&c)); // early level retained while assets load
 assert(atomic_load(&c.wind_on)==1);
 atomic_store(&c.wind_ready,1);
 ha_set_wind_level(&c,1); assert(ha_preempt_normal(&c));
 atomic_store(&c.wind_playing,1); assert(!ha_preempt_normal(&c));
 atomic_store(&c.night,1); assert(ha_queue_oi(&c)); assert(ha_preempt_wait(&c));
 unsigned token; assert(!ha_begin_oi(&c,&token)); // wind owns scene until cleaned
 atomic_store(&c.wind_playing,0); assert(ha_begin_oi(&c,&token));
 assert(!ha_preempt_wait(&c)); // sustained fan cannot interrupt OI
 ha_set_wind_level(&c,0); ha_finish_oi(&c,token,0);
 assert(!ha_preempt_normal(&c)); // off during OI prevents wind return
 ha_set_wind_level(&c,1); assert(ha_preempt_normal(&c));
 puts("wind levels, 850ms continuity, wrap, pose bounds and OI ownership passed");
}
