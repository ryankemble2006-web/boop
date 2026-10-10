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
        if (command == null) return null;
        try {
            if (!command.explicitOtherRoom()) {
                String areaId = discoverAreaId(baseUrl, token, command.target());
                if (!areaId.isEmpty()) {
                    Set<String> exposed = discovery.allowedEntityIds(baseUrl, token);
                    List<HomeAssistantEntity> allEntities = discoverAll(baseUrl, token, exposed);
                    List<String> areaEntityIds = discoverAreaEntityIds(baseUrl, token, areaId);
                    NamedHomeTargetResolver.Result named = NamedHomeTargetResolver.resolve(
                            command.target(), areaId, areaEntityIds, exposed, allEntities);
                    return performNamed(baseUrl, token, command, named);
                }

                // Named global targets are intentionally exact and limited to multiword names.
                // Ordinary device names remain scoped to the selected room below.
                if (command.target().trim().contains(" ")) {
                    Set<String> exposed = discovery.allowedEntityIds(baseUrl, token);
                    List<HomeAssistantEntity> allEntities = discoverAll(baseUrl, token, exposed);
                    NamedHomeTargetResolver.Result named = NamedHomeTargetResolver.resolve(
                            command.target(), "", Collections.emptyList(), exposed, allEntities);
                    if (named.kind() != NamedHomeTargetResolver.Kind.NONE) {
                        return performNamed(baseUrl, token, command, named);
                    }
                }
            }

            if (command.explicitOtherRoom() || command.groupTarget()) return null;
            HomeAssistantEntityResolver.Result resolution = HomeAssistantEntityResolver.resolveResult(
                    command, discover(baseUrl, token, room));
            if (resolution.kind() == HomeAssistantEntityResolver.Kind.AMBIGUOUS) {
                return CommandOutcome.ambiguousTarget();
            }
            HomeAssistantEntity entity = resolution.entity();
            if (entity == null) return null;
            int status = service(baseUrl, token,
                    entity.entityId().substring(0, entity.entityId().indexOf('.')),
                    command.service(), Collections.singletonList(entity.entityId()));
            if (status == 401 || status == 403) throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            return status >= 200 && status < 300 ? CommandOutcome.success(entity.name()) : null;
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            throw e;
        } catch (Exception ignored) {
            // Discovery is an optimisation. Assist remains authoritative when evidence is absent.
            return null;
        }
    }

    private CommandOutcome performNamed(String baseUrl, String token, GenericHomeCommand command,
            NamedHomeTargetResolver.Result named) throws Exception {
        if (named.kind() == NamedHomeTargetResolver.Kind.AMBIGUOUS) {
            return CommandOutcome.ambiguousTarget();
        }
        if (named.kind() == NamedHomeTargetResolver.Kind.NONE) return null;
        if (named.entityIds().isEmpty()) return CommandOutcome.noTarget();
        int status = service(baseUrl, token, named.serviceDomain(), command.service(), named.entityIds());
        if (status == 401 || status == 403) {
            throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
        }
        return status >= 200 && status < 300
                ? CommandOutcome.success(command.target()) : null;
    }

    private String discoverAreaId(String baseUrl, String token, String areaName) throws Exception {
        String escaped = escapeTemplateString(areaName);
        try {
            String body = request(baseUrl + "/api/template", "POST", token,
                    new JSONObject().put("template", "{{ area_id('" + escaped
                            + "') | default('', true) | to_json }}"));
            Object value = new org.json.JSONTokener(body).nextValue();
            return value instanceof String ? ((String) value).trim() : "";
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            throw e;
        } catch (Exception ignored) {
            // An ordinary entity name is not a valid area; continue with exact group lookup.
            return "";
        }
    }

    private List<String> discoverAreaEntityIds(String baseUrl, String token, String areaId)
            throws Exception {
        String body = request(baseUrl + "/api/template", "POST", token,
                new JSONObject().put("template", "{{ area_entities('"
                        + escapeTemplateString(areaId) + "') | list | to_json }}"));
        return new ArrayList<>(strings(new JSONArray(body)));
    }

    private List<HomeAssistantEntity> discoverAll(String baseUrl, String token,
            Set<String> exposedIds) throws Exception {
        JSONArray states = new JSONArray(request(baseUrl + "/api/states", "GET", token, null));
        List<HomeAssistantEntity> result = new ArrayList<>();
        for (int i = 0; i < states.length(); i++) {
            JSONObject state = states.optJSONObject(i);
            if (state == null) continue;
            String id = state.optString("entity_id", "");
            JSONObject attrs = state.optJSONObject("attributes");
            result.add(new HomeAssistantEntity(id,
                    attrs == null ? id : attrs.optString("friendly_name", id),
                    exposedIds.contains(id), false, false, state.optString("state", "unknown")));
        }
        return result;
    }

    private static String escapeTemplateString(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
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

    private int service(String baseUrl, String token, String domain, String service,
            List<String> entityIds) throws Exception {
        HttpURLConnection c = open(baseUrl + "/api/services/" + domain + "/" + service, "POST", token);
        try {
            c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            Object entityValue = entityIds.size() == 1 ? entityIds.get(0) : new JSONArray(entityIds);
            byte[] bytes = new JSONObject().put("entity_id", entityValue).toString().getBytes(StandardCharsets.UTF_8);
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
