// SPDX-License-Identifier: GPL-3.0-or-later
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "../native/oi_render.h"
int main(void) {
 uint32_t *frame=malloc(640*480*4), *assets=calloc(HA_OI_PIXELS,4);
 assert(frame && assets);
 for(int i=0;i<640*480;i++)frame[i]=0xff010203;
 ha_oi_compose(frame,assets,-1);
 assert(frame[218*640+341]==0xff010203);
 for(int i=0;i<5*80*90;i++)assets[i]=0xffff0000;
 for(int i=5*80*90;i<HA_OI_PIXELS;i++)assets[i]=0xff0000ff;
 ha_oi_compose(frame,assets,3);
 assert(frame[218*640+341]==0xff0000ff); // bubble follows body and tail
 assert(frame[307*640+420]==0xffff0000); // final body pixel at exact pose origin
 assert(frame[160*640+284]==0xff0000ff);
 assert(frame[159*640+284]==0xff010203);
 uint32_t pixel=0xff000000, transparent=0, red=0x80ff0000;
 ha_argb_blit(frame,&transparent,1,1,0,0); assert(frame[0]==0xff010203);
 frame[0]=pixel;ha_argb_blit(frame,&red,1,1,0,0);assert(frame[0]==0xff800000);
 ha_argb_blit(frame,&red,1,1,-1,-1); // bounded clipping
 ha_fill_triangle(frame,0,0,4,0,0,4,0xff123456);
 assert(frame[0]==0xff123456 && frame[3*640+3]!=0xff123456);
 free(frame);free(assets);
 puts("synthetic ARGB placement, bubble/tail order, transparent pixels, alpha and clipping passed");
}
