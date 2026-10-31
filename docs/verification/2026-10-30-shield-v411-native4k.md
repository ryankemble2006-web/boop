# Shield v411 native 4K verification

GitHub source commit: cd428be388c68f8fe4c7e997506de807c45d1387
Build/sign run: https://github.com/ryankemble2006-web/boop/actions/runs/41120632445
Release: https://github.com/ryankemble2006-web/boop/releases/tag/boop-shield-v411

Signed APK SHA-256: 73224f11feb95d51bbee32896576e2a227be10f8b1648c2f928fd4688234afb4
Certificate SHA-256: f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde

Installed on Shield 192.168.1.169:5555 using adb install -r. Package com.boop.shieldoverlay, versionCode 411, versionName 1.2.411-shield. App data retained. Signature matches device v407 and repository expected certificate. Checksum and alignment verified; only AndroidManifest.xml and classes3.dex differ from the verified working v407 APK, excluding signatures.

Live resize: same process PID 20002, 1080p override then native reset. Eight Home static text/card bounds retained within one pixel. Firmware retained a 3839 width override after live reset; reboot cleared it. Final wm size reports physical 3840x2160 with no override. SurfaceFlinger launcher geomBufferSize is 3840x2160 with identity transform. Home screenshot checked. No launcher fatal observed after final reboot. Motion smoothness and subjective audio quality still need user acceptance.

Native NVIDIA Developer options > HDMI > Show all supported modes in custom display modes enabled through official reboot prompt. Native mode persisted through repeated reboots. Global density reverted to firmware default 320 on reboot; launcher uses its existing 512 density policy for 2160p, keeping apparent sizes. Original upscaled 1080p layout vs native 4K labels differed by only 1-5 output pixels from rounding/font metrics.

Build investigations: v409 stopped before signing because smali serialization dropped an explicit false static field default. v410 added an exact normalization for that equivalent field encoding and built/signed, but device startup found v408 baseline IllegalAccessError in BoopRoomPreferences accessing relocated alphax.BoopRoom. Restored working v407 without clearing data. v411 uses v407 source/APK baseline plus the configuration refresh. Non-signature APK ZIP entries of repository v407 match the pulled working device v407. v409/v410 should not be installed.

Original checkout shield-v325-live at f82a0e18 was left unchanged and clean; unrelated local commit was not published. Release commits were isolated from origin in a managed worktree.
