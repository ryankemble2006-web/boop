// SPDX-License-Identifier: GPL-3.0-or-later
// Exercise the actual ownership/frame/cancellation bridge with synthetic data only.
#include <assert.h>
#include "../native/ha_port.c"
struct TIslandState islandState;
void parseResourceFiles(char *path) { (void)path; ha_fail("synthetic parser failure"); }
void graphicsInit(void) {}
void graphicsEnd(void) {}
void storyPlay(void) {}
int main(void) {
 Player *p=calloc(1,sizeof(*p)); assert(p);
 ha_control_init(&p->controls);
 pthread_mutex_init(&p->frame_lock,NULL);
 strcpy(p->path,"/tmp");
 active=p;
 for(int i=0;i<100;i++) {
   atomic_store(&p->controls.stop,0);
   int reason=setjmp(unwind);
   if(!reason) {
     void *pixels=ha_malloc(16);
     ha_surface_from(pixels,2,2,32,8,0,0,0,0);
     ha_malloc(64);
     FILE *file=ha_fopen("johnny-native-synthetic-test.tmp","wb");
     assert(file);
     assert(!ha_fopen("../outside","rb"));
     SDL_Surface *frame=ha_surface(0,640,480,32,0x00ff0000,0x0000ff00,0x000000ff,0);
     SDL_FillRect(frame,NULL,0x00123456);
     ha_present(frame);
     assert(p->frame[0]==0xff123456 && p->frame[640*480-1]==0xff123456);
     atomic_store(&p->controls.stop,1);
     ha_check_stop();
     assert(0);
   }
   assert(reason==1);
   cleanup();
   assert(owned==NULL);
 }
 assert(p->frames==100);
 atomic_store(&p->controls.stop,0);
 assert(ha_queue_fan(&p->controls));
 last_tick=ticks();
 uint32_t began=last_tick;
 ha_wait(1000);
 assert(ticks()-began<500); // queued fan wakes a long normal-scene delay
 assert(atomic_load(&p->controls.pending)==1);
 atomic_store(&p->controls.fan_playing,1);
 last_tick=ticks(); began=last_tick;
 ha_wait(40);
 assert(ticks()-began>=40); // same queued request cannot shorten active fan timing
 atomic_store(&p->controls.fan_playing,0);
 assert(ha_take_fan(&p->controls));
 assert(SDL_WasInit(SDL_INIT_VIDEO|SDL_INIT_AUDIO)==0);
 int reason=setjmp(unwind);
 if(!reason) ha_fail("expected failure");
 assert(reason==2);
 assert(!strcmp(p->status,"expected failure"));
 cleanup();
 pthread_mutex_destroy(&p->frame_lock);
 free(p); active=NULL;
 remove("/tmp/johnny-native-synthetic-test.tmp");
 puts("100 cancellation generations, allocation/surface/file cleanup, ARGB frame corners, error containment, no audio/video init passed");
 return 0;
}
