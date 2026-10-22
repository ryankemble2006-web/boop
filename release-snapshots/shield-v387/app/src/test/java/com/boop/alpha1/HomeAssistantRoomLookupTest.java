package com.boop.alpha1;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;
public class HomeAssistantRoomLookupTest {
 @Test public void registryIdIsPreservedWithoutGuessingSlug() throws Exception {
  BoopRoom room=HomeAssistantRoomLookup.parse(new JSONObject().put("id","area_23").put("name","Front room"));
  assertEquals("area_23",room.id()); assertEquals("Front room",room.name());
 }
 @Test public void unknownOrPartialRoomCannotBeSaved() throws Exception {
  assertNull(HomeAssistantRoomLookup.parse(new JSONObject().put("id",JSONObject.NULL).put("name",JSONObject.NULL)));
  assertNull(HomeAssistantRoomLookup.parse(new JSONObject().put("id","" ).put("name","Guess")));
 }
}
