import com.boop.shared.DeezerScreen;

public final class LyricsTargetCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        DeezerScreen one = DeezerScreen.parse("<hierarchy><node package='deezer.android.app'><node clickable='true' enabled='true' bounds='[20,40][220,120]'><node content-desc='Lyrics'/></node></node></hierarchy>");
        DeezerScreen.Target target = one.lyricsTarget();
        check(target != null && target.x == 120 && target.y == 80, "nested semantic Lyrics control resolves to clickable parent");

        DeezerScreen two = DeezerScreen.parse("<hierarchy><node package='deezer.android.app'><node text='Lyrics' clickable='true' enabled='true' bounds='[0,0][100,100]'/><node content-desc='Lyrics' clickable='true' enabled='true' bounds='[100,0][200,100]'/></node></hierarchy>");
        check(two.lyricsTarget() == null, "ambiguous Lyrics controls fail closed");

        DeezerScreen lyrics = DeezerScreen.parse("<hierarchy><node package='deezer.android.app'><node scrollable='true'><node class='android.widget.TextView' text='line one'/><node class='android.widget.TextView' text='line two'/><node class='android.widget.TextView' text='line three'/></node></node></hierarchy>");
        check(lyrics.hasLyricsPanel(), "scrolling Deezer lyric text is recognised");

        DeezerScreen player = DeezerScreen.parse("<hierarchy><node package='deezer.android.app'><node class='android.widget.TextView' text='Track'/><node content-desc='Lyrics' clickable='true' enabled='true' bounds='[0,0][100,100]'/></node></hierarchy>");
        check(!player.hasLyricsPanel(), "ordinary player is not mistaken for lyrics");
        System.out.println("LyricsTargetCheck PASS");
    }
}
