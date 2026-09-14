// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "../native/wind_render.h"
int main(void){
 uint32_t *frame=calloc(640*480,sizeof(uint32_t));
 uint32_t *pose=calloc(240*240,sizeof(uint32_t));
 pose[120*240+120]=0xff123456;
 ha_wind_pose(frame,pose,1);
 assert(frame[211*640+451]==0xff123456);
 pose[120*240+119]=0xffabcdef;
 ha_wind_pose(frame,pose,-1);
 assert(frame[211*640+451]==0xffabcdef);
 for(int i=0;i<640*480;i++) frame[i]=0;
 ha_wind_gust_pixels(frame,640,0,200,1);
 for(int i=0;i<640*480;i++) assert(!frame[i]);
 ha_wind_gust_pixels(frame,640,1,0,1);
 assert(frame[129*640+123]==0xffa8fcfc);
 for(int d=-1;d<=1;d+=2)for(int f=-2000;f<2000;f+=19)
  ha_wind_gust_pixels(frame,640,1,f,d);
 free(frame);free(pose);
 puts("wind anchor, no calm gusts, palette and clipped bidirectional gusts passed");
}
