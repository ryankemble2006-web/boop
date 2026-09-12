// SPDX-License-Identifier: GPL-2.0-or-later
#pragma once
#include <array>
#include <deque>
#include <mutex>
#include <utility>
/** Keep short down/up taps even when both arrive between emulated frames. */
class InputQueue {
    std::array<bool,512> held_{};
    std::deque<std::pair<unsigned,bool>> events_;
    std::mutex mutex_;
public:
    void change(unsigned key,bool down){
        if(key>=held_.size())return;
        std::lock_guard<std::mutex> guard(mutex_);
        if(held_[key]==down)return;
        if(events_.size()>=1024){
            events_.clear();
            for(unsigned i=0;i<held_.size();i++)events_.emplace_back(i,false);
            held_.fill(false);
        }
        held_[key]=down;events_.emplace_back(key,down);
    }
    void clear(){for(unsigned i=0;i<held_.size();i++)change(i,false);}
    bool held(unsigned key){
        std::lock_guard<std::mutex> guard(mutex_);return key<held_.size()&&held_[key];
    }
    bool pop(unsigned& key,bool& down){
        std::lock_guard<std::mutex> guard(mutex_);
        if(events_.empty())return false;
        key=events_.front().first;down=events_.front().second;events_.pop_front();return true;
    }
};
