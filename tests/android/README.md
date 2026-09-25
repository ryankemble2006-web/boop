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
both rows, complete visibility of the focused cards, selecting the second card,
and returning to favourites. Screenshots additionally check that scrolling never
paints over the fixed navigation. The runner fails on any regression and saves
the result plus initial, row1, row2 and returned screenshots outside the repo by
default, under the sibling `home-row-audit` directory.

These fixtures exercise emulator rendering and navigation. They do not establish
physical Shield or user visual acceptance.
