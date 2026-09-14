// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <stdio.h>
#include "../native/control.h"
int main(void) {
 HaControl c; ha_control_init(&c); unsigned token=0;
 assert(!ha_queue_oi(&c));
 atomic_store(&c.night,1);
 assert(ha_queue_oi(&c) && ha_queue_oi(&c));
 assert(ha_oi_is_pending(&c));
 assert(ha_preempt_normal(&c));
 atomic_store(&c.fan_playing,1);
 assert(!ha_begin_oi(&c,&token));
 assert(!ha_preempt_normal(&c));
 atomic_store(&c.fan_playing,0);
 assert(ha_begin_oi(&c,&token));
 assert(!ha_begin_oi(&c,&token));
 assert(ha_oi_is_current(&c,token));
 assert(ha_queue_oi(&c)); // duplicate active request is coalesced, not queued
 assert(!ha_oi_is_pending(&c));
 ha_finish_oi(&c,token,0);
 assert(!ha_oi_is_pending(&c)); // later fan cannot replay a completed OI
 assert(ha_queue_fan(&c));
 assert(!ha_begin_oi(&c,&token));
 assert(ha_take_fan(&c));
 assert(ha_queue_oi(&c) && ha_begin_oi(&c,&token));
 assert(ha_queue_fan(&c));
 assert(ha_preempt_wait(&c)); // fan promptly takes priority over active OI
 ha_finish_oi(&c,token,1);
 assert(ha_oi_is_pending(&c)); // at most one pending OI behind this fan
 atomic_store(&c.fan_playing,1);
 assert(!ha_begin_oi(&c,&token));
 assert(!ha_preempt_wait(&c));
 atomic_store(&c.night,0); ha_cancel_oi(&c);
 assert(!ha_oi_is_pending(&c));
 atomic_store(&c.fan_playing,0); assert(ha_take_fan(&c));
 for(int i=0;i<100;i++) {
  atomic_store(&c.night,1);
  assert(ha_queue_oi(&c) && ha_begin_oi(&c,&token));
  atomic_store(&c.night,0);ha_cancel_oi(&c);
  assert(!ha_oi_is_current(&c,token) && ha_preempt_wait(&c));
  atomic_store(&c.night,1); assert(ha_queue_oi(&c));
  ha_finish_oi(&c,token,0);
  assert(ha_oi_is_pending(&c)); // old teardown does not erase new off edge
  assert(ha_begin_oi(&c,&token));
  ha_finish_oi(&c,token,0); assert(!ha_oi_is_pending(&c));
 }
 const unsigned times[]={0,249,250,349,350,449,450,599,600,749,750,1099,1100,1549,1550,1999,2000,2499,2500,2899,2900,3049,3050,3149,3150,4000};
 const int expected[]={-1,-1,2,2,1,1,0,0,1,1,2,2,3,3,4,4,3,3,2,2,1,1,2,2,-1,-1};
 for(unsigned i=0;i<sizeof(times)/sizeof(times[0]);i++) assert(ha_oi_frame(times[i])==expected[i]);
 puts("OI timing3150ms, full-fan priority, coalescing, cancellation and 100 rapid toggle generations passed");
}
