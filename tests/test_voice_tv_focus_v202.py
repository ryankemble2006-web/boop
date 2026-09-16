"""Non-visual Voice focus configuration tests; Shield proves actual navigation."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
CHROME = "unified/shield-home/src/main/java/com/boop/shieldhome/BoopTvChrome.java"


def method(source, signature):
    assert signature in source, "Missing Voice TV focus behavior: " + signature
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 0
    for index in range(opening, len(source)):
        if source[index] == "{":
            depth += 1
        elif source[index] == "}":
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise AssertionError("Unclosed method: " + signature)


def test_voice_focus_is_connected_after_scroll_attachment():
    call = "com.boop.shieldhome.BoopTvChrome.prepareVoiceSettings("
    patch = (ROOT / "scripts/patch-unified-v200-voice-ui.py").read_text()
    assert call in patch, "Voice settings still has no TV child-focus handoff"
    assert "voiceSettingsScroll, voiceSettingsOverlay, pitchSlider" in patch
    built = ROOT / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
    if built.is_file():
        show = method(built.read_text(), "private void showVoiceSettings()")
        assert show.index("voiceSettingsScroll.bringToFront()") < show.index(call)
        assert show.count(call) == 1
        assert 'face.setOccluded("voice_settings", true)' in show


def test_real_focus_configuration_preserves_native_controls(tmp_path):
    # Compile actual production methods with small framework stand-ins. This
    # verifies flags, initial target and drawable state wiring, not Android's
    # focus-search engine or compositor. No key/touch callbacks are replaced.
    roots = [ROOT / CHROME]
    built = ROOT / "boop-build/BOOP-Alpha1/shield-home-lib/src/main/java/com/boop/shieldhome/BoopTvChrome.java"
    if built.is_file():
        roots.append(built)
    for number, path in enumerate(roots):
        source = path.read_text()
        actual = "\n".join(method(source, signature) for signature in (
            "public static void prepareVoiceSettings(",
            "private static void prepareVoiceContainer(",
            "private static void decorateVoiceEditors(",
            "private static boolean isTelevision(",
        ))
        for forbidden in ("setOnKeyListener", "setOnTouchListener", "setOnFocusChangeListener", "setProgress", "setText("):
            assert forbidden not in actual, "Voice focus must not replace existing control behavior"
        harness = r'''import java.util.*;
class Configuration {
    static final int UI_MODE_TYPE_MASK = 15, UI_MODE_TYPE_TELEVISION = 4;
    int uiMode;
}
class Resources {
    final Configuration configuration = new Configuration();
    Configuration getConfiguration() { return configuration; }
}
class Context {
    final Resources resources = new Resources();
    Resources getResources() { return resources; }
}
class Drawable { }
class Outline extends Drawable {
    final int fill, corners;
    final boolean focused;
    Outline(int f, int c, boolean b) { fill=f; corners=c; focused=b; }
}
class StateListDrawable extends Drawable {
    final List<int[]> states = new ArrayList<>();
    final List<Drawable> drawables = new ArrayList<>();
    void addState(int[] state, Drawable drawable) { states.add(state); drawables.add(drawable); }
}
class Color { static final int TRANSPARENT = 0; }
class android { static class R { static class attr { static final int state_focused = 16842908; } } }
class View {
    static final ArrayDeque<Runnable> posts = new ArrayDeque<>();
    final Context context;
    boolean focusable=true, touchFocusable=true, attached=true, shown=true, enabled=true, focused;
    final Object background = new Object();
    Drawable foreground;
    View(Context c) { context=c; }
    Context getContext() { return context; }
    void setFocusable(boolean b) { focusable=b; }
    void setFocusableInTouchMode(boolean b) { touchFocusable=b; if(b) focusable=true; }
    boolean isAttachedToWindow() { return attached; }
    boolean isShown() { return shown; }
    boolean isEnabled() { return enabled; }
    void setForeground(Drawable d) { foreground=d; }
    void post(Runnable r) { posts.add(r); }
    boolean requestFocus() { focused=focusable && enabled && shown; return focused; }
}
class ViewGroup extends View {
    static final int FOCUS_AFTER_DESCENDANTS=262144;
    int descendants;
    final List<View> children=new ArrayList<>();
    ViewGroup(Context c) { super(c); }
    void setDescendantFocusability(int value) { descendants=value; }
    int getChildCount() { return children.size(); }
    View getChildAt(int i) { return children.get(i); }
}
class EditText extends View { EditText(Context c) { super(c); } }
class SeekBar extends View { SeekBar(Context c) { super(c); } }
class Button extends View { Button(Context c) { super(c); } }
public class BoopTvChrome {
    static final int CORNER_DP=10;
    static Drawable filled(Context c,int fill,int corners,boolean focused) { return new Outline(fill,corners,focused); }
    // ACTUAL_METHODS
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    static void drain() { while(!View.posts.isEmpty()) View.posts.remove().run(); }
    static void outline(View view) {
        check(view.foreground instanceof StateListDrawable,"native editor has focus-state foreground");
        StateListDrawable states=(StateListDrawable)view.foreground;
        check(states.states.size()==2,"focused and resting drawable states");
        check(Arrays.equals(states.states.get(0),new int[]{android.R.attr.state_focused}),"native focused state");
        Outline focused=(Outline)states.drawables.get(0), rest=(Outline)states.drawables.get(1);
        check(focused.focused && !rest.focused,"outline clears when focus leaves");
        check(focused.fill==Color.TRANSPARENT && rest.fill==Color.TRANSPARENT,"track and text remain visible");
        check(focused.corners==CORNER_DP,"canonical BOOP corner size");
    }
    public static void main(String[] args) {
        Context tv=new Context(); tv.resources.configuration.uiMode=0x24;
        ViewGroup scroll=new ViewGroup(tv), column=new ViewGroup(tv), nested=new ViewGroup(tv);
        SeekBar pitch=new SeekBar(tv), cadence=new SeekBar(tv);
        EditText name=new EditText(tv); Button test=new Button(tv);
        column.children.add(name); column.children.add(pitch); column.children.add(nested);
        nested.children.add(cadence); nested.children.add(test);
        Object originalPitchBackground=pitch.background, originalNameBackground=name.background;
        prepareVoiceSettings(scroll,column,pitch);
        check(!scroll.focusable && !column.focusable,"containers cannot steal D-pad focus");
        check(!scroll.touchFocusable && !column.touchFocusable,"touch-mode focus trap removed");
        check(scroll.descendants==ViewGroup.FOCUS_AFTER_DESCENDANTS && column.descendants==ViewGroup.FOCUS_AFTER_DESCENDANTS,"children own navigation");
        check(!pitch.focused,"initial focus waits for attached layout"); drain();
        check(pitch.focused && !column.focused,"Pitch is the initial target");
        outline(pitch); outline(cadence); outline(name);
        check(test.foreground==null,"existing button chrome unchanged");
        check(pitch.background==originalPitchBackground && name.background==originalNameBackground,"native backgrounds preserved");
        column.attached=false; pitch.focused=false;
        prepareVoiceSettings(scroll,column,pitch); drain();
        check(!pitch.focused,"dismissed page cannot reclaim focus");
        column.attached=true; pitch.enabled=false;
        prepareVoiceSettings(scroll,column,pitch); drain();
        check(!pitch.focused,"disabled target is not focused");
        Context phone=new Context(); phone.resources.configuration.uiMode=1;
        ViewGroup phoneScroll=new ViewGroup(phone), phoneColumn=new ViewGroup(phone);
        SeekBar phonePitch=new SeekBar(phone); phoneColumn.children.add(phonePitch);
        prepareVoiceSettings(phoneScroll,phoneColumn,phonePitch); drain();
        check(phoneScroll.focusable && phoneColumn.focusable && phonePitch.foreground==null && !phonePitch.focused,"phone behavior untouched");
        System.out.println("PASS: TV child focus, native editor outlines, dismissal and phone isolation");
    }
}
'''.replace("// ACTUAL_METHODS", actual)
        work = tmp_path / str(number)
        work.mkdir()
        java = work / "BoopTvChrome.java"
        java.write_text(harness)
        compiled = subprocess.run(["javac", "-d", str(work), str(java)], capture_output=True, text=True, timeout=60)
        assert compiled.returncode == 0, compiled.stdout + compiled.stderr
        run = subprocess.run(["java", "-cp", str(work), "BoopTvChrome"], capture_output=True, text=True, timeout=30)
        assert run.returncode == 0, run.stdout + run.stderr
        assert "PASS: TV child focus" in run.stdout
