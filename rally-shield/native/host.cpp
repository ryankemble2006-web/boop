// SPDX-License-Identifier: GPL-2.0-or-later
#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <aaudio/AAudio.h>
#include <atomic>
#include <array>
#include <map>
#include <string>
#include <mutex>
#include <chrono>
#include <thread>
#include <cstring>
#include <cstdio>
#include <cstdarg>
#include <algorithm>
#include "libretro.h"
#include "PcmQueue.h"
#include "InputQueue.h"
#include <exception>

namespace {
std::atomic<bool> stopped{false},paused{true},running{false};
std::atomic<double> frameRate{30.0};
std::atomic<unsigned> frameWidth{0},frameHeight{0};
std::atomic<uint64_t> frames{0},audioSamples{0},nonzeroSamples{0},keyEvents{0},audioDrops{0},audioShortfalls{0};
std::atomic<int> audioError{0};
InputQueue input;
retro_keyboard_event_t keyboardCallback=nullptr;
retro_pixel_format pixelFormat=RETRO_PIXEL_FORMAT_0RGB1555;
std::map<std::string,std::string> options;
std::string saveDirectory,systemDirectory;
ANativeWindow* window=nullptr;
std::mutex windowLock;
unsigned bufferWidth=0,bufferHeight=0;
PcmQueue<32768> audioQueue;

void logCore(retro_log_level level,const char* format,...) {
    va_list args;va_start(args,format);
    __android_log_vprint(level>=RETRO_LOG_ERROR?ANDROID_LOG_ERROR:ANDROID_LOG_INFO,"RallyCore",format,args);
    va_end(args);
}
void registerOptions(const retro_core_options_v2* definitions){
    if(!definitions || !definitions->definitions)return;
    for(auto* d=definitions->definitions;d->key;d++)
        if(options.find(d->key)==options.end() && d->default_value)options[d->key]=d->default_value;
}
bool environment(unsigned command,void* data) {
    switch(command){
        case RETRO_ENVIRONMENT_GET_SYSTEM_DIRECTORY: *static_cast<const char**>(data)=systemDirectory.c_str();return true;
        case RETRO_ENVIRONMENT_GET_SAVE_DIRECTORY: *static_cast<const char**>(data)=saveDirectory.c_str();return true;
        case RETRO_ENVIRONMENT_GET_CAN_DUPE: *static_cast<bool*>(data)=true;return true;
        case RETRO_ENVIRONMENT_GET_CORE_OPTIONS_VERSION: *static_cast<unsigned*>(data)=2;return true;
        case RETRO_ENVIRONMENT_SET_CORE_OPTIONS_V2: registerOptions(static_cast<retro_core_options_v2*>(data));return true;
        case RETRO_ENVIRONMENT_SET_CORE_OPTIONS_V2_INTL: registerOptions(static_cast<retro_core_options_v2_intl*>(data)->us);return true;
        case RETRO_ENVIRONMENT_SET_VARIABLES: {
            for(auto* v=static_cast<retro_variable*>(data);v && v->key;v++) {
                if(options.count(v->key)||!v->value)continue;
                std::string value(v->value);size_t start=value.find(';');if(start==std::string::npos)continue;
                start=value.find_first_not_of(' ',start+1);if(start==std::string::npos)continue;
                options[v->key]=value.substr(start,value.find('|',start)-start);
            }return true;
        }
        case RETRO_ENVIRONMENT_GET_VARIABLE: {
            auto* v=static_cast<retro_variable*>(data);auto i=options.find(v->key?v->key:"");
            v->value=i==options.end()?nullptr:i->second.c_str();return v->value!=nullptr;
        }
        case RETRO_ENVIRONMENT_SET_VARIABLE: {
            auto* v=static_cast<retro_variable*>(data);if(v->key && v->value)options[v->key]=v->value;return true;
        }
        case RETRO_ENVIRONMENT_GET_VARIABLE_UPDATE: *static_cast<bool*>(data)=false;return true;
        case RETRO_ENVIRONMENT_SET_KEYBOARD_CALLBACK: keyboardCallback=static_cast<retro_keyboard_callback*>(data)->callback;return true;
        case RETRO_ENVIRONMENT_SET_PIXEL_FORMAT: {
            auto format=*static_cast<retro_pixel_format*>(data);
            if(format!=RETRO_PIXEL_FORMAT_0RGB1555 && format!=RETRO_PIXEL_FORMAT_XRGB8888 && format!=RETRO_PIXEL_FORMAT_RGB565)return false;
            pixelFormat=format;return true;
        }
        case RETRO_ENVIRONMENT_GET_LOG_INTERFACE: static_cast<retro_log_callback*>(data)->log=logCore;return true;
        case RETRO_ENVIRONMENT_SET_SYSTEM_AV_INFO: {
            auto* av=static_cast<retro_system_av_info*>(data);
            if(av->timing.fps>=10 && av->timing.fps<=120)frameRate=av->timing.fps;return true;
        }
        case RETRO_ENVIRONMENT_SET_GEOMETRY: return true;
        case RETRO_ENVIRONMENT_GET_FASTFORWARDING: *static_cast<bool*>(data)=false;return true;
        case RETRO_ENVIRONMENT_GET_INPUT_BITMASKS: return false;
        case RETRO_ENVIRONMENT_GET_AUDIO_VIDEO_ENABLE: *static_cast<int*>(data)=3;return true;
        case RETRO_ENVIRONMENT_GET_MESSAGE_INTERFACE_VERSION: *static_cast<unsigned*>(data)=1;return true;
        case RETRO_ENVIRONMENT_SET_MESSAGE: logCore(RETRO_LOG_INFO,"%s",static_cast<retro_message*>(data)->msg);return true;
        case RETRO_ENVIRONMENT_SET_MESSAGE_EXT: logCore(RETRO_LOG_INFO,"%s",static_cast<retro_message_ext*>(data)->msg);return true;
        case RETRO_ENVIRONMENT_SET_INPUT_DESCRIPTORS:
        case RETRO_ENVIRONMENT_SET_CONTROLLER_INFO:
        case RETRO_ENVIRONMENT_SET_SUPPORT_NO_GAME:
        case RETRO_ENVIRONMENT_SET_CORE_OPTIONS_DISPLAY: return true;
        case RETRO_ENVIRONMENT_SHUTDOWN: stopped=true;return true;
        default:return false;
    }
}
void inputPoll(){
    unsigned key;bool down;
    while(input.pop(key,down)){
        keyEvents++;
        if(keyboardCallback)keyboardCallback(down,key,(key>=32 && key<127)?key:0,0);
    }
}
int16_t inputState(unsigned port,unsigned device,unsigned index,unsigned id){
    (void)port;(void)index;
    return (device==RETRO_DEVICE_KEYBOARD && input.held(id))?1:0;
}
void video(const void* data,unsigned width,unsigned height,size_t pitch){
    if(!data || data==RETRO_HW_FRAME_BUFFER_VALID || !width || !height || width>4096 || height>4096)return;
    unsigned bytes=pixelFormat==RETRO_PIXEL_FORMAT_XRGB8888?4:2;if(pitch<width*bytes)return;
    frames++;frameWidth=width;frameHeight=height;
    std::lock_guard<std::mutex> guard(windowLock);if(!window || paused || stopped)return;
    if(bufferWidth!=width || bufferHeight!=height){
        if(ANativeWindow_setBuffersGeometry(window,width,height,WINDOW_FORMAT_RGBA_8888)!=0)return;
        bufferWidth=width;bufferHeight=height;
    }
    ANativeWindow_Buffer buffer{};if(ANativeWindow_lock(window,&buffer,nullptr)!=0)return;
    if(buffer.width>=static_cast<int>(width) && buffer.height>=static_cast<int>(height) && buffer.stride>=static_cast<int>(width)){
        for(unsigned y=0;y<height;y++){
            auto* dest=static_cast<uint32_t*>(buffer.bits)+y*buffer.stride;
            const auto* row=static_cast<const uint8_t*>(data)+y*pitch;
            for(unsigned x=0;x<width;x++){
                unsigned r,g,b;
                if(pixelFormat==RETRO_PIXEL_FORMAT_XRGB8888){uint32_t p;std::memcpy(&p,row+x*4,4);r=(p>>16)&255;g=(p>>8)&255;b=p&255;}
                else{uint16_t p;std::memcpy(&p,row+x*2,2);b=(p&31)*255/31;
                    if(pixelFormat==RETRO_PIXEL_FORMAT_RGB565){r=(p>>11)*255/31;g=((p>>5)&63)*255/63;}
                    else{r=((p>>10)&31)*255/31;g=((p>>5)&31)*255/31;}}
                dest[x]=0xff000000u|(b<<16)|(g<<8)|r;
            }
        }
    }
    ANativeWindow_unlockAndPost(window);
}
size_t audioBatch(const int16_t* data,size_t frameCount){
    if(!data || paused || stopped)return frameCount;
    size_t count=frameCount*2;audioSamples+=count;
    uint64_t nonzero=0;for(size_t i=0;i<count;i++)if(data[i]!=0)nonzero++;
    nonzeroSamples+=nonzero;size_t accepted=audioQueue.push(data,count);audioDrops+=count-accepted;return frameCount;
}
void audioSample(int16_t left,int16_t right){int16_t data[]={left,right};audioBatch(data,1);}
aaudio_data_callback_result_t audioCallback(AAudioStream*,void*,void* output,int32_t frameCount){
    auto* data=static_cast<int16_t*>(output);size_t count=static_cast<size_t>(frameCount)*2;
    if(paused || stopped){audioQueue.discard();std::memset(data,0,count*sizeof(int16_t));return AAUDIO_CALLBACK_RESULT_CONTINUE;}
    size_t received=audioQueue.pop(data,count);if(received<count){std::memset(data+received,0,(count-received)*sizeof(int16_t));audioShortfalls+=count-received;}
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}
void audioFailed(AAudioStream*,void*,aaudio_result_t error){audioError=error;}
AAudioStream* openAudio(){
    AAudioStreamBuilder* builder=nullptr;if(AAudio_createStreamBuilder(&builder)!=AAUDIO_OK)return nullptr;
    AAudioStreamBuilder_setDirection(builder,AAUDIO_DIRECTION_OUTPUT);
    AAudioStreamBuilder_setFormat(builder,AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setChannelCount(builder,2);
    AAudioStreamBuilder_setSampleRate(builder,48000);
    AAudioStreamBuilder_setSharingMode(builder,AAUDIO_SHARING_MODE_SHARED);
    AAudioStreamBuilder_setPerformanceMode(builder,AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setDataCallback(builder,audioCallback,nullptr);
    AAudioStreamBuilder_setErrorCallback(builder,audioFailed,nullptr);
    AAudioStream* stream=nullptr;auto result=AAudioStreamBuilder_openStream(builder,&stream);AAudioStreamBuilder_delete(builder);
    if(result!=AAUDIO_OK || !stream)return nullptr;
    if(AAudioStream_getFormat(stream)!=AAUDIO_FORMAT_PCM_I16 || AAudioStream_getChannelCount(stream)!=2 || AAudioStream_getSampleRate(stream)!=48000){AAudioStream_close(stream);return nullptr;}
    AAudioStream_setBufferSizeInFrames(stream,AAudioStream_getFramesPerBurst(stream)*3);
    if(AAudioStream_requestStart(stream)!=AAUDIO_OK){AAudioStream_close(stream);return nullptr;}return stream;
}
std::string utf(JNIEnv* env,jstring value){if(!value)return {};const char* p=env->GetStringUTFChars(value,nullptr);std::string s(p?p:"");if(p)env->ReleaseStringUTFChars(value,p);return s;}
}

extern "C" JNIEXPORT void JNICALL Java_com_boop_rally_NativeBridge_initialize(JNIEnv*,jclass){
    stopped=false;paused=true;frames=0;audioSamples=0;nonzeroSamples=0;keyEvents=0;audioDrops=0;audioShortfalls=0;audioError=0;
    input.clear();
}
extern "C" JNIEXPORT void JNICALL Java_com_boop_rally_NativeBridge_surface(JNIEnv* env,jclass,jobject surface){
    ANativeWindow* next=surface?ANativeWindow_fromSurface(env,surface):nullptr;
    std::lock_guard<std::mutex> guard(windowLock);if(window)ANativeWindow_release(window);window=next;bufferWidth=bufferHeight=0;
}
extern "C" JNIEXPORT void JNICALL Java_com_boop_rally_NativeBridge_key(JNIEnv*,jclass,jint key,jboolean down){if(key>0)input.change(static_cast<unsigned>(key),down);}
extern "C" JNIEXPORT void JNICALL Java_com_boop_rally_NativeBridge_pause(JNIEnv*,jclass,jboolean value){paused=value;if(value)input.clear();}
extern "C" JNIEXPORT void JNICALL Java_com_boop_rally_NativeBridge_stop(JNIEnv*,jclass){stopped=true;input.clear();}
extern "C" JNIEXPORT jstring JNICALL Java_com_boop_rally_NativeBridge_stats(JNIEnv* env,jclass){
    char result[384];std::snprintf(result,sizeof(result),"frames=%llu size=%ux%u output_fps=%.1f audio_samples=%llu audio_nonzero=%llu keys=%llu dropped=%llu underrun=%llu paused=%d audio_error=%d",
        (unsigned long long)frames.load(),frameWidth.load(),frameHeight.load(),frameRate.load(),(unsigned long long)audioSamples.load(),(unsigned long long)nonzeroSamples.load(),
        (unsigned long long)keyEvents.load(),(unsigned long long)audioDrops.load(),(unsigned long long)audioShortfalls.load(),paused?1:0,audioError.load());
    return env->NewStringUTF(result);
}
extern "C" JNIEXPORT jstring JNICALL Java_com_boop_rally_NativeBridge_run(JNIEnv* env,jclass,jstring gamePath,jstring saves,jstring system,jstring gameId){
    if(running.exchange(true))return env->NewStringUTF("A rally is already running.");
    std::string path=utf(env,gamePath),id=utf(env,gameId),failure;
    saveDirectory=utf(env,saves);systemDirectory=utf(env,system);
    if(path.empty() || (id!="rac93" && id!="rac96")){running=false;return env->NewStringUTF("The selected game is not available.");}
    options.clear();options["dosbox_pure_conf"]="inside";options["dosbox_pure_force60fps"]="30";
    options["dosbox_pure_audiorate"]="48000";options["dosbox_pure_auto_mapping"]="false";
    options["dosbox_pure_on_screen_keyboard"]="false";options["dosbox_pure_mouse_input"]="false";
    options["dosbox_pure_cpu_core"]="auto";options["dosbox_pure_memory_size"]="16";
    options["dosbox_pure_cycles"]=id=="rac93"?"13400":"max";
    options["dosbox_pure_aspect_correction"]="true";
    bool initialized=false,loaded=false;AAudioStream* stream=nullptr;
    try {
        retro_set_environment(environment);retro_set_video_refresh(video);retro_set_audio_sample(audioSample);retro_set_audio_sample_batch(audioBatch);
        retro_set_input_poll(inputPoll);retro_set_input_state(inputState);retro_init();initialized=true;
        retro_game_info info{};info.path=path.c_str();loaded=retro_load_game(&info);
        if(!loaded)failure="The rally engine could not load this game pack.";
        else if(!stopped){
            retro_set_controller_port_device(0,RETRO_DEVICE_JOYPAD);
            retro_system_av_info av{};retro_get_system_av_info(&av);if(av.timing.fps>=10 && av.timing.fps<=120)frameRate=av.timing.fps;
            stream=openAudio();if(!stream)failure="Audio output could not open. Return to the collection and try again.";
            else {
                auto next=std::chrono::steady_clock::now();
                while(!stopped && !audioError){
                    if(paused){std::this_thread::sleep_for(std::chrono::milliseconds(10));next=std::chrono::steady_clock::now();continue;}
                    retro_run();
                    auto period=std::chrono::microseconds(static_cast<int64_t>(1000000.0/frameRate.load()));next+=period;
                    auto now=std::chrono::steady_clock::now();if(next>now)std::this_thread::sleep_until(next);else if(now-next>std::chrono::milliseconds(150))next=now;
                }
                if(audioError)failure="Audio output disconnected. Return to the collection and try again.";
            }
        }
    }catch(const std::exception& e){logCore(RETRO_LOG_ERROR,"Native exception: %s",e.what());failure="The rally engine stopped unexpectedly.";}
    catch(...){failure="The rally engine stopped unexpectedly.";}
    paused=true;
    if(stream){AAudioStream_requestStop(stream);AAudioStream_close(stream);}
    if(loaded){inputPoll();retro_unload_game();}if(initialized)retro_deinit();
    running=false;return env->NewStringUTF(failure.c_str());
}
