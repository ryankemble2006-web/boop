"""Non-visual checks for artist-only focus styling; no device or emulator access."""
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'


def java_method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 0
    for index in range(opening, len(source)):
        if source[index] == '{':
            depth += 1
        elif source[index] == '}':
            depth -= 1
            if depth == 0:
                return source[start:index + 1]
    raise AssertionError('Unclosed production method: ' + signature)


def roots():
    result = [SOURCE]
    generated = ROOT / 'boop-build/BOOP-Alpha1/shield-home-lib/src/main/java/com/boop/shieldhome'
    if generated.is_dir():
        result.append(generated)
    return result


def test_artist_alone_uses_text_only_focus_without_changing_navigation():
    for root in roots():
        view = (root / 'ShieldNowPlayingView.java').read_text()
        assert 'BoopTvChrome.useTextOnlyFocus(subtitle);' in view, 'Artist still receives generic button chrome'
        assert view.count('BoopTvChrome.useTextOnlyFocus(') == 1
        assert 'subtitle.setOnFocusChangeListener' not in view, 'A focus callback must not replace state-list colours'
        assert 'subtitle.setOnKeyListener(this::handleArtistKey);' in view
        assert 'callbacks.onBrowseNowPlayingArtist();' in view
        assert 'subtitle.setFocusable(artistAvailable);' in view
        assert 'subtitle.setClickable(artistAvailable);' in view
        assert 'progress.setProgressTintList(ColorStateList.valueOf(FocusChrome.accentColor(context)));' in view
        focus = (root / 'FocusChrome.java').read_text()
        assert 'BoopTvChrome.accentColor' in focus


def test_actual_chrome_methods_preserve_artist_style_and_normal_buttons(tmp_path):
    """Execute production decoration methods against property-recording View doubles.

    This checks emitted colours/backgrounds and repeat-decoration behaviour, not
    Android rendering, focus delivery or physical navigation. Ryan tests those.
    """
    for number, root in enumerate(roots()):
        chrome = (root / 'BoopTvChrome.java').read_text()
        signatures = [
            'public static int accentColor(Context context)',
            'public static GradientDrawable filled(',
            'private static void decorateTree(View view)',
            'private static boolean eligible(View view)',
            'private static void applyState(TextView view, boolean focused)',
        ]
        optional = [
            'public static void useTextOnlyFocus(TextView view)',
            'private static boolean isTextOnlyAction(View view)',
        ]
        methods = '\n'.join(java_method(chrome, s) for s in signatures + [s for s in optional if s in chrome])
        fields = '\n'.join(line.strip() for line in chrome.splitlines()
                           if re.match(r'\s*(?:public|private) static final (?:int|float|long|Set<View>) ', line))
        registration = 'useTextOnlyFocus(artist);' if optional[0] in chrome else ''
        harness = r'''
import java.util.*;
class android { static class R { static class attr { static final int state_focused = 1; } } }
class Context { }
class Color {
    static final int WHITE = 0xffffffff;
    static int rgb(int r, int g, int b) { return 0xff000000 | r << 16 | g << 8 | b; }
}
class ColorStateList {
    final int[][] states; final int[] colors;
    ColorStateList(int[][] states, int[] colors) { this.states = states; this.colors = colors; }
}
class GradientDrawable {
    int color, stroke;
    void setColor(int color) { this.color = color; }
    void setCornerRadius(float radius) { }
    void setStroke(int width, int color) { stroke = width; }
}
class View {
    Object background;
    boolean focus, clickable = true, focusable = true, defaultHighlight = true;
    int animations;
    float scaleX = 1f, scaleY = 1f;
    Context getContext() { return new Context(); }
    boolean isClickable() { return clickable; }
    boolean isFocusable() { return focusable; }
    boolean hasFocus() { return focus; }
    void setBackground(Object background) { this.background = background; }
    void setDefaultFocusHighlightEnabled(boolean enabled) { defaultHighlight = enabled; }
    Animator animate() { animations++; return new Animator(this); }
}
class Animator {
    final View view;
    Animator(View view) { this.view = view; }
    Animator scaleX(float value) { view.scaleX = value; return this; }
    Animator scaleY(float value) { view.scaleY = value; return this; }
    Animator setDuration(long duration) { return this; }
    void start() { }
}
class TextView extends View {
    ColorStateList colours;
    int color;
    void setTextColor(ColorStateList value) { colours = value; }
    void setTextColor(int value) { colours = null; color = value; }
}
class EditText extends TextView { }
class SeekBar extends View { }
class ImageView extends View { }
class CompoundButton extends TextView { }
class ViewGroup extends View {
    final View[] children;
    ViewGroup(View... children) { this.children = children; }
    int getChildCount() { return children.length; }
    View getChildAt(int index) { return children[index]; }
}
public class ChromeProbe {
    // FIELDS
    static int dp(Context context, int value) { return value; }
    static void tintSlider(SeekBar slider) { }
    // METHODS
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void checkArtist(TextView artist) {
        check(artist.background == null, "Artist acquired the unwanted button box");
        check(!artist.defaultHighlight, "Android default focus rectangle remains enabled");
        check(artist.colours != null, "Generic chrome overwrote artist state-list colours");
        check(artist.colours.states.length == 2 && artist.colours.colors.length == 2, "Unexpected artist states");
        check(Arrays.equals(artist.colours.states[0], new int[]{android.R.attr.state_focused}), "First artist state must be focus");
        check(artist.colours.colors[0] == accentColor(artist.getContext()), "Artist cyan differs from progress accent");
        check(artist.colours.states[1].length == 0 && artist.colours.colors[1] == Color.WHITE, "Unfocused artist is not white");
        check(artist.animations == 0, "Artist unexpectedly receives generic focus scaling");
        check(artist.clickable && artist.focusable, "Artist action lost navigation/clickability");
    }
    public static void main(String[] args) {
        TextView artist = new TextView();
        TextView button = new TextView();
        // REGISTER
        ViewGroup root = new ViewGroup(artist, button);
        for (boolean focused : new boolean[]{false, true, false, true, false}) {
            artist.focus = focused;
            button.focus = focused;
            decorateTree(root);
            checkArtist(artist);
            check(!eligible(artist), "Global focus handler may still restyle the artist");
            applyState(artist, focused); // A stale queued generic callback must also be harmless.
            checkArtist(artist);
            check(eligible(button), "Ordinary buttons were accidentally exempted");
            check(button.color == Color.WHITE, "Ordinary button text changed");
            check(button.background instanceof GradientDrawable, "Ordinary button box disappeared");
            GradientDrawable bg = (GradientDrawable) button.background;
            check(bg.color == Color.rgb(34, 34, 34), "Ordinary button fill changed");
            check(bg.stroke == (focused ? BORDER_DP : 0), "Ordinary button focus border changed");
            check(button.scaleX == (focused ? FOCUSED_SCALE : 1f), "Ordinary button focus scale changed");
        }
        System.out.println("PASS: artist-only text focus; generic buttons unchanged");
    }
}
'''.replace('// FIELDS', fields).replace('// METHODS', methods).replace('// REGISTER', registration)
        work = tmp_path / str(number)
        work.mkdir()
        file = work / 'ChromeProbe.java'
        file.write_text(harness)
        compiled = subprocess.run(['javac', '-d', str(work), str(file)], capture_output=True, text=True, timeout=60)
        assert compiled.returncode == 0, compiled.stdout + compiled.stderr
        result = subprocess.run(['java', '-cp', str(work), 'ChromeProbe'], capture_output=True, text=True, timeout=30)
        assert result.returncode == 0, str(root) + '\n' + result.stdout + result.stderr
        assert 'PASS: artist-only text focus' in result.stdout
