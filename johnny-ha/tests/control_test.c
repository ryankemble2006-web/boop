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
 atomic_store(&c.stop,1); assert(!ha_queue_fan(&c));
 puts("control queue, wrap-safe clock, cancellation, framebuffer bounds passed");
}
