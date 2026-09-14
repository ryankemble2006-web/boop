// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <stdio.h>
#include "../native/control.h"
#include "../native/music_pose.h"
int main(void){
 HaControl c;ha_control_init(&c);unsigned token;int kind;
 assert(!ha_queue_music(&c,0,100));
 assert(ha_queue_music(&c,1,100));
 assert(ha_preempt_normal(&c));
 assert(ha_begin_music(&c,110,&kind,&token)&&kind==1);
 assert(ha_music_current(&c,token)&&!ha_preempt_wait(&c));
 assert(ha_queue_music(&c,1,200));assert(ha_music_current(&c,token)); // same event coalesces
 assert(ha_queue_music(&c,2,300));assert(!ha_music_current(&c,token));
 ha_finish_music(&c,token);
 assert(ha_begin_music(&c,310,&kind,&token)&&kind==2);
 atomic_store(&c.night,1);assert(ha_queue_oi(&c));
 assert(!ha_music_current(&c,token)&&ha_preempt_wait(&c));
 assert(!ha_queue_music(&c,1,320));
 unsigned oi;assert(!ha_begin_oi(&c,&oi)); // cleanup first; never two actors
 ha_finish_music(&c,token);assert(ha_begin_oi(&c,&oi));
 assert(!ha_queue_music(&c,1,400));
 ha_finish_oi(&c,oi,0);assert(!ha_begin_music(&c,500,&kind,&token));
 assert(ha_queue_music(&c,1,1000));assert(!ha_begin_music(&c,2500,&kind,&token));
 assert(!ha_preempt_normal(&c)); // expired pending event gone
 assert(ha_queue_music(&c,1,3000));assert(ha_begin_music(&c,3001,&kind,&token));
 atomic_store(&c.wind_ready,1);ha_set_wind_level(&c,1);
 assert(!ha_music_current(&c,token));assert(!ha_try_begin_wind(&c,3002));
 ha_finish_music(&c,token);assert(ha_try_begin_wind(&c,3003));
 assert(!ha_queue_music(&c,2,3100));atomic_store(&c.wind_playing,0);
 ha_set_wind_level(&c,0);assert(!ha_begin_music(&c,4000,&kind,&token)); // never replay after HA
 assert(ha_queue_music(&c,2,UINT32_MAX-100));assert(ha_begin_music(&c,100,&kind,&token));
 ha_cancel_music(&c);assert(!ha_music_current(&c,token));ha_finish_music(&c,token);
 for(unsigned t=0;t<4800;t++){
  HaMusicPose p=ha_music_pose(1,t);assert(p.sprite==16||p.sprite==33||p.sprite==34||p.sprite==35);
  assert(p.hop>=0&&p.hop<=8);assert(!p.question);
 }
 assert(ha_music_pose(1,4800).sprite<0);
 for(unsigned t=0;t<2600;t++){HaMusicPose p=ha_music_pose(2,t);assert(p.sprite>=18&&p.sprite<=21);}
 assert(ha_music_pose(2,700).question);assert(!ha_music_pose(2,2200).question);
 assert(ha_music_pose(2,2600).sprite<0);
 puts("music latest/coalescing/expiry, HA priority/drop, exclusive ownership and4800/2600ms pose bounds passed");
}
