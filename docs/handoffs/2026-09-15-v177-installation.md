# v177 installed on Shield and Pixel 7 Pro

2026-09-15. Ryan requested both installations and gave go for the phone. Shield updated176to177; Pixel7Pro updated161to177. Both installed packages report `com.boop.alpha1`, version177 / `1.2.177-felt-eyelids`, and APK SHA256 `e8d082480e63fd02f6b391e6a362a1b71fb529061614c36ed821770347a68c8c`, matching signed run34928908920. Existing BOOP entry opened on both. Physical felt appearance and smoothness verdict pending.

Shield: all16 saved preference-file hashes identical before/after. Pixel7Pro:10of11 identical, including eye/appearance/voice. Only `boop_notifications.xml` changed; its current keys are `observed_channels` and `onboarding_seen_v1`. Existing source automatically records observed channels. Before-values were not captured, so the exact key delta is not proven; do not claim all phone preferences byte-identical. No manual settings, permission or data reset. Pixel10ProXL was not updated. Direct ADB worked; no Desktop Commander required.

[Signed build and source receipt](2026-09-15-v177-felt-eyelids.md). New grip and fake-notification diagnostic remain subsequent work.

---

