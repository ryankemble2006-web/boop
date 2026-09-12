#include "PcmQueue.h"
#include <cassert>
#include <thread>
#include <iostream>
int main(){
    PcmQueue<8> q; int16_t input[]={1,2,3,4,5,6,7,8,9},out[12]={};
    assert(q.push(input,9)==8);assert(q.pop(out,3)==3);assert(out[0]==1&&out[2]==3);
    assert(q.push(input,3)==3);assert(q.pop(out,8)==8);
    for(int i=0;i<5;i++)assert(out[i]==i+4);
    assert(out[5]==1&&out[7]==3);assert(q.pop(out,1)==0);
    q.push(input,4);q.discard();assert(q.pop(out,4)==0);
    PcmQueue<128> concurrent;
    std::thread producer([&](){for(int i=0;i<200000;i++){int16_t value=i%32767;while(!concurrent.push(&value,1))std::this_thread::yield();}});
    for(int i=0;i<200000;i++){int16_t value=-1;while(!concurrent.pop(&value,1))std::this_thread::yield();assert(value==i%32767);}
    producer.join();std::cout<<"PCM queue: wrap, capacity, discard and 200000 concurrent samples passed\n";
}
