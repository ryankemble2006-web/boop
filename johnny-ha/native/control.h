// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_CONTROL_H
#define HA_CONTROL_H
#include <stdatomic.h>
#include <stdint.h>
#include <stddef.h>
typedef struct {
 atomic_int stop, night, fan_playing, oi_lock, wind_on, wind_playing, wind_ready;
 atomic_uint pending;
 unsigned oi_generation, oi_active_generation;
 int oi_pending, oi_playing;
} HaControl;
static inline void ha_control_init(HaControl *c) {
 atomic_init(&c->stop,0); atomic_init(&c->night,0); atomic_init(&c->fan_playing,0); atomic_init(&c->pending,0);
 atomic_init(&c->wind_on,0); atomic_init(&c->wind_playing,0); atomic_init(&c->wind_ready,0);
 atomic_init(&c->oi_lock,0); c->oi_generation=1; c->oi_active_generation=0; c->oi_pending=c->oi_playing=0;
}
static inline void ha_set_wind_level(HaControl *c,int on) { atomic_store(&c->wind_on,!!on); }
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
static inline void ha_oi_lock(HaControl *c) {
 while(atomic_exchange_explicit(&c->oi_lock,1,memory_order_acquire)) {}
}
static inline void ha_oi_unlock(HaControl *c) { atomic_store_explicit(&c->oi_lock,0,memory_order_release); }
static inline int ha_queue_oi(HaControl *c) {
 ha_oi_lock(c);
 int accepted=atomic_load(&c->night) && !atomic_load(&c->stop);
 if(accepted && !(c->oi_playing && c->oi_active_generation==c->oi_generation)) c->oi_pending=1;
 ha_oi_unlock(c); return accepted;
}
static inline void ha_cancel_oi(HaControl *c) {
 ha_oi_lock(c); c->oi_pending=0; c->oi_generation++; ha_oi_unlock(c);
}
static inline int ha_oi_is_pending(HaControl *c) {
 ha_oi_lock(c); int result=c->oi_pending; ha_oi_unlock(c); return result;
}
static inline int ha_try_begin_wind(HaControl *c) {
 ha_oi_lock(c);
 int result=atomic_load(&c->wind_ready) && atomic_load(&c->wind_on) && !atomic_load(&c->stop) &&
     !atomic_load(&c->wind_playing) && !c->oi_playing && !c->oi_pending;
 if(result)atomic_store(&c->wind_playing,1);
 ha_oi_unlock(c);return result;
}
static inline int ha_begin_oi(HaControl *c,unsigned *generation) {
 ha_oi_lock(c);
 int result=c->oi_pending && !c->oi_playing && !atomic_load(&c->wind_playing) && atomic_load(&c->night) &&
     !atomic_load(&c->stop) && !atomic_load(&c->fan_playing) && !atomic_load(&c->pending);
 if(result) {
  c->oi_pending=0; c->oi_playing=1;
  *generation=c->oi_active_generation=c->oi_generation;
 }
 ha_oi_unlock(c); return result;
}
static inline int ha_oi_is_current(HaControl *c,unsigned generation) {
 ha_oi_lock(c);
 int result=c->oi_playing && c->oi_active_generation==generation && c->oi_generation==generation &&
     atomic_load(&c->night) && !atomic_load(&c->stop);
 ha_oi_unlock(c); return result;
}
static inline void ha_finish_oi(HaControl *c,unsigned generation,int interrupted_by_fan) {
 ha_oi_lock(c);
 if(c->oi_playing && c->oi_active_generation==generation) {
  if(interrupted_by_fan && c->oi_generation==generation && atomic_load(&c->night) && !atomic_load(&c->stop))
    c->oi_pending=1;
  c->oi_playing=0;
 }
 ha_oi_unlock(c);
}
static inline int ha_preempt_normal(HaControl *c) {
 ha_oi_lock(c);
 int result=!atomic_load(&c->fan_playing) && !atomic_load(&c->wind_playing) && !c->oi_playing &&
     (atomic_load(&c->pending)>0 || (c->oi_pending && atomic_load(&c->night)) ||
      (atomic_load(&c->wind_on) && atomic_load(&c->wind_ready)));
 ha_oi_unlock(c); return result;
}
static inline int ha_preempt_wait(HaControl *c) {
 ha_oi_lock(c);
 int result=0;
 if(!atomic_load(&c->fan_playing)) {
  if(c->oi_playing)
   result=c->oi_active_generation!=c->oi_generation || !atomic_load(&c->night) || atomic_load(&c->pending)>0;
  else if(atomic_load(&c->wind_playing)) result=c->oi_pending && atomic_load(&c->night);
  else result=atomic_load(&c->pending)>0 || (c->oi_pending && atomic_load(&c->night)) ||
      (atomic_load(&c->wind_on) && atomic_load(&c->wind_ready));
 }
 ha_oi_unlock(c); return result;
}
/* Completion wins over a simultaneous fan edge: a finished reaction cannot replay. */
static inline int ha_oi_exit_reason(unsigned elapsed_ms,int fan_pending) {
 return elapsed_ms>=3150 ? 1 : (fan_pending ? 2 : 0);
}
static inline int ha_oi_frame(unsigned elapsed_ms) {
 if(elapsed_ms<250 || elapsed_ms>=3150) return -1;
 if(elapsed_ms<350) return 2;
 if(elapsed_ms<450) return 1;
 if(elapsed_ms<600) return 0;
 if(elapsed_ms<750) return 1;
 if(elapsed_ms<1100) return 2;
 if(elapsed_ms<1550) return 3;
 if(elapsed_ms<2000) return 4;
 if(elapsed_ms<2500) return 3;
 if(elapsed_ms<2900) return 2;
 if(elapsed_ms<3050) return 1;
 return 2;
}
static inline uint32_t ha_remaining_ms(uint32_t start,uint32_t now,uint32_t delay) {
 uint32_t elapsed=now-start; return elapsed>=delay?0:delay-elapsed;
}
static inline int ha_frame_fits(unsigned w,unsigned h,size_t pitch,size_t length) {
 return w==640 && h==480 && pitch>=2560 && pitch<=SIZE_MAX/480 && length>=pitch*480;
}
#endif
