# BOOP Shield v400 Release Receipt

- **Snapshot**: `shield-v400`
- **Version Code**: `400`
- **Version Name**: `1.2.400-shield`
- **Summary**: Fixed `AdbWire` 2,048-character command length cap in `StartupLocalBridge.autoOptimiseShield` by chunking batch shell commands into `<=1,200` character packets (`execChunked`). `⚡ Auto Optimise Shield` now executes end-to-end without triggering `Invalid command` on step 3.
