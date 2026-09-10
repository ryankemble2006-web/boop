package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

final class GenericHomeAssistantClient {
    private static final int TIMEOUT_MS = 5000;
    private final HomeAssistantEntityDiscoveryClient discovery = new HomeAssistantEntityDiscoveryClient();

    CommandOutcome process(String baseUrl, String token, String text, BoopRoom room)
            throws HomeAssistantAuth.AuthRejectedException {
        GenericHomeCommand command = BoopRoomCommandParser.parse(text, room.id(), room.name());
        if (command == null || command.explicitOtherRoom() || command.groupTarget()) return null;
        try {
            HomeAssistantEntityResolver.Result resolution = HomeAssistantEntityResolver.resolveResult(
                    command, discover(baseUrl, token, room));
            if (resolution.kind() == HomeAssistantEntityResolver.Kind.AMBIGUOUS) {
                return CommandOutcome.ambiguousTarget();
            }
            HomeAssistantEntity entity = resolution.entity();
            if (entity == null) return null;
            int status = service(baseUrl, token, command.service(), entity.entityId());
            if (status == 401 || status == 403) throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            return status >= 200 && status < 300 ? CommandOutcome.success(entity.name()) : null;
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            throw e;
        } catch (Exception ignored) {
            // Discovery is an optimisation. Assist remains authoritative when evidence is absent.
            return null;
        }
    }

    private List<HomeAssistantEntity> discover(String baseUrl, String token, BoopRoom room) throws Exception {
        // area_entities accepts a canonical area name or ID; the stored name avoids
        // guessing IDs for installations whose slug differs from the display name.
        String area = room.name().replace("\\", "\\\\").replace("'", "\\'");
        String template = "{{ area_entities('" + area + "') | list | to_json }}";
        JSONObject payload = new JSONObject().put("template", template);
        String body = request(baseUrl + "/api/template", "POST", token, payload);
        JSONArray areaIds = new JSONArray(body);
        Set<String> areaSet = strings(areaIds);
        Set<String> exposedSet = discovery.allowedEntityIds(baseUrl, token);
        JSONArray states = new JSONArray(request(baseUrl + "/api/states", "GET", token, null));
        List<HomeAssistantEntity> result = new ArrayList<>();
        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.optJSONObject(i);
            if (state == null) continue;
            String id = state.optString("entity_id", "");
            if (!areaSet.contains(id)) continue;
            JSONObject attrs = state.optJSONObject("attributes");
            result.add(new HomeAssistantEntity(id,
                    attrs == null ? id : attrs.optString("friendly_name", id),
                    exposedSet.contains(id),
                    false,
                    false,
                    state.optString("state", "unknown")));
        }
        return result;
    }

    private int service(String baseUrl, String token, String service, String entityId) throws Exception {
        HttpURLConnection c = open(baseUrl + "/api/services/" + entityId.substring(0, entityId.indexOf('.')) + "/" + service, "POST", token);
        try {
            c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = new JSONObject().put("entity_id", entityId).toString().getBytes(StandardCharsets.UTF_8);
            c.setFixedLengthStreamingMode(bytes.length);
            try (OutputStream out = c.getOutputStream()) { out.write(bytes); }
            return c.getResponseCode();
        } finally { c.disconnect(); }
    }

    private String request(String url, String method, String token, JSONObject payload) throws Exception {
        HttpURLConnection c = open(url, method, token);
        try {
            if (payload != null) {
                c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
                c.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream out = c.getOutputStream()) { out.write(bytes); }
            }
            int status = c.getResponseCode();
            if (status == 401 || status == 403) throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            if (status < 200 || status >= 300) throw new IOException("Home Assistant HTTP " + status);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder out = new StringBuilder(); String line;
                while ((line = reader.readLine()) != null) out.append(line);
                return out.toString();
            }
        } finally { c.disconnect(); }
    }

    private static Set<String> strings(JSONArray values) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < values.length(); i++) result.add(values.optString(i, ""));
        return result;
    }

    private static HttpURLConnection open(String url, String method, String token) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(TIMEOUT_MS); c.setReadTimeout(TIMEOUT_MS);
        c.setRequestProperty("Authorization", "Bearer " + token); c.setRequestProperty("Accept", "application/json");
        return c;
    }
}
