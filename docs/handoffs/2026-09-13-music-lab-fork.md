# Music Lab side-by-side fork: work in progress

Ryan requested a fully separate installable fork, no merge while other operations continue. Parent is boop-unified-music-audio-prompt at 174d492f36db80bc3da4036d9434ce1ec3c1582a, based on accepted Unified v161. Work belongs ONLY to boop-music-lab-side-by-side-v161. No parent branch or device may be changed for this task.

Target: BOOP Music Lab, package com.boop.musiclab, independent version 1 / 0.1.1-v161-audio-prompt. This is a full source fork with the existing conditional audio-permission entry. VU sampling/bounce remains unimplemented. Preserve artwork and timing. Do not merge lyrics or other ongoing work.

New GitHub-only non-visual tests specify separate application identity, preservation of the parent materialization and assets, private permission entry, and no competing Home/assistant/boot registration. At this initial test-first checkpoint the materializer is not implemented and the fork tests are expected to fail. No signed artifact or physical acceptance is claimed. Build and existing permanent signing will run on GitHub after the isolation checks pass. No local builds, device input, install, permission grant or data copying is implied by generating the APK.
