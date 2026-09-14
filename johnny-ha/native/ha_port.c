// SPDX-License-Identifier: GPL-3.0-or-later
#include "ha_port.h"
#include "control.h"
#include "oi_render.h"
#include <jni.h>
#include <pthread.h>
#include <setjmp.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "mytypes.h"
#include "resource.h"
#include "graphics.h"
#include "story.h"
#include "island.h"

typedef struct Owned { void *ptr; int kind; struct Owned *next; } Owned;
typedef struct Player {
 HaControl controls;
 pthread_mutex_t frame_lock;
 uint32_t frame[640*480];
 unsigned frames, started, completed;
 uint32_t oi_pixels[HA_OI_PIXELS];
 atomic_int oi_ready;
 unsigned oi_token, oi_started, oi_completed;
 int oi_frame;
 char path[1024], status[192];
} Player;
static pthread_mutex_t engine_lock=PTHREAD_MUTEX_INITIALIZER;
static Player *active;
static Owned *owned;
static jmp_buf unwind;
static uint32_t last_tick;
static void status(const char *text) {
 pthread_mutex_lock(&active->frame_lock);
 snprintf(active->status,sizeof(active->status),"%s",text);
 pthread_mutex_unlock(&active->frame_lock);
}
void ha_fail(const char *why) { status(why); longjmp(unwind,2); }
void ha_check_stop(void) { if(atomic_load(&active->controls.stop)) longjmp(unwind,1); }
static void track(void *p,int kind) {
 if(!p) ha_fail("native allocation failed");
 Owned *n=malloc(sizeof(*n));
 if(!n) {
   if(kind==0) free(p); else if(kind==1) SDL_FreeSurface(p); else fclose(p);
   ha_fail("native ownership allocation failed");
 }
 n->ptr=p; n->kind=kind; n->next=owned; owned=n;
}
static void forget(void *p,int kind) {
 if(!p) return;
 Owned **at=&owned;
 while(*at) {
   if((*at)->ptr==p && (*at)->kind==kind) { Owned *n=*at; *at=n->next; free(n); return; }
   at=&(*at)->next;
 }
 ha_fail("native ownership mismatch");
}
void *ha_malloc(size_t n) { void *p=malloc(n?n:1); track(p,0); return p; }
void ha_free(void *p) { if(p) { forget(p,0); free(p); } }
SDL_Surface *ha_surface(Uint32 flags,int w,int h,int depth,Uint32 r,Uint32 g,Uint32 b,Uint32 a) {
 SDL_Surface *p=SDL_CreateRGBSurface(flags,w,h,depth,r,g,b,a); track(p,1); return p;
}
SDL_Surface *ha_surface_from(void *pixels,int w,int h,int depth,int pitch,Uint32 r,Uint32 g,Uint32 b,Uint32 a) {
 SDL_Surface *p=SDL_CreateRGBSurfaceFrom(pixels,w,h,depth,pitch,r,g,b,a); track(p,1); return p;
}
void ha_free_surface(SDL_Surface *p) { if(p) { forget(p,1); SDL_FreeSurface(p); } }
FILE *ha_fopen(const char *name,const char *mode) {
 char path[1200];
 // Engine resource map may choose a filename, never an external path.
 if(strchr(name,'/') || strchr(name,'\\') || strstr(name,"..")) return NULL;
 if(snprintf(path,sizeof(path),"%s/%s",active->path,name)>=(int)sizeof(path)) return NULL;
 FILE *p=fopen(path,mode); if(p) track(p,2); return p;
}
int ha_fclose(FILE *p) { forget(p,2); return fclose(p); }
static void cleanup(void) {
 // SDL surfaces may reference separately owned pixel buffers. Free surfaces first.
 for(int kind=2;kind>=0;kind--) {
   Owned **at=&owned;
   while(*at) {
     Owned *n=*at;
     if(n->kind==kind) {
       *at=n->next;
       if(kind==2) fclose(n->ptr); else if(kind==1) SDL_FreeSurface(n->ptr); else free(n->ptr);
       free(n);
     } else at=&n->next;
   }
 }
}
static uint32_t ticks(void) {
 struct timespec t; clock_gettime(CLOCK_MONOTONIC,&t);
 return (uint32_t)((uint64_t)t.tv_sec*1000+t.tv_nsec/1000000);
}
void ha_wait(unsigned delay) {
 if(!delay) { last_tick=ticks(); ha_check_stop(); return; }
 unsigned left;
 while((left=ha_remaining_ms(last_tick,ticks(),delay))) {
   ha_check_stop();
   if(ha_preempt_wait(&active->controls)) break;
   struct timespec t={0,(long)(left>5?5:left)*1000000L};
   nanosleep(&t,NULL);
 }
 last_tick=ticks(); ha_check_stop();
}
unsigned ha_clock_ms(void) { return ticks(); }
int ha_begin_oi_request(void) {
 if(!atomic_load(&active->oi_ready) || !ha_begin_oi(&active->controls,&active->oi_token)) return 0;
 active->oi_frame=-1;
 pthread_mutex_lock(&active->frame_lock); active->oi_started++; pthread_mutex_unlock(&active->frame_lock);
 return 1;
}
int ha_oi_active(void) { return ha_oi_is_current(&active->controls,active->oi_token); }
void ha_set_oi_frame(unsigned elapsed) { active->oi_frame=ha_oi_frame(elapsed); }
void ha_end_oi(int completed,int interrupted_by_fan) {
 if(completed && ha_oi_active()) {
  pthread_mutex_lock(&active->frame_lock); active->oi_completed++; pthread_mutex_unlock(&active->frame_lock);
 }
 ha_finish_oi(&active->controls,active->oi_token,interrupted_by_fan);
 active->oi_frame=-1;
}
int ha_should_preempt(void) { return ha_preempt_normal(&active->controls); }
int ha_night(void) { return atomic_load(&active->controls.night); }
int ha_fan_pending(void) { return atomic_load(&active->controls.pending)>0; }
int ha_take_fan_request(void) { return ha_take_fan(&active->controls); }
void ha_fan_started(void) {
 atomic_store(&active->controls.fan_playing,1);
 pthread_mutex_lock(&active->frame_lock); active->started++; pthread_mutex_unlock(&active->frame_lock);
}
void ha_fan_finished(void) {
 atomic_store(&active->controls.fan_playing,0);
 pthread_mutex_lock(&active->frame_lock); active->completed++; pthread_mutex_unlock(&active->frame_lock);
}
void ha_present(SDL_Surface *s) {
 ha_check_stop();
 if(!s || !ha_frame_fits(s->w,s->h,s->pitch,(size_t)s->pitch*s->h)) ha_fail("invalid framebuffer");
 if(SDL_LockSurface(s)) ha_fail("framebuffer lock failed");
 pthread_mutex_lock(&active->frame_lock);
 for(unsigned y=0;y<480;y++) {
   uint32_t *row=(uint32_t *)((uint8_t *)s->pixels+y*s->pitch);
   for(unsigned x=0;x<640;x++) active->frame[y*640+x]=row[x]|0xff000000u;
 }
 if(atomic_load(&active->oi_ready) && ha_oi_active())
   ha_oi_compose(active->frame,active->oi_pixels,active->oi_frame);
 active->frames++;
 pthread_mutex_unlock(&active->frame_lock);
 SDL_UnlockSurface(s);
}
#define JNI_NAME(name) Java_local_johnnycastaway_shield_NativeJohnnyView_##name
JNIEXPORT jlong JNICALL JNI_NAME(nCreate)(JNIEnv *env,jclass cls,jstring path,jboolean night) {
 (void)cls;
 Player *p=calloc(1,sizeof(*p)); if(!p) return 0;
 const char *s=(*env)->GetStringUTFChars(env,path,NULL);
 if(!s) { free(p); return 0; }
 int length=snprintf(p->path,sizeof(p->path),"%s",s);
 (*env)->ReleaseStringUTFChars(env,path,s);
 if(length>=(int)sizeof(p->path)) { free(p); return 0; }
 ha_control_init(&p->controls); atomic_store(&p->controls.night,night?1:0);
 atomic_init(&p->oi_ready,0); p->oi_frame=-1;
 pthread_mutex_init(&p->frame_lock,NULL); strcpy(p->status,"starting");
 return (jlong)(intptr_t)p;
}
JNIEXPORT void JNICALL JNI_NAME(nRun)(JNIEnv *env,jclass cls,jlong handle) {
 (void)env; (void)cls;
 Player *p=(Player *)(intptr_t)handle;
 pthread_mutex_lock(&engine_lock); active=p;
 int reason=setjmp(unwind);
 if(!reason) {
   ha_check_stop();
   memset(&islandState,0,sizeof(islandState));
   parseResourceFiles("RESOURCE.MAP");
   graphicsInit(); status("playing");
   storyPlay();
 }
 cleanup(); graphicsEnd();
 if(reason==1) status("stopped");
 active=NULL; pthread_mutex_unlock(&engine_lock);
}
JNIEXPORT void JNICALL JNI_NAME(nStop)(JNIEnv *e,jclass c,jlong h) {
 (void)e;(void)c; atomic_store(&((Player *)(intptr_t)h)->controls.stop,1);
}
JNIEXPORT void JNICALL JNI_NAME(nNight)(JNIEnv *e,jclass c,jlong h,jboolean night) {
 (void)e;(void)c; atomic_store(&((Player *)(intptr_t)h)->controls.night,night?1:0);
 if(!night) ha_cancel_oi(&((Player *)(intptr_t)h)->controls);
}
JNIEXPORT jboolean JNICALL JNI_NAME(nOiAssets)(JNIEnv *e,jclass c,jlong h,jintArray pixels) {
 (void)c; Player *p=(Player *)(intptr_t)h;
 if(!pixels || (*e)->GetArrayLength(e,pixels)!=HA_OI_PIXELS) return JNI_FALSE;
 (*e)->GetIntArrayRegion(e,pixels,0,HA_OI_PIXELS,(jint *)p->oi_pixels);
 if((*e)->ExceptionCheck(e)) return JNI_FALSE;
 atomic_store(&p->oi_ready,1); return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL JNI_NAME(nOi)(JNIEnv *e,jclass c,jlong h) {
 (void)e;(void)c; Player *p=(Player *)(intptr_t)h;
 return atomic_load(&p->oi_ready) && ha_queue_oi(&p->controls) ? JNI_TRUE : JNI_FALSE;
}
JNIEXPORT void JNICALL JNI_NAME(nCancelOi)(JNIEnv *e,jclass c,jlong h) {
 (void)e;(void)c; ha_cancel_oi(&((Player *)(intptr_t)h)->controls);
}
JNIEXPORT jboolean JNICALL JNI_NAME(nFan)(JNIEnv *e,jclass c,jlong h) {
 (void)e;(void)c; return ha_queue_fan(&((Player *)(intptr_t)h)->controls)?JNI_TRUE:JNI_FALSE;
}
JNIEXPORT jboolean JNICALL JNI_NAME(nCopy)(JNIEnv *e,jclass c,jlong h,jintArray pixels) {
 (void)c; Player *p=(Player *)(intptr_t)h;
 if((*e)->GetArrayLength(e,pixels)!=640*480) return JNI_FALSE;
 pthread_mutex_lock(&p->frame_lock);
 int valid=p->frames>0;
 if(valid) (*e)->SetIntArrayRegion(e,pixels,0,640*480,(jint *)p->frame);
 pthread_mutex_unlock(&p->frame_lock); return valid?JNI_TRUE:JNI_FALSE;
}
JNIEXPORT jstring JNICALL JNI_NAME(nStatus)(JNIEnv *e,jclass c,jlong h) {
 (void)c; Player *p=(Player *)(intptr_t)h; char text[320];
 pthread_mutex_lock(&p->frame_lock);
 snprintf(text,sizeof(text),"%s frames=%u fan=%u/%u queued=%u night=%d oi=%u/%u oiPending=%d oiReady=%d",p->status,p->frames,p->completed,p->started,atomic_load(&p->controls.pending),atomic_load(&p->controls.night),p->oi_completed,p->oi_started,ha_oi_is_pending(&p->controls),atomic_load(&p->oi_ready));
 pthread_mutex_unlock(&p->frame_lock); return (*e)->NewStringUTF(e,text);
}
JNIEXPORT void JNICALL JNI_NAME(nDestroy)(JNIEnv *e,jclass c,jlong h) {
 (void)e;(void)c; Player *p=(Player *)(intptr_t)h;
 pthread_mutex_destroy(&p->frame_lock); free(p);
}
