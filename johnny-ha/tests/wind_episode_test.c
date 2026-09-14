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
 drive(&e,&w,8150);assert(ha_wind_strength(&w,8150)==1);
 assert(w.target==0 && w.changed==8150);
 drive(&e,&w,8575);assert(ha_wind_strength(&w,8575)==0.5);
 drive(&e,&w,9000);assert(ha_wind_strength(&w,9000)==0);
 assert(!ha_episode_begin(&e,9001));
 ha_episode_level(&e,1);assert(!ha_episode_begin(&e,9900));
 ha_episode_level(&e,0);ha_episode_level(&e,1);assert(ha_episode_begin(&e,10000));
 // OI owns the scene, but the original episode clock continues.
 assert(ha_episode_begin(&e,12000));assert(e.begin==10000);
 assert(!ha_episode_begin(&e,18100));
 ha_episode_level(&e,1);assert(!ha_episode_begin(&e,18200));
 // Early off and reversal retain strength; only an actual new on rearms.
 ha_episode_level(&e,0);ha_episode_level(&e,1);assert(ha_episode_begin(&e,20000));
 ha_wind_init(&w,20000);drive(&e,&w,20000);
 ha_episode_level(&e,0);drive(&e,&w,20425);
 assert(ha_wind_strength(&w,20425)==0.5 && w.target==0);
 double halfway=ha_wind_strength(&w,20850);
 ha_episode_level(&e,1);drive(&e,&w,20850);
 assert(ha_wind_strength(&w,20850)==halfway);
 assert(e.begin==20850);
 // Missed rendering frames cannot push lowering beyond the eight-second deadline.
 drive(&e,&w,29000);assert(ha_wind_strength(&w,29000)==0);
 ha_episode_level(&e,0);ha_episode_level(&e,1);
 assert(ha_episode_begin(&e,UINT32_MAX-2000));
 uint32_t at;assert(!ha_episode_target(&e,5999,&at));assert(!e.armed);
 puts("eight-second total, repeated-level suppression, rearm, OI wall-clock expiry and smooth early-off reversal passed");
}
