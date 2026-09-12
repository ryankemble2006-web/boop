# Casualty 1.0 - personal Android TV shortcut

Opens the official BBC iPlayer directly to Casualty on Android TV / Nvidia Shield.

With the optional accessibility helper enabled, the shortcut can:

- choose the currently focused existing iPlayer profile;
- open the newest Casualty episode;
- click an exact visible `Skip trailer` control once if BBC shows a promo;
- restore focus to the first episode card when playback later returns to the Casualty programme page.

It does not intercept remote keys, choose unknown profiles, record screen contents, or modify iPlayer.

## Install and use

1. Sideload `Casualty-1.0.apk`.
2. Open the Casualty tile once.
3. In Shield Settings > Accessibility, enable **Casualty auto-play**.
4. Open the tile again. The helper only acts during an armed Casualty launch.

The initial launch helper expires after two minutes. After it starts an episode, the return-focus guard remains bounded for up to two hours. The optional trailer click is restricted to an exact `Skip trailer` label during the first 60 seconds after the episode launch and is claimed at most once.

## BBC routes

Programme PID: `b006m8wd`.

TV deep link:
`https://www.live.bbctvapps.co.uk/tap/telly/iplayer?deeplink=tv%2Fprogrammes%2Fb006m8wd`

Browser fallback:
`https://www.bbc.co.uk/iplayer/episodes/b006m8wd/casualty`

## Artwork

Launcher artwork source used for this private shortcut:
`https://image.tmdb.org/t/p/w1280/2dE4H9M1gVs7pZ8xZlHpDjnFVfE.jpg`

The source image is retained under `artwork/`; the Android TV banner is a 16:9 resize and the square icon is a centre crop of the same image. Rights remain with their respective owners. Inclusion here does not imply BBC or TMDb endorsement or a redistribution licence.

## Physical Shield verification - 12 September 2026

On the test Shield, the installed BBC iPlayer accepted the Casualty deep link. With Casualty auto-play enabled, the helper selected the existing profile, detected the exact Casualty title, clicked the newest episode, clicked the real BBC `Skip trailer` button when it appeared, and entered playback.

A Back-from-playback return test then logged `Return episode focus accepted: true`; a fresh UI hierarchy showed the Casualty episode card genuinely focused. Natural end-of-episode return remains a separate long-duration acceptance case.

Package: `uk.local.casualty`, versionCode 1 / versionName 1.0. The app uses its own private signing identity and can be installed beside `uk.local.eastenders`.

The project has no ads, analytics, microphone permission, storage permission, account credentials, or episode-list scraping. Build output and private signing material are ignored and must not be committed.
