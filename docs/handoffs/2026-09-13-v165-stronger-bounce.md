# v165 stronger bounce: candidate preparation

Ryan tested installed v164 and reported "ha it kinda works :P make him bounce more obviously". This is partial positive user feedback on v164 movement, not blanket acceptance of v164 lyrics or every audio source.

Task remains boop-unified-v164-music-bounce, based on live95e2d153ac6e6b319f50284d4fbdb53bcb83c294. Minimal v165 candidate: multiply visible vertical lift by2 in MusicBounceRenderer; version165 /1.2.165-music-bounce-stronger. Audio source, smoothing timings, permissions, blink engine, artwork, viewport dimensions and all native lyrics inputs remain unchanged from installed v164. Accepted v162/main/other task branches must not be merged or rewritten.

Test-first red71942f8282c618e260864e9ff46a110906e63304, run34779468840/job103783705756: 3 failures expected for missing165 version, missing2x gain and the production-renderer test reporting expected2px/got1px. Three existing groups passed, including332 envelope assertions. Failure logs were read before implementing the one-line multiplier. Build and signing were skipped.

This preparation checkpoint is not a completed build or physical acceptance. Deliver the signed candidate for Ryan after source/package/signature checks. No installation, permission grant, device input or local app-source editing/build is part of this tuning task unless subsequently requested. Historical v164 build/install receipts remain intact.
