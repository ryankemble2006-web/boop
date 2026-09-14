// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_WIND_EPISODE_H
#define HA_WIND_EPISODE_H
#include <stdint.h>
typedef struct { int on,armed,started; uint32_t begin; } HaWindEpisode;
static inline void ha_episode_level(HaWindEpisode *e,int on) {
 on=!!on;if(e->on==on)return;
 e->on=on;e->armed=on;
 if(on)e->started=0;
}
static inline int ha_episode_begin(HaWindEpisode *e,uint32_t now) {
 if(!e->armed || !e->on)return 0;
 if(!e->started){e->started=1;e->begin=now;}
 /* No fresh lift if OI used the remainder of this episode. */
 if((uint32_t)(now-e->begin)>=7150){e->armed=0;return 0;}
 return 1;
}
static inline int ha_episode_target(HaWindEpisode *e,uint32_t now,uint32_t *transition) {
 *transition=now;
 if(!e->armed || !e->on)return 0;
 if(!e->started){e->started=1;e->begin=now;}
 uint32_t elapsed=now-e->begin;
 if(elapsed<7150)return 1;
 *transition=e->begin+7150;
 if(elapsed>=8000)e->armed=0;
 return 0;
}
#endif
