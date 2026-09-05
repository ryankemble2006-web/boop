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

final class HomeAssistantLightColourClient {
    private static final int TIMEOUT_MS = 5000;

    private final String homeArea;

    HomeAssistantLightColourClient(String homeArea) {
        this.homeArea = homeArea;
    }

    CommandOutcome setColour(String baseUrl, String accessToken, String colour) throws Exception {
        JSONArray areaEntities = fetchAreaEntities(baseUrl, accessToken);
        JSONArray states = fetchStates(baseUrl, accessToken);
        List<String> entityIds = HomeAssistantLightColourSelector.select(areaEntities, states);
        if (entityIds.isEmpty()) {
            return CommandOutcome.noTarget();
        }

        int status = callService(baseUrl, accessToken, entityIds, colour);
        checkAuthorized(status);
        if (status < 200 || status >= 300) {
            return CommandOutcome.failed();
        }
        return CommandOutcome.success("lights");
    }

    private JSONArray fetchAreaEntities(String baseUrl, String accessToken) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(baseUrl + "/api/template", "POST", accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String escapedArea = homeArea
                    .replace("\\", "\\\\")
                    .replace("'", "\\'");
            String template = "{{ area_entities('" + escapedArea + "') | list | to_json }}";
            byte[] payload = new JSONObject()
                    .put("template", template)
                    .toString()
                    .getBytes(StandardCharsets.UTF_8);
            writePayload(connection, payload);

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

    private int callService(
            String baseUrl,
            String accessToken,
            List<String> entityIds,
            String colour) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(
                    baseUrl + "/api/services/light/turn_on",
                    "POST",
                    accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] payload = HomeAssistantLightColourProtocol.serviceBody(entityIds, colour)
                    .toString()
                    .getBytes(StandardCharsets.UTF_8);
            writePayload(connection, payload);
            return connection.getResponseCode();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void writePayload(HttpURLConnection connection, byte[] payload)
            throws IOException {
        connection.setFixedLengthStreamingMode(payload.length);
        try (OutputStream out = connection.getOutputStream()) {
            out.write(payload);
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
}
