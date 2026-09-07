import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "shield-overlay" / "app" / "src" / "main" / "java" / "com" / "boop" / "shieldoverlay"


class ShieldDeezerAccessSetupSourceTest(unittest.TestCase):
    def test_first_home_offers_deezer_access_once_and_refreshes_on_return(self):
        home = (JAVA / "BoopHomeActivity.java").read_text(encoding="utf-8")
        prefs = (JAVA / "BoopPreferences.java").read_text(encoding="utf-8")

        self.assertIn("maybeOfferDeezerAccessSetup();", home)
        self.assertIn("Let BOOP see Deezer playback?", home)
        self.assertIn("puppetAccess.setEnabled(true);", home)
        self.assertIn("preferences.markDeezerAccessSetupOffered();", home)
        self.assertIn("if (homeShellVisible)", home)
        self.assertIn("DeezerPuppetAccess.get(this).refresh();", home)
        self.assertIn("deezerAccessSetupOffered()", prefs)

    def test_access_settings_prefers_boop_detail_page_with_safe_generic_fallback(self):
        access = (JAVA / "DeezerPuppetAccess.java").read_text(encoding="utf-8")
        plan = (JAVA / "DeezerAccessSettingsPlan.java").read_text(encoding="utf-8")

        self.assertIn("ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS", access)
        self.assertIn("EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME", access)
        self.assertIn("listenerComponent.flattenToString()", access)
        self.assertIn("ACTION_NOTIFICATION_LISTENER_SETTINGS", access)
        self.assertIn("Route.DETAIL, Route.GENERIC", plan)


if __name__ == "__main__":
    unittest.main()
