# BOOP Shield v406 Release Receipt

- **Snapshot**: `shield-v406`
- **Version Code**: `406`
- **Version Name**: `1.2.406-shield`
- **Summary**: Real-time Shared Puppet Sync (Eye Hue, Voice Profile, Felt, Hand) via Home Assistant, Hardware Button Remapper "No Action / Disabled (Swallow)" Support, and Shield Experience Menu Safeguard Lock.
  - **Shared Puppet Sync via Home Assistant**:
    - `HomeAssistantAuth.java`: Added Long-Lived Access Token / JWT bypass in `freshAccessToken()` (`if (refreshToken.startsWith("eyJ") || refreshToken.contains(".") || refreshToken.length() > 60) return refreshToken;`), preventing sending bearer tokens to `/auth/token` with `grant_type=refresh_token` (HTTP 400 rejection).
    - `BoopSharedEyeColourRuntime.java`, `BoopSharedVoiceProfileRuntime.java`, `BoopSharedFeltColourRuntime.java`, `BoopSharedHandColourRuntime.java`: Fixed server scope checks (`if (!explicitSetup && !saved.isEmpty() && !scope.equals(saved))`) so initial setup with empty saved scope connects immediately; saved scope if `explicitSetup || saved.isEmpty()`; added logcat tags and resilient network retries.
  - **Hardware Button Remapper "No Action / Disabled (Swallow)"**:
    - `BoopKeyMappingStore.java`: Defined `ACTION_NONE = "none"`, surfaced `ACTION_NONE` in `availableActions()` right after `ACTION_DEFAULT`, and mapped label to `"No Action / Disabled (Swallow)"`.
    - `BoopKeyActionExecutor.java`: In `execute()`, safely no-ops on `ACTION_NONE` without error.
    - `ShieldHomeOverrideService.java`: Consumes and intercepts keys configured with `ACTION_NONE` (`return true`), swallowing them so Shield OS, foreground apps, and third-party services never receive them.
  - **Shield Experience Menu Settings Safeguard Lock**:
    - `ShieldHomeOverrideService.java`: In `onServiceConnected()`, added an idempotent safeguard inspecting `Settings.Global.getString(getContentResolver(), "customize_menu_option")`. If not `"13"`, automatically sets `customize_menu_option`, `customize_menu_double_press_option`, and `customize_menu_hold_option` to `"13"` ("Send 'Menu' command to app"), permanently preventing Shield Experience updates or remote re-pairing from resetting the burger button back to opening default Shield settings.
  - **Zero Gradle & Signed Pipeline**:
    - Compiled `classes2.dex` (with updated `com.boop.alpha1` shared sync classes), `classes3.dex` (with updated `BoopKeyMappingStore`, `BoopKeyActionExecutor`, and `ShieldHomeOverrideService`), and `classes4.dex` (with direct LAN TCP server pairing) using direct Android SDK build tools (`javac`, `d8`, `aapt2`).
    - Aligned 4-byte boundaries with `zipalign -f -p 4`.
