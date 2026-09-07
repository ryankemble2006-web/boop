from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "shield-overlay/app/src/main/java/com/boop/shieldoverlay"


class ShieldHomeDashboardSourceTest(unittest.TestCase):
    def test_activity_wires_live_dashboard_controller_to_home_view(self):
        activity = (JAVA / "BoopHomeActivity.java").read_text(encoding="utf-8")

        self.assertIn("HomeDashboardController dashboardController", activity)
        self.assertIn("TvHomeView homeView", activity)
        self.assertIn("startHomeDashboard", activity)
        self.assertIn("new HomeDashboardController", activity)
        self.assertIn("repository.loadDashboard", activity)
        self.assertIn("repository.toggleBinary", activity)
        self.assertIn("preferences.cachedFavourite", activity)
        self.assertIn("preferences.setCachedFavourite", activity)
        self.assertIn("preferences.clearCachedFavourite", activity)
        self.assertIn("homeView.render", activity)
        self.assertIn("dashboardController.toggleFavourite", activity)

    def test_live_dashboard_wires_state_change_confirmation_from_socket(self):
        activity = (JAVA / "BoopHomeActivity.java").read_text(encoding="utf-8")

        self.assertIn("HomeAssistantRepository.StateChangePort", activity)
        self.assertIn("socket.subscribeStateChanges", activity)
        self.assertIn("new HomeAssistantRepository(socket::send, stateChangePort)", activity)

    def test_socket_loss_marks_existing_card_stale(self):
        activity = (JAVA / "BoopHomeActivity.java").read_text(encoding="utf-8")

        self.assertIn("dashboardController.markOffline", activity)
        self.assertIn("Home Assistant is offline.", activity)

    def test_home_view_shows_all_room_cards_and_plain_stale_label(self):
        home = (JAVA / "TvHomeView.java").read_text(encoding="utf-8")

        self.assertIn("class TvHomeView extends ScrollView", home)
        self.assertIn("void render(HomeDashboardController.ViewState state)", home)
        self.assertIn("List<EntityCard> cards = state.cards()", home)
        self.assertIn("for (EntityCard card : cards)", home)
        self.assertIn("card.displayName()", home)
        self.assertIn("card.state()", home)
        self.assertIn("current.toggle(card)", home)
        self.assertIn("Last known", home)
        self.assertIn("Use Up/Down to see the room", home)
        self.assertNotIn("entity_id", home)

    def test_activity_scaling_does_not_use_late_override_configuration(self):
        application = (JAVA / "BoopApplication.java").read_text(encoding="utf-8")

        self.assertIn("UI_SCALE = 0.80f", application)
        self.assertIn("resources.updateConfiguration", application)
        self.assertNotIn("applyOverrideConfiguration", application)


if __name__ == "__main__":
    unittest.main()
