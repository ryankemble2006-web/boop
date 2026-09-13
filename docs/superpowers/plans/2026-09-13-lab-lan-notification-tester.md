# Animation Lab LAN Notification Tester Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Let Shield Animation Lab discover a Pixel Animation Lab on the LAN and remotely trigger genuine Android test notifications.

**Architecture:** Pure-Java packet codec plus Android UDP receiver service and sender UI. Pixel receiver runs only when explicitly enabled; Shield discovers peers by broadcast and sends synthetic notification requests.

**Tech Stack:** Java 8, Android SDK 36, UDP DatagramSocket, NotificationManager.

**Spec:** `docs/superpowers/specs/2026-09-13-lab-lan-notification-tester-design.md`

## Global Constraints
- Do not alter existing animation definitions, artwork, shaders, or v12 motion timing.
- Keep feature inside `com.boop.animationlab` only.
- Generated notifications must enter Android through NotificationManager, never direct BOOP animation calls.
- Receiver must be user-enabled and stoppable.
- Use only local LAN; no cloud or HA dependency.

### Task 1: Packet protocol
- [ ] Write failing `LabPacketTest` for discovery, presence and notification round-trips.
- [ ] Add `LabPacket` pure-Java codec with versioned Base64-safe fields.
- [ ] Run the test green.

### Task 2: Pixel receiver and real notifications
- [ ] Add manifest permissions/service declaration and exact build allowlist.
- [ ] Add `LabNotificationReceiverService` foreground UDP receiver.
- [ ] Map Basic, Message, Private and Actionable requests to genuine Android notifications.
- [ ] Compile with Android SDK 36.

### Task 3: Shield sender UI
- [ ] Add receiver On/Off, Discover, target label and four send buttons to Lab UI.
- [ ] Add background LAN discovery/send helper and runtime notification permission request.
- [ ] Preserve all existing animation controls and v12 behavior.

### Task 4: Build and physical proof
- [ ] Bump Animation Lab to v13 and CI-build with permanent signer.
- [ ] Install exact artifact on Shield and Pixel 10 Pro XL.
- [ ] Enable receiver on Pixel, discover it from Shield, send each test type.
- [ ] Verify Pixel posts real notifications and record Unified reaction separately from Lab transport proof.
- [ ] Update handoff, commit/push, and verify live branch head.
