// SPDX-License-Identifier: GPL-3.0-or-later
// Deterministically enumerate the actual patched selector, without game resources.
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
static int next_index;
static int controlled_rand(void) { return next_index; }
#define rand controlled_rand
#include "story.c"
#undef rand
void ha_fail(const char *why) { fprintf(stderr,"%s\\n",why); abort(); }
static int is_fan(struct TStoryScene *s) {
 return !strcmp(s->adsName,"MISCGAG.ADS") && s->adsTagNo==1;
}
int main(void) {
 for(int day=1;day<=11;day++) {
  storyCurrentDay=day;
  for(int mode=0;mode<2;mode++) {
   unsigned wanted=mode?0:FINAL, unwanted=mode?FINAL:0;
   int seen[NUM_SCENES]={0}, expected=0, found=0;
   for(int i=0;i<NUM_SCENES;i++) {
    struct TStoryScene *s=&storyScenes[i];
    if((s->flags&wanted)==wanted && !(s->flags&unwanted) &&
       (!s->dayNo || s->dayNo==day) && !is_fan(s)) expected++;
   }
   for(next_index=0;next_index<NUM_SCENES;next_index++) {
    struct TStoryScene *s=storyPickScene(wanted,unwanted);
    assert(!is_fan(s));
    seen[s-storyScenes]=1;
   }
   for(int i=0;i<NUM_SCENES;i++) found+=seen[i];
   assert(found==expected && found>0);
  }
 }
 assert(is_fan(ha_fan_scene()));
 puts("all automatic choices across 11 story days exclude HA fan; explicit fan descriptor retained");
 return 0;
}
