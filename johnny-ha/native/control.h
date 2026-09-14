// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_CONTROL_H
#define HA_CONTROL_H
#include <stdatomic.h>
#include <stdint.h>
#include <stddef.h>
typedef struct { atomic_int stop, night, fan_playing; atomic_uint pending; } HaControl;
static inline void ha_control_init(HaControl *c) {
 atomic_init(&c->stop,0); atomic_init(&c->night,0); atomic_init(&c->fan_playing,0); atomic_init(&c->pending,0);
}
static inline int ha_queue_fan(HaControl *c) {
 if(atomic_load(&c->stop)) return 0;
 unsigned p=atomic_load(&c->pending);
 while(p<8) if(atomic_compare_exchange_weak(&c->pending,&p,p+1)) return 1;
 return 0;
}
static inline int ha_take_fan(HaControl *c) {
 unsigned p=atomic_load(&c->pending);
 while(p) if(atomic_compare_exchange_weak(&c->pending,&p,p-1)) return 1;
 return 0;
}
static inline int ha_preempt_normal(HaControl *c) {
 return atomic_load(&c->pending)>0 && !atomic_load(&c->fan_playing);
}
static inline uint32_t ha_remaining_ms(uint32_t start,uint32_t now,uint32_t delay) {
 uint32_t elapsed=now-start; return elapsed>=delay?0:delay-elapsed;
}
static inline int ha_frame_fits(unsigned w,unsigned h,size_t pitch,size_t length) {
 return w==640 && h==480 && pitch>=2560 && pitch<=SIZE_MAX/480 && length>=pitch*480;
}
#endif
