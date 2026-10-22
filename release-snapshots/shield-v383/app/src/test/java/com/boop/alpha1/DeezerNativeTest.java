package com.boop.alpha1;

import com.boop.shared.MediaRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.util.*;
import java.io.IOException;

public class DeezerNativeTest {
    @Test public void sameButtonWrapperCanOwnFocus() throws Exception {
        com.boop.shared.DeezerScreen screen=com.boop.shared.DeezerScreen.parse("<hierarchy><node package='deezer.android.app' focused='true' bounds='[0,0][100,100]'><node clickable='true' enabled='true' focused='false' bounds='[0,0][100,100]' text='Play top tracks'/></node></hierarchy>");
        assertTrue(screen.target("Play top tracks").focused);
        screen=com.boop.shared.DeezerScreen.parse("<hierarchy><node package='deezer.android.app' focused='true' bounds='[0,0][200,200]'><node clickable='true' enabled='true' focused='false' bounds='[0,0][100,100]' text='Play top tracks'/></node></hierarchy>");
        assertFalse(screen.target("Play top tracks").focused);
    }
    @Test public void exactTrackUsesItsOwnIdWithoutAlbumNavigation() throws Exception {
        DeezerCatalogue.Selection s=DeezerCatalogue.resolve((url,token,body)->{
            assertNull(token); assertNull(body);
            if(url.contains("search/artist")) return "{\"data\":[]}";
            if(url.contains("search/track")) return "{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\",\"artist\":{\"id\":412,\"name\":\"Queen\"},\"album\":{\"id\":9}},{\"id\":8,\"title\":\"Don't Stop Me Now\",\"artist\":{\"id\":279863,\"name\":\"Bohemian Rhapsody\"}}]}";
            throw new AssertionError("No album browsing required");
        },MediaRequest.parse("play Bohemian Rhapsody"));
        assertNotNull(s); assertEquals("https://www.deezer.com/track/7",s.url); assertEquals("Bohemian Rhapsody by Queen",s.name);
    }
    @Test public void flowDoesNotNeedPublicCatalogue() throws Exception {
        assertTrue(DeezerCatalogue.resolve((u,t,b)->{throw new AssertionError("Flow is local");},MediaRequest.parse("play music")).flow);
    }
    @Test public void rankedTrackArtistDisambiguatesDuplicateArtistNames() throws Exception {
        DeezerCatalogue.Selection s=DeezerCatalogue.resolve((u,t,b)->u.contains("search/artist")
            ? "{\"data\":[{\"id\":412,\"name\":\"Queen\"},{\"id\":2,\"name\":\"Queen\"}]}"
            : "{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\",\"artist\":{\"id\":412,\"name\":\"Queen\"}},{\"id\":8,\"title\":\"Don't Stop Me Now\",\"artist\":{\"id\":412,\"name\":\"Queen\"}}]}",MediaRequest.parse("play Queen"));
        assertEquals("https://www.deezer.com/artist/412",s.url);
    }
    @Test public void artistNameWinsEvenWhenNamesakeSongRanksFirst() throws Exception {
        DeezerCatalogue.Selection selected=lennon("play John Lennon");
        assertTrue(selected.ambiguous());
        assertEquals("Do you mean the artist John Lennon, or the song?", selected.question);
        DeezerCatalogue.Selection answered=DeezerCatalogue.answer(selected, "the artist");
        assertNotNull(answered);
        assertEquals("https://www.deezer.com/artist/226", answered.url);
        assertFalse(answered.track);
        DeezerCatalogue.Selection explicit=lennon("play artist John Lennon");
        assertEquals("https://www.deezer.com/artist/226", explicit.url);
        assertFalse(explicit.track);
    }
    @Test public void explicitPerformerStillSelectsNamesakeSong() throws Exception {
        assertEquals("https://www.deezer.com/track/112736672",lennon("play John Lennon by SCH").url);
    }
    private DeezerCatalogue.Selection lennon(String command) throws Exception {
        return DeezerCatalogue.resolve((u,t,b)->{
            if (u.contains("search/artist")) {
                if (u.contains("SCH")) return "{\"data\":[{\"id\":162665,\"name\":\"SCH\"}]}";
                return "{\"data\":[{\"id\":226,\"name\":\"John Lennon\"}]}";
            }
            return "{\"data\":[{\"id\":112736672,\"title\":\"John Lennon\",\"artist\":{\"id\":162665,\"name\":\"SCH\"}},{\"id\":7193834,\"title\":\"Imagine\",\"artist\":{\"id\":226,\"name\":\"John Lennon\"}},{\"id\":7163159,\"title\":\"Woman\",\"artist\":{\"id\":226,\"name\":\"John Lennon\"}}]}";
        },MediaRequest.parse(command));
    }
}
