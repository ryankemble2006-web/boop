// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <stdio.h>
#include "../native/wind_episode.h"
#include "../native/wind_state.h"
static void drive(HaWindEpisode *e,HaWind *w,uint32_t now){
 uint32_t at;int target=ha_episode_target(e,now,&at);ha_wind_set(w,target,at);
}
int main(void){
 HaWindEpisode e={0}; HaWind w;ha_wind_init(&w,0);
 ha_episode_level(&e,1);assert(ha_episode_begin(&e,1000));
 drive(&e,&w,1000);assert(ha_wind_strength(&w,1000)==0);
 drive(&e,&w,1850);assert(ha_wind_strength(&w,1850)==1);
 ha_episode_level(&e,1); // repeated poll/reconnect cannot extend or rearm
 drive(&e,&w,5150);assert(ha_wind_strength(&w,5150)==1);
 assert(w.target==0 && w.changed==5150);
 drive(&e,&w,5575);assert(ha_wind_strength(&w,5575)==0.5);
 drive(&e,&w,6000);assert(ha_wind_strength(&w,6000)==0);
 assert(!ha_episode_begin(&e,6001));
 ha_episode_level(&e,1);assert(!ha_episode_begin(&e,9000));
 ha_episode_level(&e,0);ha_episode_level(&e,1);assert(ha_episode_begin(&e,10000));
 // OI owns the scene, but the original episode clock continues.
 assert(ha_episode_begin(&e,12000));assert(e.begin==10000);
 assert(!ha_episode_begin(&e,15100));
 ha_episode_level(&e,1);assert(!ha_episode_begin(&e,15200));
 // Early off and reversal retain strength; only an actual new on rearms.
 ha_episode_level(&e,0);ha_episode_level(&e,1);assert(ha_episode_begin(&e,20000));
 ha_wind_init(&w,20000);drive(&e,&w,20000);
 ha_episode_level(&e,0);drive(&e,&w,20425);
 assert(ha_wind_strength(&w,20425)==0.5 && w.target==0);
 double halfway=ha_wind_strength(&w,20850);
 ha_episode_level(&e,1);drive(&e,&w,20850);
 assert(ha_wind_strength(&w,20850)==halfway);
 assert(e.begin==20850);
 // Missed rendering frames cannot push lowering beyond the five-second deadline.
 drive(&e,&w,26000);assert(ha_wind_strength(&w,26000)==0);
 ha_episode_level(&e,0);ha_episode_level(&e,1);
 assert(ha_episode_begin(&e,UINT32_MAX-2000));
 uint32_t at;assert(!ha_episode_target(&e,2999,&at));assert(!e.armed);
 puts("five-second total, repeated-level suppression, rearm, OI wall-clock expiry and smooth early-off reversal passed");
}
