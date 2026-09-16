#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include "../native/control.h"
int main(void) {
 HaControl c; ha_control_init(&c);
 assert(!ha_take_fan(&c));
 for(int i=0;i<8;i++) assert(ha_queue_fan(&c));
 assert(!ha_queue_fan(&c));
 for(int i=0;i<8;i++) assert(ha_take_fan(&c));
 assert(!ha_take_fan(&c));
 atomic_store(&c.night,1); assert(atomic_load(&c.night)==1);
 assert(ha_remaining_ms(100,110,20)==10);
 assert(ha_remaining_ms(UINT32_MAX-9,5,20)==5);
 assert(ha_remaining_ms(100,150,20)==0);
 assert(ha_frame_fits(640,480,2560,1228800));
 assert(!ha_frame_fits(639,480,2560,1228800));
 assert(!ha_frame_fits(640,480,2559,1228800));
 assert(!ha_frame_fits(640,480,2560,1228799));
 assert(ha_queue_fan(&c));
 assert(ha_preempt_normal(&c));
 atomic_store(&c.fan_playing,1);
 assert(!ha_preempt_normal(&c));
 assert(ha_queue_fan(&c)); // another real edge waits instead of interrupting this fan
 assert(!ha_preempt_normal(&c));
 atomic_store(&c.fan_playing,0);
 assert(ha_preempt_normal(&c));
 assert(ha_take_fan(&c) && ha_take_fan(&c));
 assert(!ha_preempt_normal(&c));
 atomic_store(&c.stop,1); assert(!ha_queue_fan(&c));

 // A real light edge is an urgent environment change. It wakes an ordinary
 // routine and its interruptible waits, but duplicate steady-state samples do not create more work.
 HaControl light; ha_control_init(&light);
 assert(!ha_environment_pending(&light));
 assert(ha_set_night(&light,1));
 assert(atomic_load(&light.night)==1);
 assert(ha_environment_pending(&light));
 assert(ha_preempt_normal(&light));
 assert(ha_preempt_wait(&light));
 unsigned night_token=ha_environment_token(&light);
 ha_environment_applied(&light,night_token);
 assert(!ha_environment_pending(&light));
 assert(!ha_preempt_normal(&light));
 assert(!ha_preempt_wait(&light));
 assert(!ha_set_night(&light,1));
 assert(!ha_environment_pending(&light));
 assert(!ha_preempt_normal(&light));

 // The active fan emergency keeps ownership. The light edge remains pending
 // and becomes urgent as soon as the protected fan scene finishes.
 atomic_store(&light.fan_playing,1);
 assert(ha_set_night(&light,0));
 assert(ha_environment_pending(&light));
 assert(!ha_preempt_normal(&light));
 assert(!ha_preempt_wait(&light));
 atomic_store(&light.fan_playing,0);
 assert(ha_preempt_normal(&light));
 unsigned day_token=ha_environment_token(&light);
 ha_environment_applied(&light,day_token);
 assert(!ha_environment_pending(&light));
 assert(!ha_preempt_normal(&light));

 puts("control queue, light urgency, wrap-safe clock, cancellation, framebuffer bounds passed");
}
