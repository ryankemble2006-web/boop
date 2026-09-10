# Tablet recipes implementation plan

Ryan explicitly approved implementation now; the local September 17 idea is no longer parked.

Use a tablet-only adapter in the existing Wall activity, keeping its selected conversation client and microphone lifecycle. A pure Java session owns ingredient collection, bounded recipe parsing and navigation. The Android panel renders that session with large text. No new provider, permission, microphone, subscription or device-profile routing.

1. Add failing JVM tests for ingredient additions/corrections, explicit readiness, strict generated recipe parsing, offline navigation, recipe/media disambiguation and cancellation of late results.
2. Implement the session and a large black/blue cooking panel. One short step at a time; ingredients can be read aloud. Preserve the previous screen-awake flag on exit. Back closes cooking. Backgrounding invalidates pending generation without opening a microphone.
3. Add a final materialization adapter before media handling. Ingredient lists stay local until explicit readiness; direct recipe requests use the selected existing conversation client. Provider errors remain plain English. Generated recipes use a bounded text protocol; malformed output is rejected, never displayed as executable markup.
4. Run focused functional tests, materialize/build in task-private scratch without overwriting the pre-existing boop-build directory, review, publish and inspect GitHub functional build/signing. Manually inspect tablet UI locally; actual Pad readability, voice and provider acceptance remain Ryan's tests.

Other work remains separate: Home spacing/icons unfinished; EastEnders artwork owned elsewhere; animations and natural-voice tuning deferred. Startup Manager remains investigation/design only, with processor/headroom experiments abandoned and NVIDIA Max unchanged. Preserve Undo/selections/manual launches and essentials; no performance claim or Dolphin testing.
