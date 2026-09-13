# Animation Lab LAN Notification Tester Design

## Goal
Add a dev-only cross-device tester to BOOP Animation Lab so a Shield can discover a Pixel running the same Lab APK and ask that Pixel to post a genuine Android notification.

## Architecture
The Lab uses a tiny UDP broadcast protocol on the local network. A user-enabled foreground receiver service on the Pixel listens for discovery and notification-test packets, responds with its device label, and posts real notifications through Android NotificationManager.

The Shield Lab adds discovery and four send buttons: Basic, Message, Private, and Actionable. These packets never call BOOP animation code directly. Unified, if installed with notification-listener access, must encounter the resulting notification through the real Android notification path.

## Boundaries
- Package remains com.boop.animationlab and stays dev-only.
- Existing 26 eye animations, timing keys, shaders, artwork, and v12 independent-motion behavior remain unchanged.
- Receiver is opt-in with an explicit On/Off control.
- No Home Assistant, cloud service, account, IP entry, or saved device pairing is required.
- Local-network protocol carries only synthetic test labels/content.
- The receiver foreground-service notification is separate from generated test notifications.
