package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.json.JSONArray;
import org.junit.Test;

public final class HomeAssistantMediaSelectorTest {
    @Test public void prefersLivingRoomPlayerWithActiveAppEvenWhenStateIsOn() throws Exception {
        JSONArray areaEntities = new JSONArray("[\"media_player.living_room_tv\",\"media_player.receiver\",\"light.candle\"]");
        JSONArray states = new JSONArray("["
                + "{\"entity_id\":\"media_player.living_room_tv\",\"state\":\"on\",\"attributes\":{\"friendly_name\":\"TV\",\"app_id\":\"deezer.android.app\"}},"
                + "{\"entity_id\":\"media_player.receiver\",\"state\":\"on\",\"attributes\":{\"friendly_name\":\"TA-AN1000\"}},"
                + "{\"entity_id\":\"media_player.bedroom_tv\",\"state\":\"playing\",\"attributes\":{\"friendly_name\":\"Bedroom TV\",\"media_title\":\"Film\"}}"
                + "]");

        List<HomeAssistantMediaSelector.Candidate> candidates =
                HomeAssistantMediaSelector.rank(areaEntities, states);

        assertEquals("media_player.living_room_tv", candidates.get(0).entityId());
        assertEquals("TV", candidates.get(0).name());
    }

    @Test public void realPlayingStateBeatsIdleAppMetadata() throws Exception {
        JSONArray areaEntities = new JSONArray("[\"media_player.tv\",\"media_player.speaker\"]");
        JSONArray states = new JSONArray("["
                + "{\"entity_id\":\"media_player.tv\",\"state\":\"on\",\"attributes\":{\"friendly_name\":\"TV\",\"app_id\":\"launcher\"}},"
                + "{\"entity_id\":\"media_player.speaker\",\"state\":\"playing\",\"attributes\":{\"friendly_name\":\"Living Room speaker\",\"media_title\":\"Song\"}}"
                + "]");

        List<HomeAssistantMediaSelector.Candidate> candidates =
                HomeAssistantMediaSelector.rank(areaEntities, states);

        assertEquals("media_player.speaker", candidates.get(0).entityId());
    }

    @Test public void ignoresNonMediaAndUnavailableEntities() throws Exception {
        JSONArray areaEntities = new JSONArray("[\"light.candle\",\"media_player.dead\"]");
        JSONArray states = new JSONArray("[{\"entity_id\":\"media_player.dead\",\"state\":\"unavailable\",\"attributes\":{}}]");

        assertTrue(HomeAssistantMediaSelector.rank(areaEntities, states).isEmpty());
    }
}
