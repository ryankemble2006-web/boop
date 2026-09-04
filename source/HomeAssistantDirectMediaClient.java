package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

final class HomeAssistantDirectMediaClient {
    private static final int TIMEOUT_MS = 5000;

    private final String homeArea;

    HomeAssistantDirectMediaClient(String homeArea) {
        this.homeArea = homeArea;
    }

    CommandOutcome processIfMedia(String baseUrl, String accessToken, String text)
            throws Exception {
        MediaCommand command = MediaCommandParser.parse(text);
        if (command == null) {
            return null;
        }

        JSONArray areaEntities = fetchAreaEntities(baseUrl, accessToken);
        JSONArray states = fetchStates(baseUrl, accessToken);
        List<HomeAssistantMediaSelector.Candidate> candidates =
                HomeAssistantMediaSelector.rank(areaEntities, states);

        if (candidates.isEmpty()) {
            return null;
        }

        // Two equally strong candidates means BOOP does not have enough evidence to guess.
        if (candidates.size() > 1
                && candidates.get(0).score() == candidates.get(1).score()) {
            return null;
        }

        for (HomeAssistantMediaSelector.Candidate candidate : candidates) {
            ServiceResult result = callService(
                    baseUrl,
                    accessToken,
                    command.service(),
                    candidate.entityId());
            if (result == ServiceResult.SUCCESS) {
                return CommandOutcome.success(candidate.name());
            }
            if (result == ServiceResult.AUTH_REJECTED) {
                throw new HomeAssistantAuth.AuthRejectedException(
                        "Home Assistant authorization expired");
            }
        }

        // If the direct path cannot safely complete the command, preserve the old Assist path.
        return null;
    }

    private JSONArray fetchAreaEntities(String baseUrl, String accessToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(baseUrl + "/api/template", "POST", accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String escapedArea = homeArea
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");
            String template = "{{ area_entities(\"" + escapedArea + "\") | list | to_json }}";
            byte[] payload = new JSONObject()
                    .put("template", template)
                    .toString()
                    .getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            checkAuthorized(status);
            if (status < 200 || status >= 300) {
                throw new IOException("Home Assistant template HTTP " + status);
            }
            return new JSONArray(readAll(connection.getInputStream()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private JSONArray fetchStates(String baseUrl, String accessToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(baseUrl + "/api/states", "GET", accessToken);
            int status = connection.getResponseCode();
            checkAuthorized(status);
            if (status < 200 || status >= 300) {
                throw new IOException("Home Assistant states HTTP " + status);
            }
            return new JSONArray(readAll(connection.getInputStream()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ServiceResult callService(
            String baseUrl,
            String accessToken,
            String service,
            String entityId) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(
                    baseUrl + "/api/services/media_player/" + service,
                    "POST",
                    accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] payload = new JSONObject()
                    .put("entity_id", entityId)
                    .toString()
                    .getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            if (status == 401 || status == 403) {
                return ServiceResult.AUTH_REJECTED;
            }
            return status >= 200 && status < 300
                    ? ServiceResult.SUCCESS
                    : ServiceResult.NOT_SUPPORTED;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void checkAuthorized(int status)
            throws HomeAssistantAuth.AuthRejectedException {
        if (status == 401 || status == 403) {
            throw new HomeAssistantAuth.AuthRejectedException(
                    "Home Assistant authorization expired");
        }
    }

    private static HttpURLConnection open(String url, String method, String accessToken)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept", "application/json");
        return connection;
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private enum ServiceResult {
        SUCCESS,
        AUTH_REJECTED,
        NOT_SUPPORTED
    }
}
