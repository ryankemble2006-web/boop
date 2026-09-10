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
            if(url.contains("search/artist")) return "{\"data\":[{\"id\":279863,\"name\":\"Bohemian Rhapsody\"}]}";
            if(url.contains("search/track")) return "{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\",\"artist\":{\"name\":\"Queen\"},\"album\":{\"id\":9}}]}";
            throw new AssertionError("No album browsing required");
        },MediaRequest.parse("play Bohemian Rhapsody"));
        assertNotNull(s); assertEquals("https://www.deezer.com/track/7",s.url); assertEquals("Bohemian Rhapsody by Queen",s.name);
    }
    @Test public void flowDoesNotNeedPublicCatalogue() throws Exception {
        assertTrue(DeezerCatalogue.resolve((u,t,b)->{throw new AssertionError("Flow is local");},MediaRequest.parse("play music")).flow);
    }
    @Test public void rankedTrackArtistDisambiguatesDuplicateArtistNames() throws Exception {
        DeezerCatalogue.Selection s=DeezerCatalogue.resolve((u,t,b)->u.contains("search/artist")
            ? "{\"data\":[{\"id\":1,\"name\":\"Queen\"},{\"id\":2,\"name\":\"Queen\"}]}"
            : "{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\",\"artist\":{\"id\":412,\"name\":\"Queen\"}}]}",MediaRequest.parse("play Queen"));
        assertEquals("https://www.deezer.com/artist/412",s.url);
    }
}
