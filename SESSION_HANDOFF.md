# Animation Lab v13: LAN notification tester

Updated 2026-09-13. Owner: `animation-lab-system-scale-poc`.

Source `57c1bdf932fa739b27bbd353e27e6f9d090ce262`, signed Actions `34733070034` succeeded.
APK version `13 / 0.13-lan-notification-tester`, package `com.boop.animationlab`.
Downloaded APK SHA256: `cee36a826ae3d2b00ce73b8c7ba3cd7ed5823e7d4e074ed8b9b003a6663e7a63`.
The exact downloaded APK was passed to `adb install -r` on Shield and Pixel 10 Pro XL; both returned Success.

Physical LAN test: Shield Discover device found the Pixel automatically, without entering its IP.
Shield Basic, Message, Private and Actionable buttons each caused a real Pixel NotificationManager post.
Pixel BOOPLabLAN logs and Android NotificationRecord entries independently confirmed all four test types.
The actionable record includes an action; private includes a publicVersion. Tap-through while locked was not tested.
The Pixel's test receiver was already running during this test and was left running.
The receiver is explicitly opt-in. This v13 development transport is trusted-LAN-only in intended use,
not a paired/encrypted production household-sync implementation; sender Sent is not a delivery acknowledgement.

All 26 animation definitions, master artwork, shaders and choreography remain unchanged from v12.
No Unified app code was changed during the Lab work.
Actual Unified animation in response to these posts is NOT yet verified.
Read-only diagnosis found selected phone apps but no enabled channels, plus overlay access unavailable.
Those are separate Unified setup/runtime issues, not failure of the Lab sender.

Local unshipped packet-validation edits in LabPacket.java/LabPacketTest.java are not in this APK.
A proposed receiver hardening edit was tool-blocked and was not applied.
An emulator booted but System UI stalled; no passing emulator runtime result is claimed.
Next: handle the explicitly requested Unified duplicate surfaces on its isolated v148 worktree.
