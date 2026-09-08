# BOOP notification implementation override

Date: 2026-09-08
Branch: `boop-unified-notifications`
Base: `boop-unified@dec9e2d24a6485a53ed5f8c4d2393b15eada1ff5`

Ryan explicitly overrode Hard Gate 0 in `docs/superpowers/plans/2026-09-08-boop-notification-presenter.md`.

Notification implementation may proceed immediately on top of the latest live canonical unified source without waiting for physical acceptance or reconciliation of the separate procedural-eye/sleep/hue experiment. The notification branch must remain isolated from `boop-unified-v63-fast-eyes`; do not merge, repoint, or overwrite that animation experiment while building notifications.

Animation, sleep, eye puppetry and notification-presentation polish are deliberately deferred. Ryan intends to do a consolidated animation pass later rather than block functional notification work now.

All other notification spec constraints remain in force: package `com.boop.alpha1`, permanent signer, default-deny app/channel selection, privacy-safe locked state, non-destructive swipe/timeout, original `PendingIntent` behavior, no root/device-admin/keyguard bypass, and manual real-device visual/acoustic acceptance.
