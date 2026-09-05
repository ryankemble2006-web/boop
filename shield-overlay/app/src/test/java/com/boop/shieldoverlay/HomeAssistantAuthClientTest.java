package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class HomeAssistantAuthClientTest {
    private MockWebServer server;
    private HomeAssistantAuthClient client;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new HomeAssistantAuthClient(new OkHttpClient());
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void authorizationCodeExchangeUsesExactClientIdAndReturnsBothTokens() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"access-1\",\"refresh_token\":\"refresh-1\",\"expires_in\":1800,\"token_type\":\"Bearer\"}"));

        String clientId = "http://127.0.0.1:43123/";
        AuthTokenSet tokens = client.exchangeAuthorizationCode(
                server.url("/").toString(), "one-time-code", clientId);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/auth/token", request.getPath());
        assertEquals("application/x-www-form-urlencoded", request.getHeader("Content-Type"));
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("grant_type=authorization_code"));
        assertTrue(body.contains("code=one-time-code"));
        assertTrue(body.contains("client_id=http%3A%2F%2F127.0.0.1%3A43123%2F"));

        assertEquals("access-1", tokens.accessToken());
        assertEquals("refresh-1", tokens.refreshToken());
        assertEquals(1800L, tokens.expiresInSeconds());
        assertEquals("Bearer", tokens.tokenType());
    }

    @Test
    public void refreshUsesSameClientIdAndDoesNotInventANewRefreshToken() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"access_token\":\"access-2\",\"expires_in\":1800,\"token_type\":\"Bearer\"}"));

        String clientId = "http://127.0.0.1:43123/";
        AuthTokenSet tokens = client.refresh(
                server.url("/").toString(), "refresh-1", clientId);

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/auth/token", request.getPath());
        String body = request.getBody().readUtf8();
        assertTrue(body.contains("grant_type=refresh_token"));
        assertTrue(body.contains("refresh_token=refresh-1"));
        assertTrue(body.contains("client_id=http%3A%2F%2F127.0.0.1%3A43123%2F"));

        assertEquals("access-2", tokens.accessToken());
        assertNull(tokens.refreshToken());
    }

    @Test
    public void tokenEndpointFailureDoesNotEchoSensitiveResponseBody() {
        server.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("{\"error_description\":\"super-secret-token\"}"));

        IOException error = assertThrows(IOException.class, () ->
                client.exchangeAuthorizationCode(
                        server.url("/").toString(), "one-time-code", "http://127.0.0.1:43123/"));

        assertFalse(error.getMessage().contains("super-secret-token"));
    }

    @Test
    public void malformedSuccessResponseIsRejected() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        assertThrows(IOException.class, () ->
                client.exchangeAuthorizationCode(
                        server.url("/").toString(), "one-time-code", "http://127.0.0.1:43123/"));
    }
}
