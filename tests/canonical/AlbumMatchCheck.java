import com.boop.shared.DeezerAlbumMatch;
import java.util.Arrays;
public final class AlbumMatchCheck {
    static DeezerAlbumMatch.Row row(String t,String a,String album,long id) { return new DeezerAlbumMatch.Row(t,a,album,id); }
    static void check(boolean value) { if(!value) throw new AssertionError("album match"); }
    public static void main(String[] args) {
        check(DeezerAlbumMatch.resolve("Song","Singer","Album",Arrays.asList(row("Song","Singer","Other",1),row("Song","Singer","Album",2)))==2);
        check(DeezerAlbumMatch.resolve("Song","Singer","Album",Arrays.asList(row("Song","Cover","Album",2)))==0);
        check(DeezerAlbumMatch.resolve("Song","Singer","Album",Arrays.asList(row("Song","Singer","Album",2),row("Song","Singer","Album",3)))==0);
        check(DeezerAlbumMatch.resolve("Song","Singer","",Arrays.asList(row("Song","Singer","Album",2)))==0);
        check(DeezerAlbumMatch.resolve("Song","Singer","Album",Arrays.asList(row("Song live","Singer","Album",2)))==0);
        check(DeezerAlbumMatch.resolve("Song","Singer","Album",Arrays.asList(row("Song","Singer","Album",2),row("Song","Singer","Album",2)))==2);
        System.out.println("Album matching checks passed");
    }
}
