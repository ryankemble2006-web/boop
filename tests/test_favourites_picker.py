"""Non-visual favourite model and selector wiring checks."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SHIELD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_add_only_favourite_model():
    def tool(name):
        home = os.environ.get("JAVA_HOME")
        return str(Path(home) / "bin" / (name + (".exe" if os.name == "nt" else ""))) if home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix="boop-favourite-picker-") as output:
        sources = [SHIELD / (name + ".java") for name in
                   ["TvAppEntry", "FavouriteOrder", "FavouriteGrabSession"]]
        subprocess.run([tool("javac"), "-d", output, *map(str, sources),
                        str(ROOT / "tests/java/FavouritePickerHarness.java")], check=True)
        subprocess.run([tool("java"), "-cp", output,
                        "com.boop.shieldhome.FavouritePickerHarness"], check=True)


def test_home_selector_is_add_only_and_not_an_app_launch():
    home = (SHIELD / "ShieldHomeView.java").read_text(encoding="utf-8")
    activity = (SHIELD / "ShieldLauncherActivity.java").read_text(encoding="utf-8")
    assert "callbacks.onAddFavourite()" in home
    assert "private void showFavouritePicker()" in activity
    picker = activity[activity.index("private void showFavouritePicker()"):
                      activity.index("private void toggleFavourite")]
    assert "FavouriteOrder.availableToAdd" in picker
    assert "FavouriteOrder.addInstalled" in picker
    assert "saveFavouriteEdit(next)" in picker
    assert "launchApp(" not in picker and "toggleFavourite(" not in picker
    assert 'setNegativeButton("Cancel", null)' in picker
    assert "favouritePicker.dismiss()" in activity


def test_add_tile_survives_reordering_and_is_the_last_stop():
    home = (SHIELD / "ShieldHomeView.java").read_text(encoding="utf-8")
    reorder = home[home.index("private void reorderFavouriteChildren"):
                   home.index("private void scrollGrabbedIntoView")]
    assert "favouriteRow.addView(addFavouriteTile)" in reorder
    assert "favouriteRow.indexOfChild(v)" in home
    assert "focusAddFavourite()" in home
    assert "safeFavourites.isEmpty()" not in home


if __name__ == "__main__":
    test_add_only_favourite_model()
    test_home_selector_is_add_only_and_not_an_app_launch()
    test_add_tile_survives_reordering_and_is_the_last_stop()
