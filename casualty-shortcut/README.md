# Casualty 1.2 — Shield iPlayer shortcut

One press launches the existing official iPlayer programme route, selects the existing focused profile and newest episode, and clicks an exact `Skip trailer` control once if offered within 60 seconds. Both programme shortcuts use the same helper logic. Existing banner and square icon are unchanged.

The obsolete Watch/browser/settings/Close menu is removed. The shortcut finishes after launching iPlayer. When playback returns to the programme page, the armed helper returns to the device's configured Home once. Pause remains in the player. Switching to another app cancels the helper; the return guard expires after two hours. Ordinary iPlayer browsing without a shortcut launch is not armed.

The existing `Casualty auto-play` accessibility helper must remain enabled in Shield Accessibility settings. No new permission grant or setup change is required for an update. If disabled, a short message explains where to enable it. If official iPlayer cannot launch, the shortcut shows a message and closes; it does not show a browser selector.

Current branch: `iplayer-shortcuts-home-20260916`. See [SESSION_HANDOFF.md](SESSION_HANDOFF.md) and the [joint receipt](../docs/handoffs/2026-09-16-iplayer-shortcuts-home.md) for CI, signing, installation, rollback and pending physical acceptance.

GitHub workflow `build-iplayer-shortcuts.yml` performs logic checks and builds unsigned aligned artifacts. Signing uses the existing original private keys locally; do not substitute keys or commit signing material. Tests do not claim physical playback acceptance.
