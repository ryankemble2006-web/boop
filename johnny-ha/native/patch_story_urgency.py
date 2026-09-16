# SPDX-License-Identifier: GPL-3.0-or-later
"""Make urgent HA reactions abandon the remainder of Johnny's current mini-story."""
from pathlib import Path
import sys

root=Path(sys.argv[1])
path=root/"story.c"
s=path.read_text()

def once(text,old,new):
    assert text.count(old)==1,(old,text.count(old))
    return text.replace(old,new)

s=once(s,
"""        int prevSpot = -1;
        int prevHdg  = -1;

        if (!requestedFan && !(finalScene->flags & FIRST)) {""",
"""        int prevSpot = -1;
        int prevHdg  = -1;
        int urgentStoryPlan=0;

        if (!requestedFan && !(finalScene->flags & FIRST)) {""")

s=once(s,
"""            for (int i=0; i < 6 + (rand() % 14); i++) {

                if ((finalScene->flags & ISLAND) && ha_fan_pending()) {""",
"""            for (int i=0; i < 6 + (rand() % 14); i++) {

                if(ha_should_preempt()) { urgentStoryPlan=1; break; }
                if ((finalScene->flags & ISLAND) && ha_fan_pending()) {""")

s=once(s,
"""                if (prevSpot != -1)
                    adsPlayWalk(prevSpot, prevHdg,
                        scene->spotStart, scene->hdgStart);

                ttmDx = islandState.xPos""",
"""                if (prevSpot != -1) {
                    adsPlayWalk(prevSpot, prevHdg,
                        scene->spotStart, scene->hdgStart);
                    if(ha_should_preempt()) { urgentStoryPlan=1; break; }
                }

                ttmDx = islandState.xPos""")

s=once(s,
"""                adsPlay(scene->adsName, scene->adsTagNo);

                unwantedFlags |= FIRST;""",
"""                adsPlay(scene->adsName, scene->adsTagNo);
                if(ha_should_preempt()) { urgentStoryPlan=1; break; }

                unwantedFlags |= FIRST;""")

s=once(s,
"""        }

        if (!requestedFan && prevSpot != -1)""",
"""        }

        if(urgentStoryPlan) {
            if(finalScene->flags & ISLAND) adsReleaseIsland();
            continue;
        }

        if (!requestedFan && prevSpot != -1)""")

path.write_text(s)
