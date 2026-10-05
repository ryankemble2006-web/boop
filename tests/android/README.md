# Home optional-row regression

Install the BOOP Shield APK to an Android TV emulator, then run from PowerShell:

```powershell
./tests/android/run-home-optional-rows.ps1 -Serial emulator-5554 -EvidenceLabel current
```

The runner requires JDK 17 on PATH, Android SDK platform 36 and build-tools 36.0.0.
`-Sdk`, `-OutputDirectory` and `-Package` override the local defaults. It refuses
non-emulator serials and also checks `ro.kernel.qemu` before installing anything.

The separately signed fixture loads the installed APK's real Home classes and
resources, supplies two offline content rows, and uses its own preferences. It
has no Internet permission and does not launch the BOOP application or connect
to Home Assistant. It does not alter the installed BOOP APK or its saved data.

Android instrumentation sends actual D-pad events. Assertions cover reaching
both rows, complete visibility including the enlarged focus borders, selecting the second card,
and returning to favourites. Screenshots additionally check that scrolling never
paints over the fixed navigation (allowing small antialiasing-rounding differences).
The runner fails on any regression and saves
the result plus initial, row1, row2, returned, tall-room and tall-no-rows screenshots outside the repo by
default, under the sibling `home-row-audit` directory.

A second fixture uses an activity-local 160dpi resource context, one optional row,
and a synthetic live room containing one lamp. It verifies that spare vertical
space still exposes the complete room panel and device, and that D-pad traversal
visits favourites, the optional row, then the room device and returns through the
same controls. Device selection must reach the fixture callback. A third case
removes optional rows and checks the existing favourites-to-room shortcut in both
directions. This changes no emulator display settings or real Home Assistant state.

These fixtures exercise emulator rendering and navigation. They do not establish
physical Shield or user visual acceptance.
