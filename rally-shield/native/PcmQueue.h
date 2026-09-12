#pragma once
#include <array>
#include <atomic>
#include <algorithm>
#include <cstdint>
#include <cstddef>

// Single producer (emulation), single consumer (AAudio). Consumer owns discard.
template<size_t Capacity> class PcmQueue {
    std::array<int16_t,Capacity> samples{};
    alignas(64) std::atomic<uint64_t> write{0};
    alignas(64) std::atomic<uint64_t> read{0};
public:
    size_t push(const int16_t* data,size_t count) {
        uint64_t w=write.load(std::memory_order_relaxed),r=read.load(std::memory_order_acquire);
        size_t n=std::min(count,Capacity-static_cast<size_t>(w-r));
        for(size_t i=0;i<n;i++)samples[(w+i)%Capacity]=data[i];
        write.store(w+n,std::memory_order_release);return n;
    }
    size_t pop(int16_t* data,size_t count) {
        uint64_t r=read.load(std::memory_order_relaxed),w=write.load(std::memory_order_acquire);
        size_t n=std::min(count,static_cast<size_t>(w-r));
        for(size_t i=0;i<n;i++)data[i]=samples[(r+i)%Capacity];
        read.store(r+n,std::memory_order_release);return n;
    }
    void discard(){read.store(write.load(std::memory_order_acquire),std::memory_order_release);}
};
