# SPDX-License-Identifier: GPL-3.0-or-later
"""Apply source hooks to the pinned upstream; no original game data is included."""
from pathlib import Path
import re, sys
root=Path(sys.argv[1])
def read(name): return (root/name).read_text()
def write(name,s): (root/name).write_text(s)
def once(s,a,b):
    assert s.count(a)==1, (a,s.count(a))
    return s.replace(a,b)
def function(s,name,body):
    m=re.search(r"(?m)^(?:static )?(?:void|char \*)\s*"+name+r"\([^)]*\)\s*\{",s)
    assert m,name
    depth=1; end=m.end()
    while depth:
        depth += (s[end]=="{")-(s[end]=="}"); end+=1
    return s[:m.end()]+ "\n"+body+"\n"+s[end-1:]
# Graphics remain upstream software blits, with an owned framebuffer in place of a window.
s=read("graphics.c")
s=once(s,"static SDL_Window *sdl_window;","static SDL_Surface *ha_frame;")
s=function(s,"graphicsInit",'''    ha_frame = ha_surface(0,640,480,32,0x00ff0000,0x0000ff00,0x000000ff,0);
    if (!ha_frame) ha_fail("framebuffer allocation failed");
    grBackgroundSfc = NULL; grSavedZonesLayer = NULL;
    grDx=grDy=0; grUpdateDelay=0; ha_rebuilding_island=0;
    grLoadPalette(palResources[0]);
    srand(time(NULL));
    eventsInit();''')
s=function(s,"graphicsEnd","    ha_frame=NULL; grBackgroundSfc=NULL; grSavedZonesLayer=NULL;")
s=function(s,"grRefreshDisplay","    ha_present(ha_frame);")
s=function(s,"grToggleFullScreen","    /* Android View owns sizing; no display mode changes. */")
s=s.replace("SDL_GetWindowSurface(sdl_window)","ha_frame").replace("SDL_UpdateWindowSurface(sdl_window)","ha_present(ha_frame)")
# Live night rebuild must retain an in-progress routine's saved image layer.
s=once(s,"static SDL_Surface *grSavedZonesLayer = NULL;","static SDL_Surface *grSavedZonesLayer = NULL;\nint ha_rebuilding_island=0;")
s=s.replace("if (grSavedZonesLayer != NULL)\n        grReleaseSavedLayer();",
            "if (grSavedZonesLayer != NULL && !ha_rebuilding_island)\n        grReleaseSavedLayer();")
write("graphics.c",s)
# No Android audio/video/events initialization. The engine ticks are interruptible.
write("events.c",'''#include "ha_port.h"
#include "mytypes.h"
int evHotKeysEnabled=0;
void eventsInit(void) { ha_wait(0); }
void eventsWaitTick(uint16 delay) { ha_wait((unsigned)delay*20); }
''')
write("sound.c",'''int soundDisabled=1;
void soundInit(void) {}
void soundEnd(void) {}
void soundPlay(int ignored) { (void)ignored; }
''')
s=read("utils.c")
s=once(s,"exit(1);",'ha_fail("resource or engine error; see native log");')
write("utils.c",s)
s=read("config.c")
s=function(s,"cfgFullPath",'    return CFG_FILENAME;')
write("config.c",s)
# Reset the resource index before each generation. Files use context-relative paths.
s=read("resource.c")
s=once(s,"void parseResourceFiles(char * filename)\n{","void parseResourceFiles(char * filename)\n{\n    numAdsResources=numBmpResources=numPalResources=numScrResources=numTtmResources=0;")
write("resource.c",s)
# Live environmental update on a frame boundary, without restarting ADS/TTM routine state.
s=read("ads.c")
s += '''
extern int ha_rebuilding_island;
void ha_refresh_island(void) {
    if (ttmBackgroundThread.isRunning && islandState.night != ha_night()) {
        int dx=grDx, dy=grDy;
        islandState.night=ha_night();
        ha_rebuilding_island=1;
        islandInit(&ttmBackgroundThread);
        ha_rebuilding_island=0;
        grDx=dx; grDy=dy;
    }
}
'''
# all stop paths unwind in worker, not exit the app; bounded checks even in script loops
s=once(s,"void adsPlay(char *adsName, uint16 adsTag)\n{","void adsPlay(char *adsName, uint16 adsTag)\n{\n    ha_check_stop();")
write("ads.c",s)
s=read("graphics.c")
s=once(s,"    // Blit the background","    extern void ha_refresh_island(void);\n    ha_check_stop();\n    ha_refresh_island();\n    // Blit the background")
write("graphics.c",s)
# Use original scene descriptor for fan. A queued request replaces only the next
# final routine; the currently executing routine always reaches its natural end.
s=read("story.c")
s=once(s, "        struct TStoryScene scene = storyScenes[i];",
"""        struct TStoryScene scene = storyScenes[i];
        // The living-room fan is HA-owned; retain its original explicit routine.
        if (!strcmp(scene.adsName, "MISCGAG.ADS") && scene.adsTagNo == 1)
            continue;
""")
s=s.replace("islandState.night = (hour == 0 || hour == 7);","islandState.night = ha_night();\n    (void)hour;")
s=once(s,"void storyPlay()","""static struct TStoryScene *ha_fan_scene(void) {
    for(int i=0;i<NUM_SCENES;i++)
        if(!strcmp(storyScenes[i].adsName,"MISCGAG.ADS") && storyScenes[i].adsTagNo==1)
            return &storyScenes[i];
    ha_fail("fan scene descriptor missing");
    return NULL;
}
void storyPlay()""")
s=once(s,"        struct TStoryScene *finalScene = storyPickScene(FINAL, unwantedFlags);",
"""        ha_check_stop();
        int requestedFan=ha_take_fan_request();
        struct TStoryScene *finalScene = requestedFan ? ha_fan_scene() : storyPickScene(FINAL, unwantedFlags);""")
s=once(s,"        if (!(finalScene->flags & FIRST)) {","        if (!requestedFan && !(finalScene->flags & FIRST)) {")
s=once(s,"                struct TStoryScene *scene = storyPickScene(wantedFlags,",
"""                if ((finalScene->flags & ISLAND) && ha_fan_pending()) {
                    requestedFan=ha_take_fan_request();
                    if(requestedFan) { finalScene=ha_fan_scene(); break; }
                }
                struct TStoryScene *scene = storyPickScene(wantedFlags,""")
s=once(s,"        adsPlay(finalScene->adsName, finalScene->adsTagNo);",
"""        if(requestedFan) ha_fan_started();
        adsPlay(finalScene->adsName, finalScene->adsTagNo);
        if(requestedFan) ha_fan_finished();""")
write("story.c",s)
# Instrument ownership on the upstream only. SDL's allocations remain SDL-owned.
for p in root.glob("*.c"):
    s=p.read_text()
    for old,new in [("malloc","ha_malloc"),("free","ha_free"),("fopen","ha_fopen"),("fclose","ha_fclose"),
                    ("SDL_CreateRGBSurface","ha_surface"),("SDL_CreateRGBSurfaceFrom","ha_surface_from"),("SDL_FreeSurface","ha_free_surface")]:
        s=re.sub(r"\b"+old+r"\s*\(",new+"(",s)
    s='#include "ha_port.h"\n'+s
    s=s.replace("#include <SDL2/SDL.h>","#include <SDL.h>")
    p.write_text(s)
for p in root.glob("*.h"):
    p.write_text(p.read_text().replace("#include <SDL2/SDL.h>","#include <SDL.h>"))

# Fan urgency: leave normal routines at a frame boundary through their normal
# cleanup path. Never unwind past live scene allocations or restart the intro.
s=read("ads.c")
finish_body="""    for (int i=0; i < MAX_TTM_SLOTS; i++)
        ttmResetSlot(&ttmSlots[i]);

    grRestoreZone(NULL, 0, 0, 0, 0);

    adsReleaseAds();"""
s=once(s,finish_body,"    adsFinishRoutine();")
helpers="""static void adsStopAllThreads(void) {
    for(int i=0;i<MAX_TTM_THREADS;i++)
        if(ttmThreads[i].isRunning) adsStopScene(i);
}
static int adsPollFanInterrupt(void) {
    if(!ha_should_preempt()) return 0;
    adsStopRequested=1;
    adsStopAllThreads();
    return 1;
}
static void adsFinishRoutine(void) {
    adsStopAllThreads();
"""+finish_body+"""
}

"""
s=once(s,"void adsPlay(char *adsName, uint16 adsTag)",helpers+"void adsPlay(char *adsName, uint16 adsTag)")
s=once(s,"    ha_check_stop();\n    uint32 offset;","    ha_check_stop();\n    if(ha_should_preempt()) return;\n    uint32 offset;")
s=once(s,"    // Main ADS loop\n    while (numThreads) {","    // Main ADS loop\n    while (numThreads) {\n        if(adsPollFanInterrupt()) break;")
s=once(s,"        // Determine min timer through all threads","        if(adsPollFanInterrupt()) break;\n\n        // Determine min timer through all threads")
s=once(s,"void adsPlayWalk(int fromSpot, int fromHdg, int toSpot, int toHdg)\n{",
"""void adsPlayWalk(int fromSpot, int fromHdg, int toSpot, int toHdg)
{
    if(ha_should_preempt()) return;
    int interrupted=0;""")
s=once(s,"    while (ttmThreads[0].delay) {","    while (ttmThreads[0].delay) {\n        if(ha_should_preempt()) { interrupted=1; break; }")
s=once(s,"        // Determine min timer from the two threads","        if(ha_should_preempt()) { interrupted=1; break; }\n\n        // Determine min timer from the two threads")
walk_start=s.index("void adsPlayWalk(")
s=s[:walk_start]+once(s[walk_start:],"    adsStopScene(0);",
"    adsStopScene(0);\n    if(interrupted) ttmResetSlot(&ttmSlots[0]);")
write("ads.c",s)
s=read("story.c")
s=once(s,"        if (prevSpot != -1)\n            adsPlayWalk(prevSpot, prevHdg, finalScene->spotStart, finalScene->hdgStart);",
"        if (!requestedFan && prevSpot != -1)\n            adsPlayWalk(prevSpot, prevHdg, finalScene->spotStart, finalScene->hdgStart);")
write("story.c",s)
s=read("graphics.c")
s=once(s,"void grFadeOut()\n{","void grFadeOut()\n{\n    if(ha_should_preempt()) return;")
write("graphics.c",s)

# OI uses the same source-level cleanup boundary, then a dedicated single actor.
# The standing lead-in comes from the original archive, not an extra PNG.
s=read("ads.c")
s += """
#include "control.h"
void adsPlayOi(void) {
    adsAddScene(0,0,0);
    grLoadBmp(ttmSlots,0,"SHKNFIST.BMP");
    unsigned start=ha_clock_ms(), last_wave=0;
    int completed=0, interrupted_by_fan=0;
    while(ha_oi_active()) {
        ha_check_stop();
        unsigned elapsed=ha_clock_ms()-start;
        int reason=ha_oi_exit_reason(elapsed,ha_fan_pending());
        if(reason) { completed=(reason==1); interrupted_by_fan=(reason==2); break; }
        ha_set_oi_frame(elapsed);
        grClearScreen(ttmThreads[0].ttmLayer);
        grDx=grDy=0;
        if(elapsed<250)
            grDrawSprite(ttmThreads[0].ttmLayer,ttmSlots,365,226,0,0);
        if(elapsed-last_wave>=160) {
            islandAnimate(&ttmBackgroundThread);
            last_wave=elapsed;
        }
        grUpdateDelay=0;
        grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
        unsigned remaining=3150-(ha_clock_ms()-start);
        if(remaining>3150) remaining=0;
        ha_wait(remaining<40?remaining:40);
    }
    adsStopScene(0);
    ttmResetSlot(ttmSlots);
    ha_end_oi(completed,interrupted_by_fan);
    // Publish without the old actor/overlay before releasing the island.
    grUpdateDelay=0;
    grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
}
"""
write("ads.c",s)
s=read("story.c")
s=once(s,"        ha_check_stop();\n        int requestedFan=ha_take_fan_request();",
"""        ha_check_stop();
        if(!ha_fan_pending() && ha_begin_oi_request()) {
            extern void adsPlayOi(void);
            islandState.xPos=islandState.yPos=0;
            islandState.lowTide=0;
            adsInitIsland();
            adsPlayOi();
            adsReleaseIsland();
            continue;
        }
        int requestedFan=ha_take_fan_request();""")
write("story.c",s)

# Sustained wind replaces the old one-shot command on the active story path.
s=read("ads.c")
s=once(s,"void ha_refresh_island(void) {","void ha_refresh_island(void) {\n    if(ha_wind_active()) return;")
s += """
#include "wind_state.h"
static void adsWindOcean(SDL_Surface *base) {
    islandState.night=ha_night();
    grLoadScreen(islandState.night ? "NIGHT.SCR" : "OCEAN00.SCR");
    ttmBackgroundThread.ttmLayer=grBackgroundSfc;
    grDx=grDy=0;
    if(islandState.raft>=1 && islandState.raft<=5)
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,512,266,islandState.raft-1,1);
    SDL_BlitSurface(grBackgroundSfc,NULL,base,NULL);
}
void adsPlayWind(void) {
    SDL_Surface *base=ha_surface(0,640,480,32,0x00ff0000,0x0000ff00,0x000000ff,0);
    grLoadBmp(&ttmBackgroundSlot,1,"MRAFT.BMP");
    adsWindOcean(base);
    while(ha_wind_step()) {
        ha_check_stop();
        if(islandState.night!=ha_night()) adsWindOcean(base);
        SDL_BlitSurface(base,NULL,grBackgroundSfc,NULL);
        grDx=grDy=0;
        double travel=ha_wind_travel(),seconds=ha_wind_seconds(),power=ha_wind_power();
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,ha_wind_wrap(55+travel*0.06,820)-150,54,15,0);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,ha_wind_wrap(580+travel*0.035,880)-180,40,16,0);
        ha_wind_gusts(grBackgroundSfc);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,288,279,0,0);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,396,279,14,0);
        int wave=(int)(seconds/0.20)%3;
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,270,306,3+wave,0);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,364,319,6+wave,0);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,518,303,9+wave,0);
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,442,148,13,0);
        int sway=ha_wind_round(power*(2+2*sin(seconds*8)));
        grDrawSprite(grBackgroundSfc,&ttmBackgroundSlot,365+sway,122,12,0);
        grUpdateDelay=0;
        grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
        if(ha_wind_settled())break;
        ha_wait(33);
    }
    ha_end_wind();
    ha_free_surface(base);
    // Drop all wind effects before OI or normal story can take ownership.
    islandState.night=ha_night();
    islandInit(&ttmBackgroundThread);
    grUpdateDelay=0;
    grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
}
"""
write("ads.c",s)
s=read("story.c")
s=once(s,"        if(!ha_fan_pending() && ha_begin_oi_request()) {","        if(ha_begin_oi_request()) {")
s=once(s,"        int requestedFan=ha_take_fan_request();",
"""        if(ha_begin_wind()) {
            extern void adsPlayWind(void);
            islandState.xPos=islandState.yPos=0;
            islandState.lowTide=0;
            adsInitIsland();
            adsPlayWind();
            adsReleaseIsland();
            continue;
        }
        int requestedFan=0; // old one-shot fan path is retired; wind is a level
""")
write("story.c",s)

# Music surprises reuse untouched original sprites; no extra artwork payload.
s=read("ads.c")
s += """
#include "music_pose.h"
static void adsClearMusicActor(void) {
    if(ttmThreads[0].isRunning) adsStopScene(0);
    ttmResetSlot(ttmSlots);
}
void adsPlayMusic(int kind) {
    adsAddScene(0,0,0);
    grLoadBmp(ttmSlots,0,kind==1 ? "JOHNWALK.BMP" : "MJTELE.BMP");
    unsigned start=ha_clock_ms(),last_wave=0;
    int completed=0;
    while(ha_music_active()) {
        ha_check_stop();
        unsigned elapsed=ha_clock_ms()-start;
        HaMusicPose pose=ha_music_pose(kind,elapsed);
        if(pose.sprite<0){completed=1;break;}
        grClearScreen(ttmThreads[0].ttmLayer);
        grDx=grDy=0;
        if(pose.sprite>=ttmSlots[0].numSprites[0])ha_fail("music sprite missing from original archive");
        SDL_Surface *sprite=ttmSlots[0].sprites[0][pose.sprite];
        // Bottom-center alignment keeps the original feet on the same sand pixel.
        grDrawSprite(ttmThreads[0].ttmLayer,ttmSlots,
                     380-sprite->w/2+pose.shift,299-sprite->h-pose.hop,pose.sprite,0);
        if(pose.question)grDrawSprite(ttmThreads[0].ttmLayer,ttmSlots,415,210,16,0);
        if(elapsed-last_wave>=160){islandAnimate(&ttmBackgroundThread);last_wave=elapsed;}
        grUpdateDelay=0;
        grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
        ha_wait(33);
    }
    adsClearMusicActor();
    ha_end_music(completed);
    grUpdateDelay=0;
    grUpdateDisplay(&ttmBackgroundThread,ttmThreads,&ttmHolidayThread);
}
"""
write("ads.c",s)
s=read("story.c")
s=once(s,"        int requestedFan=0; // old one-shot fan path is retired; wind is a level",
"""        int musicKind=ha_begin_music_request();
        if(musicKind) {
            extern void adsPlayMusic(int);
            islandState.xPos=islandState.yPos=0;
            islandState.lowTide=0;
            adsInitIsland();
            adsPlayMusic(musicKind);
            adsReleaseIsland();
            continue;
        }
        int requestedFan=0; // old one-shot fan path is retired; wind is a level""")
write("story.c",s)
