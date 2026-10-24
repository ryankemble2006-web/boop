# BOOP Shield v399 Release Receipt

- **Snapshot**: `shield-v399`
- **Version Code**: `399`
- **Version Name**: `1.2.399-shield`
- **Summary**: Added 1-click `[⚡ Auto Optimise Shield]` button to the Startup & Uninstall Manager (in both the top strip and bottom dock, dynamically styled with `FocusChrome.accentColor(context)`). Automatically rips out all 48 Shield Experience 9.x bloatware, telemetry, partner stub, and dead app packages (including `android.autoinstalls.config.nvidia` and `deezer.android.tv`), disables the 6 stock launcher/ad/OTA/SetupWraith packages (`pm disable-user`), silences background autostart on the 7 background hogs (`RUN_IN_BACKGROUND ignore` + `RUN_ANY_IN_BACKGROUND ignore`), and locks in all BOOP Media, Home Button, and Default Launcher permissions.
