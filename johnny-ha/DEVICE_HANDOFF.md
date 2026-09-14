# Johnny HA lab v8 device handoff

Updated 2026-09-14. Source `d84abd9552a7d84f8b5993d0db66d436da9ade12`, successful native/Java run `34876738106`, artifact `10361044628`. Core library SHA256 `3667a11705fb82073a9ba0670471995c6fb9615e2e9a63d0cfa22b732d999523`.

Only `local.johnnycastaway.halab` was updated. Final private packaged APK SHA256 `2be097901d7e5d5b591c9951e94cbd7bd926392ac521591de3f6772418284272` matches the installed Shield base APK. Existing Johnny signer preserved. Original resource files, artwork and signing material remained local. Windows packaging must normalize ZIP entry separators, store resources.arsc uncompressed and zipalign before signing; earlier packaging candidates are not accepted artifacts.

Native player renders original scenes with the existing Now Playing display. Actual frames advance, Deezer retains the same DIRECT 44100 output and the player opens no audio subsystem. HA bridge through BOOP v175 returns real living-room light changes. User physically confirmed night/day after a longer off period. An earlier short off test was not visibly confirmed; no renderer fix or v9 was introduced afterward.

Fan routine is MISCGAG.ADS:1, reserved for HA triggers and excluded from automatic story selection. One observed off-to-on queues one original routine; initial/reconnect on does not replay. Native status distinguishes queued, started and completed. Fan physical acceptance is pending BOOP v176 power-only mapping: v175 conservatively returned unknown because both power and oscillation switches matched.

Tests: JVM state edge/reconnect checks; C queue/stop/timing; actual patched story selection across all eleven story days; 100 sanitizer cancellation generations; framebuffer; Android arm64 link; no automatic SDL JNI loader; Java/dex compile. These do not replace user visual acceptance.

Original screensaver remains selected and other Johnny variants remain installed pending fan acceptance. User authorized removing those installed Shield variants afterward. Preserve their data using uninstall -k and retain APK backups, local scene-board names, original resources and source. Do not delete desktop projects.

Native code ownership is back with parent. Next BOOP branch is `boop-lyrics-album-v176`, adding lyrics album focus/click plus excluding oscillation from fan power.
