# Rally project memory

The user explicitly requested the two existing desktop Network Q rally games combined into one Shield launcher, installed and tested, on a separate BOOP GitHub branch. This is an adjacent game project, not Unified/HOME integration.

Use branch `boop-rally-shield`, project `rally-shield/`, package `com.boop.rally`. Preserve original desktop folders. The accepted Championship payload is Browser-Test, not RALLY or Installed. Native ARM64 DOSBox Pure is chosen instead of browser emulation or PC streaming.

Game content stays private. Public Actions APK is engine/launcher only; local preparation/provisioning supplies user-owned files separately. Ordinary game saves persist separately; do not describe this as save-state emulation. Existing permanent BOOP signing stays in Actions.

Eleven host-side behavioral tests passed before the first Android build. Device race, sound, input and save results are not inferred from that. Update handoff/status with exact subsequent evidence.
