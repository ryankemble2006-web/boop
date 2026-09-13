import com.boop.shared.DeezerArtistMatch;
import com.boop.shared.DeezerArtistMatch.Row;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Exact artist identity, not an appearance test. No network or credentials. */
public final class DeezerArtistMatchHarness {
    private static int checks;
    private static Row row(String title, String artist, String album, long id) {
        return new Row(title, artist, album, id);
    }
    private static void expect(long expected, String title, String artist,
            String album, List<Row> rows) {
        long actual = DeezerArtistMatch.resolve(title, artist, album, rows);
        if (actual != expected) throw new AssertionError("Expected " + expected + ", got " + actual);
        checks++;
    }
    public static void main(String[] args) {
        Row exact = row("The Track", "The Artist", "The Album", 42);
        expect(42, "The Track", "The Artist", "The Album", List.of(exact));
        expect(42, "The Track", "The Artist", "", List.of(exact));
        expect(42, "The Track", "The Artist", "The Album", List.of(exact, exact));
        expect(0, "The Track", "The Artist", "The Album",
                List.of(exact, row("The Track", "The Artist", "The Album", 99)));
        expect(42, "The Track", "The Artist", "The Album",
                List.of(row("The Track", "Cover Artist", "The Album", 99), exact));
        expect(0, "Other Track", "The Artist", "The Album", List.of(exact));
        expect(0, "The Track", "Different Artist", "The Album", List.of(exact));
        expect(0, "The Track", "The Artist", "Other Album", List.of(exact));
        expect(0, "", "The Artist", "The Album", List.of(exact));
        expect(0, "The Track", null, "The Album", List.of(exact));
        expect(0, "The Track", "The Artist", "The Album", null);
        expect(0, "The Track", "The Artist", "The Album", List.of());
        expect(0, "The Track", "The Artist", "The Album",
                List.of(row("The Track", "The Artist", "The Album", 0)));
        expect(42, "  THE  TRACK ", "the artist", "the album", Arrays.asList(null, exact));
        expect(42, "The Track", "Caf\u00e9", "The Album",
                List.of(row("The Track", "Cafe\u0301", "The Album", 42)));
        expect(0, "The Track", "The Artist & Other Artist", "The Album", List.of(exact));
        expect(42, "The Track", "The Artist", null,
                List.of(exact, row("The Track", "The Artist", "Another release", 42)));
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            expect(42, "THE TRACK", "THE ARTIST", "THE ALBUM", List.of(exact));
        } finally { Locale.setDefault(original); }
        System.out.println("Deezer artist identity: " + checks + " checks passed");
    }
}
