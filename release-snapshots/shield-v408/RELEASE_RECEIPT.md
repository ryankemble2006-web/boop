# BOOP Shield v408 & Wall v222 Release Receipt

- **Snapshot**: `shield-v408`
- **Shield Version Code**: `408`
- **Shield Version Name**: `1.2.408-shield`
- **Wall Version Code**: `222`
- **Wall Version Name**: `1.2.222-wall`
- **Summary**: Unified credential migration and appearance sync across Shield and Wall apps:
  - **Unified Credential Migration (`BoopVoiceTokenStore.java`)**:
    - Added automatic migration fallback in `BoopVoiceTokenStore.create(Context)`: when `SecureCredentialStore(context).load()` returns null (unmigrated), checks legacy `SecureTokenStore(context)` (`boop-ha.xml`). If valid connection credentials exist, automatically persists them into `SecureCredentialStore` (`boop_ha_credential.xml`) under `boop-legacy-token` and bridges to `HomeAssistantSavedConnection`, falling back safely to legacy store if migration fails.
  - **Real-Time Cross-Device Appearance Sync**:
    - Synchronized `HomeAssistantAuth.java` (JWT/Long-Lived Token bypass) and `BoopShared*Runtime.java` (Eye Hue, Voice Profile, Felt, Hand) across both Shield TV and Wall (Pixel) apps.
  - **Hardware Remapper & Shield Experience Menu Safeguard**:
    - Retains "No Action / Disabled (Swallow)" (`ACTION_NONE = "none"`) in `BoopKeyMappingStore`, `BoopKeyActionExecutor`, and `ShieldHomeOverrideService`.
    - Retains `customize_menu_option = 13` idempotent lock.
  - **Zero Gradle & Signed Pipeline**:
    - Assembled and aligned with local Android SDK build tools (`javac`, `d8`, `aapt`, `zipalign -f -p 4`).
