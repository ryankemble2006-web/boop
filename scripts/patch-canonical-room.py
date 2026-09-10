from pathlib import Path

path = Path(__file__).resolve().parents[1] / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
text = path.read_text(encoding="utf-8")

old = """        haAuth = new HomeAssistantAuth(this, tokenStore);
        haClient = new HomeAssistantClient(tokenStore, haAuth, HOME_AREA);"""
new = """        haAuth = new HomeAssistantAuth(this, tokenStore);
        BoopRoomSource roomSource = new BoopRoomPreferences(this);
        haClient = new HomeAssistantClient(tokenStore, haAuth, roomSource);"""
if old not in text:
    raise SystemExit("HomeAssistantClient room adapter anchor not found")
text = text.replace(old, new, 1)

old = "        deviceSetup = new HomeAssistantDeviceSetup(tokenStore, haAuth);"
new = "        deviceSetup = new HomeAssistantDeviceSetup(tokenStore, haAuth, roomSource);"
if old not in text:
    raise SystemExit("HomeAssistantDeviceSetup room adapter anchor not found")
text = text.replace(old, new, 1)

old = """HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()
                    ? HomeAssistantDeviceSetup.SetupResult.READY
                    : deviceSetup.ensureReady();"""
new = "HomeAssistantDeviceSetup.SetupResult setup = deviceSetup.ensureReady();"
if old not in text:
    raise SystemExit("Room-aware setup readiness anchor not found")
text = text.replace(old, new, 1)
path.write_text(text, encoding="utf-8")
print("Canonical Wall Home Assistant clients now read the configured room per command")
