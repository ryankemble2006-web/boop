// SPDX-License-Identifier: GPL-3.0-or-later
// Actual patched ADS cleanup and actual upstream TTM/graphics cleanup, synthetic allocations only.
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include "control.h"
#include "ads.c"
static HaControl control;
static int allocations, surfaces;
int ha_should_preempt(void) { return ha_preempt_normal(&control); }
void *ha_malloc(size_t n) { void *p=malloc(n?n:1); assert(p); allocations++; return p; }
void ha_free(void *p) { if(p){assert(allocations>0);allocations--;free(p);} }
SDL_Surface *ha_surface(Uint32 f,int w,int h,int d,Uint32 r,Uint32 g,Uint32 b,Uint32 a) {
 SDL_Surface *p=SDL_CreateRGBSurface(f,w,h,d,r,g,b,a);assert(p);surfaces++;return p;
}
SDL_Surface *ha_surface_from(void *p,int w,int h,int d,int pitch,Uint32 r,Uint32 g,Uint32 b,Uint32 a) {
 SDL_Surface *s=SDL_CreateRGBSurfaceFrom(p,w,h,d,pitch,r,g,b,a);assert(s);surfaces++;return s;
}
void ha_free_surface(SDL_Surface *p) {if(p){assert(surfaces>0);surfaces--;SDL_FreeSurface(p);}}
void seed(void) {
 adsInit();
 adsTags=ha_malloc(sizeof(*adsTags));
 for(int i=0;i<3;i++) {
  ttmThreads[i].isRunning=1;
  ttmThreads[i].ttmLayer=grNewLayer();
  ttmSlots[i].data=(uint8 *)"synthetic resource";
  ttmSlots[i].tags=ha_malloc(sizeof(struct TTtmTag));
  ttmSlots[i].numSprites[0]=1;
  ttmSlots[i].sprites[0][0]=ha_surface_from(ha_malloc(16),2,2,32,8,0,0,0,0);
 }
 numThreads=3;
 grCopyZoneToBg(ttmThreads[0].ttmLayer,0,0,2,2);
}
int main(void) {
 ha_control_init(&control);
 for(int round=0;round<100;round++) {
  seed();
  assert(!adsPollFanInterrupt());
  assert(numThreads==3);
  assert(ha_queue_fan(&control));
  atomic_store(&control.fan_playing,1);
  assert(!adsPollFanInterrupt()); // queued next fan cannot truncate an active fan
  assert(numThreads==3 && surfaces==7);
  atomic_store(&control.fan_playing,0);
  assert(adsPollFanInterrupt());
  assert(numThreads==0 && adsStopRequested==1);
  adsFinishRoutine();
  assert(allocations==0 && surfaces==0);
  for(int i=0;i<3;i++) assert(!ttmSlots[i].data && !ttmSlots[i].numSprites[0]);
  assert(ha_take_fan(&control)); // interrupt never consumes the command itself
  assert(!ha_take_fan(&control));
 }
 seed();
 adsClearMusicActor();
 assert(!ttmThreads[0].isRunning && !ttmSlots[0].data && !ttmSlots[0].numSprites[0]);
 assert(numThreads==2); // music cleanup releases only its owned actor
 adsFinishRoutine(); // common finish path also releases any still-live scene
 assert(!numThreads && !allocations && !surfaces);
 puts("100 ADS interruptions release TTM layers, tags, sprites, saved zones and ADS tags; active fan protected and queued command retained");
 return 0;
}
