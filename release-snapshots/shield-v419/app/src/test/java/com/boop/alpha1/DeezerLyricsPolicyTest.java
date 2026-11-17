import com.boop.shieldhome.DeezerLyricsPolicy;

public final class DeezerLyricsPolicyTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(DeezerLyricsPolicy.available("deezer.android.app"), "native Deezer has lyrics shortcut");
        check(!DeezerLyricsPolicy.available("com.google.android.apps.mediashell"), "Cast is not native Deezer");
        check(!DeezerLyricsPolicy.available("com.google.android.youtube.tv"), "YouTube has no Deezer lyrics shortcut");
        check(!DeezerLyricsPolicy.available(null), "null package is unavailable");
        System.out.println("DeezerLyricsPolicyTest PASS");
    }
}
