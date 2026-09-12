#include "InputQueue.h"
#include <cassert>
int main(){
 InputQueue q; unsigned key=0;bool down=false;
 q.change(13,true);q.change(13,false);
 assert(q.pop(key,down)&&key==13&&down);
 assert(q.pop(key,down)&&key==13&&!down);
 assert(!q.pop(key,down));assert(!q.held(13));
 q.change(273,true);q.change(273,true);
 assert(q.pop(key,down)&&key==273&&down);assert(!q.pop(key,down));
 q.clear();assert(q.pop(key,down)&&key==273&&!down);assert(!q.held(273));
 q.change(10000,true);assert(!q.pop(key,down));
 for(int i=0;i<1500;i++){q.change(13,true);q.change(13,false);}
 q.clear();assert(!q.held(13));
}
