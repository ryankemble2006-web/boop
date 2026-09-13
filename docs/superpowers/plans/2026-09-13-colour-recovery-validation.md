# Unified feature bumps recovery: validation coordination

2026-09-13. A continuation window was opened after the chat failed. GitHub reads and writes work; the repository has not disappeared.

The continuation added settings regression tests at `9e452b437fe3e05bf0d6847b4885b9afc23e54c6` and observed red run `34756931958`: four passing checks, three expected failures for missing startup/settings wiring. Another writer incorporated those tests and completed the implementation at `dcebdedd251585e2dae0b414cb0c92fccf52f148`. Its appearance workflow `34757187401` succeeded. The continuation's competing implementation object `c5016ef7580cc142824511f6730b0ea4a164d506` was deliberately NOT attached to any branch. It is superseded.

The continuation dispatched build `34757274196`; it passed early preservation/behaviour gates, then was superseded by scoped workflow update `0a4134ebfe8049254378b4706d2ee1df73cd7e87` and full build `34757337845`. That newer run succeeded, including permanent signing, APK/archive verification and all configured non-visual tests. Artifact `10317198102` / BOOP-Unified.

APK checksum `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353` and permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` were independently verified on the laptop with Get-FileHash/apksigner. Installed on emulator-5570 (Pixel_10_Pro_XL_API_36) and emulator-5572 (BOOP_Android_TV_API_36), using `install -r`, no permission grants. Package version 159 read back on both. Original private rollback APKs (v151/v156) and receipts remain in `%TEMP%/boop-colour-v159-checks`.

Both emulators initially had no persisted boop_eyes or boop_appearance XML. The continuation seeded deliberate migration hue fixtures 73 (phone) and 288 (TV) before upgrading; both survived. MainActivity cold launch with `boop_open_voice_settings=true` returned Status: ok on both. Phone voice-settings screenshot was inspected. No live Home Assistant helper writes or physical-device mutations by this continuation.

After an attempted phone voice-settings scroll, the returned hierarchy showed BoopProfileActivity. Attribution is unresolved (concurrent input versus app routing); do not count this as verified navigation. Further inputs paused rather than risk interacting with another session. A combined final read-only package/preferences/foreground/log receipt request was blocked by the safety layer and did not execute; no alternative route around that block was attempted.

Next owner must finish controlled appearance UI/Cancel/persistence and HA-sharing/reconnect tests before speed. Coordinate one driver per emulator and recheck installed version/current foreground. No physical Pixel 10 deployment. Static contracts are not real HA synchronization or visual acceptance. The approved artwork and authored motion must remain unchanged.
