# BOOP Win7ify branch status

This isolated branch owns the Windows utility under `win7ify/`, not an Android APK.
Current executable: 0.2.0. Built source `ee744db`, GitHub run `34674142722` SUCCESS.
41 functional tests plus real official install/profile/menu lifecycle/uninstall,
including the normal hosted Explorer-hook path, passed. Yoga GUI/read-only checks
passed. The EXE is on its Desktop. Actual Yoga installation still needs normal
Windows permission approval and visual acceptance; no approval was bypassed.

Read SESSION_HANDOFF.md, win7ify/STATUS.md, win7ify/MEMORY.md and CODEX_HANDOFF.md.
The shared product context belongs to live main. Do not build an Android app from
this Windows branch or alter the protected Android lineages/signing.
