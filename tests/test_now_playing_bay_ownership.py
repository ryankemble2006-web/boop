"""Host ownership only. These checks do not certify visual appearance."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"
HOME_SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"
PUPPET_SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java"


def test_row_owns_a_persistent_bay_not_the_transient_puppet():
    source = SOURCE.read_text(encoding="utf-8")
    assert "row.addView(puppetView," not in source, "Hiding the puppet must not remove row space"
    assert "LinearLayout puppetBay = new LinearLayout(context);" in source
    assert "row.addView(puppetBay," in source
    assert "puppetBay.addView(puppetView," in source
    assert not re.search(r"puppetBay\.(?:setVisibility|removeAllViews|removeView)\(", source)


def test_bay_does_not_create_a_second_face_or_remote_focus_target():
    source = SOURCE.read_text(encoding="utf-8")
    assert source.count("new ShieldNowPlayingPuppetView(context)") == 1
    assert "puppetBay.setFocusable(false);" in source
    assert "puppetBay.setClickable(false);" in source


def test_home_keeps_one_canonical_assistant_presence_when_media_is_gone():
    home = HOME_SOURCE.read_text(encoding="utf-8")
    assert "private ShieldNowPlayingPuppetView homeAssistantPuppet;" in home
    assert "homeAssistantPuppet = new ShieldNowPlayingPuppetView(getContext());" in home
    assert "homeAssistantPuppet.setPresentationOwner(com.boop.shared.BoopState.Owner.NONE);" in home
    assert "homeAssistantPuppet.setSnapshot(idleAssistantSnapshot());" in home
    assert "homeAssistantPuppet.setHomeVisible(!visible);" in home
    assert "PlaybackState.STATE_BUFFERING" in home, "idle presence should reuse the canonical idle media pose"


def test_home_assistant_is_separate_from_top_shield_settings_and_owns_bottom_corner():
    home = HOME_SOURCE.read_text(encoding="utf-8")
    assert "LinearLayout assistantBay = new LinearLayout(getContext());" not in home
    assert "row.addView(settings, settingsParams);" in home
    assert "FrameLayout.LayoutParams assistantParams" in home
    assert "Gravity.END | Gravity.BOTTOM" in home
    assert "homeStage.addView(homeAssistantPuppet, assistantParams);" in home


def test_puppet_releases_gl_surface_when_ownership_hides_it():
    source = PUPPET_SOURCE.read_text(encoding="utf-8")
    assert "puppet.setSurfaceActive(false);" in source
    assert "puppet.setSurfaceActive(true);" in source
    assert "void setSurfaceActive(boolean active)" in source
    assert "eyeSurface.setVisibility(active ? VISIBLE : INVISIBLE);" in source
    assert "eyeSurface.onPause();" in source
    assert "eyeSurface.onResume();" in source


def test_media_attach_cannot_mutate_an_idle_home_hosts_parameters(tmp_path):
    """Execute the actual attach-time mutator, not a duplicate of its logic.

    View stand-ins record parameter writes; they do not simulate rendering or
    certify appearance. Removing the owner guard must fail the idle-host case.
    The installed Shield screenshot remains the separate visual evidence.
    """
    source = PUPPET_SOURCE.read_text(encoding="utf-8")
    start = source.index("private void lockToMascotBay()")
    opening = source.index("{", start)
    depth = 0
    for end in range(opening, len(source)):
        if source[end] == "{":
            depth += 1
        elif source[end] == "}":
            depth -= 1
            if depth == 0:
                break
    else:
        raise AssertionError("Unclosed production attach mutator")
    production_method = source[start:end + 1]
    constants = "\n".join(re.findall(
        r"private static final int BAY_\w+\s*=\s*\d+;", source))
    bay_width = re.search(r"MASCOT_BAY_DP\s*=\s*(\d+)",
                          SOURCE.read_text(encoding="utf-8")).group(1)
    files = {
        "android/view/ViewGroup.java": """package android.view;
public class ViewGroup {
    public static class LayoutParams {
        public int width, height;
        public LayoutParams(int w, int h) { width=w; height=h; }
    }
}
""",
        "android/widget/FrameLayout.java": """package android.widget;
public class FrameLayout {
    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public int gravity, leftMargin, topMargin, rightMargin, bottomMargin;
        public LayoutParams(int w, int h, int g) { super(w,h); gravity=g; }
    }
}
""",
        "android/view/Gravity.java": """package android.view;
public class Gravity { public static final int END=8388613, TOP=48, BOTTOM=80; }
""",
        "com/boop/shared/BoopState.java": """package com.boop.shared;
public class BoopState { public enum Owner { NONE, HOME_NOW_PLAYING } }
""",
        "com/boop/shieldhome/ShieldNowPlayingView.java":
            "package com.boop.shieldhome; class ShieldNowPlayingView { "
            + "static final int MASCOT_BAY_DP=" + bay_width + "; }",
    }
    harness = """package com.boop.shieldhome;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.ViewGroup;
import com.boop.shared.BoopState;
public final class HostOwnershipProbe {
    // CONSTANTS
    private BoopState.Owner presentationOwner;
    private ViewGroup.LayoutParams params;
    private float density;
    private int writes;
    private ViewGroup.LayoutParams getLayoutParams() { return params; }
    private void setLayoutParams(ViewGroup.LayoutParams p) { params=p; writes++; }
    private int dp(int value) { return Math.round(value*density); }
    // PRODUCTION_METHOD
    private static String state(FrameLayout.LayoutParams p) {
        return p.width+","+p.height+","+p.gravity+","+p.leftMargin+","+p.topMargin
                +","+p.rightMargin+","+p.bottomMargin;
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        for (float density : new float[] {1f, 1.6f, 2f}) {
            HostOwnershipProbe home=new HostOwnershipProbe();
            home.density=density;
            home.presentationOwner=BoopState.Owner.NONE;
            FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(
                    home.dp(360), home.dp(220), Gravity.END | Gravity.BOTTOM);
            p.rightMargin=home.dp(-42); p.bottomMargin=home.dp(-30);
            home.params=p;
            String before=state(p);
            home.lockToMascotBay();
            home.lockToMascotBay();
            check(before.equals(state(p)) && home.params==p && home.writes==0,
                    "Idle Home host parameters overwritten at attach: "
                    +before+" -> "+state(p));

            HostOwnershipProbe media=new HostOwnershipProbe();
            media.density=density;
            media.presentationOwner=BoopState.Owner.HOME_NOW_PLAYING;
            FrameLayout.LayoutParams q=new FrameLayout.LayoutParams(10,20,Gravity.BOTTOM);
            media.params=q;
            media.lockToMascotBay();
            check(q.width==media.dp(ShieldNowPlayingView.MASCOT_BAY_DP)
                    && q.height==media.dp(BAY_HEIGHT_DP)
                    && q.gravity==(Gravity.END | Gravity.TOP)
                    && q.rightMargin==media.dp(BAY_RIGHT_MARGIN_DP)
                    && q.topMargin==media.dp(BAY_TOP_MARGIN_DP),
                    "Existing media bay allocation changed");
            check(media.writes==1, "Media host should update mismatched parameters once");
            media.lockToMascotBay();
            check(media.writes==1, "Matching media host should not request another layout");
            ViewGroup.LayoutParams linear=new ViewGroup.LayoutParams(11,22);
            media.params=linear;
            media.lockToMascotBay();
            check(media.params==linear && media.writes==1,
                    "Linear media bay must retain parent parameters");
            media.params=null;
            media.lockToMascotBay();
            check(media.writes==1, "Unassigned parent should be safe");
        }
        System.out.println("PASS: idle Home host isolation; existing media host unchanged");
    }
}
""".replace("// CONSTANTS", constants).replace("// PRODUCTION_METHOD", production_method)
    files["com/boop/shieldhome/HostOwnershipProbe.java"] = harness
    paths = []
    for relative, content in files.items():
        path = tmp_path / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        paths.append(str(path))
    compiled = subprocess.run(["javac", "-d", str(tmp_path), *paths],
                              capture_output=True, text=True, timeout=60)
    assert compiled.returncode == 0, compiled.stdout + compiled.stderr
    result = subprocess.run(["java", "-cp", str(tmp_path),
                             "com.boop.shieldhome.HostOwnershipProbe"],
                            capture_output=True, text=True, timeout=30)
    assert result.returncode == 0, result.stdout + result.stderr
    assert "PASS: idle Home host isolation" in result.stdout
