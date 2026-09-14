// SPDX-License-Identifier: GPL-3.0-or-later
#ifndef HA_MUSIC_POSE_H
#define HA_MUSIC_POSE_H
/* Original archive sprites, inspected in the private extraction:
 * play: JOHNWALK.BMP 16(front stand),33/34/35(front steps).
 * pause: MJTELE.BMP18..21(original puzzled shrug),16(question mark).
 * No sprites are redrawn, rotated or resampled. All whole actors share a
 * bottom-center foot anchor(380,299), with only integer hop/side offsets.
 */
typedef struct { int sprite,shift,hop,question; } HaMusicPose;
static inline HaMusicPose ha_music_pose(int kind,unsigned elapsed) {
 HaMusicPose p={-1,0,0,0};
 if(kind==1) {
  if(elapsed>=4800)return p;
  p.sprite=16;
  if((elapsed>=350 && elapsed<2450)||(elapsed>=2850 && elapsed<3250)||(elapsed>=3650 && elapsed<4350)){
   unsigned start=elapsed<2450?350:(elapsed<3250?2850:3650);
   unsigned beat=(elapsed-start)/(elapsed<2450?150:(elapsed<3250?100:140))%4;
   const int sprites[]={33,35,34,35},shifts[]={-2,0,2,0};
   p.sprite=sprites[beat];p.shift=shifts[beat];
  } else if((elapsed>=2450&&elapsed<2850)||(elapsed>=3250&&elapsed<3650)){
   unsigned t=elapsed-(elapsed<2850?2450:3250);
   p.hop=(int)(32*t*(400-t)/160000); // small integer-pixel parabolic hop
  }
 } else if(kind==2) {
  if(elapsed>=2600)return p;
  p.sprite=elapsed<250?18:elapsed<450?19:elapsed<700?20:elapsed<1600?21:elapsed<1850?20:elapsed<2150?21:18;
  p.question=elapsed>=700&&elapsed<2150;
 }
 return p;
}
#endif
