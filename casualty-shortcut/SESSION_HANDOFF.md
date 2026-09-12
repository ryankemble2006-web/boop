# Casualty shortcut 1.0 - 12 September 2026

Standalone Android TV package: `uk.local.casualty`, versionCode 1 / versionName 1.0.

Purpose: open the official BBC iPlayer Casualty programme route and automate the same bounded path proven by the EastEnders sibling: existing profile, newest episode, optional exact `Skip trailer`, then one-shot return-focus recovery.

BBC programme PID: `b006m8wd`.

Physical Shield verification:
- official iPlayer accepted the Casualty deep link;
- existing iPlayer profile click accepted;
- exact `Casualty` programme title detected;
- newest episode click accepted;
- real BBC `Skip trailer` control was detected and clicked successfully during the first physical run;
- Back-from-playback return logged `Return episode focus accepted: true` and a fresh UI hierarchy showed the episode card focused;
- the final rebuilt APK repeated profile/newest-episode launch and return-focus recovery; iPlayer did not offer the promo again on that immediate repeat.

Final signed APK SHA256: `4b59f2666a7fab0d94a4c70ba14dc528197a1e6903442d5bb85d47818befdaa3`.
Signer certificate SHA256: `d198e64f5cce0ccc77201bebd41db0283832be7b05476e26c2370b72d018a7ef`.

Artwork source: `https://image.tmdb.org/t/p/w1280/2dE4H9M1gVs7pZ8xZlHpDjnFVfE.jpg`.
Original SHA256: `4c1dfc100f2ea5eb27513f2617bda13c1e6f3633d14ab942b79951bb8ae85000`.
Banner SHA256: `f8756d11d29b3ad4d8fc90011dc0649710dfc3b13e11810e74719050e5faf6f0`.
Icon SHA256: `fdc0756b2a6f25004cec4d423ec06fc3c85277af91cf523515cc014324920bff`.

Private signing key/password and APK build outputs are ignored and must never be published. Natural end-of-episode return remains a long-duration acceptance case.