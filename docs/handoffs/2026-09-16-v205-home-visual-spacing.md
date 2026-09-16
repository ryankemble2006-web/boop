# v205 Home visual spacing - 2026-09-16

Owner branch: `boop-hand-colour-v191`.

Ryan asked for the visible gap from Kodi artwork to Now Playing to match the visible gap from the Apps row to Now Playing, and for every wide favourite tile gap to match. The earlier v204 screenshot proved the explicit spacer alone was insufficient because card-internal centering/padding created extra visible vertical space and the 240dp card width made the horizontal rhythm uneven.

v205 top-aligns the favourites row and Home card content, removes Home-card top/bottom inset, uses 230dp cards with a shared 16dp right margin, and disables Home-banner focus/grab scaling so visible artwork edges stay on that grid. The v204 parked Now Playing slot, four-button top row, removed `Favourite apps` heading and bottom-right idle BOOP ownership design are retained.

TDD evidence: focused v205 gate `35095495224` failed before implementation and `35096388830` passed after it. Full signed workflow `35096667354` succeeded from source `b529eceaa70b3d6140eeb41d5d35cd5e41ecd8ef`; artifact `10446686887`, `BOOP-Unified-v205-Home-Visual-Spacing`.

The installed Shield package reads `205` / `1.2.205-home-visual-spacing`. `adb install -r` returned Success and pulling `/data/app/.../base.apk` back produced SHA-256 `633f0d70cff6db5823909f13f1231818fa1a7ae041088975b4cfe776bfe3b088`, matching the downloaded signed artifact exactly. Launch resumed `com.boop.shieldhome.ShieldLauncherActivity`.

Fresh live Shield screenshot with active playback visually confirms equal vertical spacing on both sides of Now Playing and uniform wide-tile gaps across Kodi, YouTube, Deezer, Casualty and EastEnders. Screenshot/raw diagnostics remain private and are not committed.

Acceptance boundary: active-playback spacing is visually checked. No-media favourites parking and large bottom-right BOOP were not re-triggered in this check to avoid stopping playback; their v204/v205 source contracts remain green but a fresh physical no-media screenshot is still a separate acceptance item.
