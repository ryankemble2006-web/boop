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
