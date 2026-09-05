package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.Test;

public final class TemporaryTlsPairingServerTest {
    @Test
    public void advertisedPinMatchesPresentedCertificateAndSuccessClosesServer() throws Exception {
        PairingSession session = PairingSession.newSession(System.currentTimeMillis());
        CountDownLatch accepted = new CountDownLatch(1);
        AtomicReference<PairingRelayMessage> received = new AtomicReference<>();
        TemporaryTlsPairingServer server = new TemporaryTlsPairingServer();

        TemporaryTlsPairingServer.Endpoint endpoint = server.start(
                InetAddress.getLoopbackAddress(),
                session,
                message -> {
                    received.set(message);
                    accepted.countDown();
                });

        assertTrue(endpoint.certificatePinSha256().matches("[0-9a-f]{64}"));
        String response = send(endpoint, session.sessionId(), session.secret(), "ha-code", "http://127.0.0.1:43123/");

        assertEquals("{\"status\":\"accepted\"}", response);
        assertTrue(accepted.await(2, TimeUnit.SECONDS));
        assertEquals("ha-code", received.get().authorizationCode());
        assertEquals("http://127.0.0.1:43123/", received.get().clientId());
        assertTrue(server.isClosed());
        assertThrows(IOException.class,
                () -> send(endpoint, session.sessionId(), session.secret(), "again", "http://127.0.0.1:43123/"));
    }

    @Test
    public void wrongSecretIsRejectedWithoutConsumingSession() throws Exception {
        PairingSession session = PairingSession.newSession(System.currentTimeMillis());
        CountDownLatch accepted = new CountDownLatch(1);
        TemporaryTlsPairingServer server = new TemporaryTlsPairingServer();
        TemporaryTlsPairingServer.Endpoint endpoint = server.start(
                InetAddress.getLoopbackAddress(), session, message -> accepted.countDown());

        String rejected = send(endpoint, session.sessionId(), "wrong", "ha-code", "http://127.0.0.1:43123/");
        assertEquals("{\"status\":\"rejected\"}", rejected);
        assertFalse(server.isClosed());
        assertTrue(session.isActive(System.currentTimeMillis()));

        String acceptedResponse = send(endpoint, session.sessionId(), session.secret(), "ha-code", "http://127.0.0.1:43123/");
        assertEquals("{\"status\":\"accepted\"}", acceptedResponse);
        assertTrue(accepted.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void expiredSessionCannotStartListener() {
        PairingSession expired = PairingSession.newSession(System.currentTimeMillis() - 120_000L);
        TemporaryTlsPairingServer server = new TemporaryTlsPairingServer();

        assertThrows(IllegalStateException.class,
                () -> server.start(InetAddress.getLoopbackAddress(), expired, message -> { }));
    }

    @Test
    public void explicitCloseStopsListening() throws Exception {
        PairingSession session = PairingSession.newSession(System.currentTimeMillis());
        TemporaryTlsPairingServer server = new TemporaryTlsPairingServer();
        TemporaryTlsPairingServer.Endpoint endpoint = server.start(
                InetAddress.getLoopbackAddress(), session, message -> { });

        server.close();

        assertTrue(server.isClosed());
        assertThrows(IOException.class,
                () -> send(endpoint, session.sessionId(), session.secret(), "ha-code", "http://127.0.0.1:43123/"));
    }

    private static String send(
            TemporaryTlsPairingServer.Endpoint endpoint,
            String sessionId,
            String secret,
            String authorizationCode,
            String clientId) throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{pinningTrustManager(endpoint.certificatePinSha256())}, null);

        try (SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket(endpoint.host(), endpoint.port())) {
            socket.setSoTimeout(2_000);
            socket.startHandshake();
            Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
            writer.write("{\"session_id\":\"" + sessionId
                    + "\",\"secret\":\"" + secret
                    + "\",\"authorization_code\":\"" + authorizationCode
                    + "\",\"client_id\":\"" + clientId + "\"}\n");
            writer.flush();
            return new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
        }
    }

    private static X509TrustManager pinningTrustManager(String expectedPin) {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) { }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {
                if (chain == null || chain.length == 0) {
                    throw new java.security.cert.CertificateException("server certificate missing");
                }
                try {
                    String actual = hex(MessageDigest.getInstance("SHA-256").digest(chain[0].getEncoded()));
                    if (!MessageDigest.isEqual(
                            expectedPin.getBytes(StandardCharsets.US_ASCII),
                            actual.getBytes(StandardCharsets.US_ASCII))) {
                        throw new java.security.cert.CertificateException("certificate pin mismatch");
                    }
                } catch (java.security.GeneralSecurityException e) {
                    throw new java.security.cert.CertificateException(e);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            out.append(String.format("%02x", value & 0xff));
        }
        return out.toString();
    }
}
