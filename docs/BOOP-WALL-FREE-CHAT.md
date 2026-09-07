# BOOP Wall: optional Free Chat

Approved by Ryan in the 2026-09-07 conversation. This document describes the
candidate, not a claim of physical acceptance.

## Use

Press and hold the BOOP face. The existing playful animation happens first.
Keep holding until three seconds to open Chat mode. Select Free Chat or OpenCode;
the check mark identifies the saved choice. Cancel/Back changes nothing.

In either mode, normal local lights and media requests stay in Home Assistant.
Only an unhandled general question uses the selected chat route. OpenCode remains
the first-install default. Free Chat opens the ChatGPT website in a dark browser
Custom Tab, or a normal browser if Custom Tabs are unavailable. The question is
copied to the clipboard, marked sensitive to suppress supported clipboard previews,
for the user to paste and send. Browser Back returns to BOOP. A browser login may
be required; ChatGPT account limits still apply. BOOP does not extract browser
cookies or read the answer back into the eyes/voice interface.

## Scope and implementation

Separate preferences (`boop_chat_mode`) protect existing HA/voice settings.
The pure Java hold detector and routing gate have behavioral tests. The router
checks current mode after local processing, so switching away while local work
runs does not initiate an OpenCode call. Results from an old mode revision or a
backgrounded Activity are not spoken or used to open a browser.

The original MainActivity baseline is preserved. The existing materialization
pipeline applies a narrowly checked chat patch after its wake and toast patches.
Builds must run that pipeline. Patch anchor drift fails the build rather than
silently editing a different runtime. AndroidX Browser adds no dangerous device
permission; browser package-visibility queries are declared explicitly.

This is not an invisible ChatGPT backend or a workaround for service limits.
An already-running OpenCode HTTP request is not forcibly aborted; its old-mode
result is ignored. The current house connection/setup prerequisites remain.
No timed routines, new HA connections, new API keys, physical installations,
credential changes or alterations to the other BOOP apps are included.

## Verification gates

Local: JVM behavior suite, syntax, workflow parsing.
GitHub: preserved-source diff, existing bridge/source/JVM checks, materialization,
Android unit tests/build, package/version/signature checks, real wake-microphone
activation, UI three-second hold, selection/persistence/revert/cancellation,
and existing Shield pairing return.

Manual physical acceptance remains necessary: tap/wake and short gag, three-second
menu in portrait/landscape, Free Chat browser login/paste/send/Back, local media
with OpenCode unavailable, switching back, and companion left swipe. Do not
replace the preserved physical checkpoint until those checks pass.
