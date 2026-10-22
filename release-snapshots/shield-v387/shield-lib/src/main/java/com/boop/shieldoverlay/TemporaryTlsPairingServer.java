package com.boop.shieldoverlay;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import okhttp3.tls.HeldCertificate;
import org.json.JSONException;
import org.json.JSONObject;

public final class TemporaryTlsPairingServer implements AutoCloseable {
    private static final int MAX_MESSAGE_BYTES = 8192;

    public interface Listener {
        void onAccepted(PairingRelayMessage message);
    }

    public static final class Endpoint {
        private final String host;
        private final int port;
        private final String certificatePinSha256;

        private Endpoint(String host, int port, String certificatePinSha256) {
            this.host = host;
            this.port = port;
            this.certificatePinSha256 = certificatePinSha256;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        public String certificatePinSha256() {
            return certificatePinSha256;
        }
    }

    private final Object lock = new Object();
    private SSLServerSocket serverSocket;
    private Thread serverThread;
    private volatile boolean closed = true;

    public Endpoint start(InetAddress bindAddress, PairingSession session, Listener listener)
            throws IOException, GeneralSecurityException {
        if (bindAddress == null || session == null || listener == null) {
            throw new IllegalArgumentException("pairing server arguments are required");
        }
        long nowMs = System.currentTimeMillis();
        if (!session.isActive(nowMs)) {
            throw new IllegalStateException("pairing session is not active");
        }

        synchronized (lock) {
            if (!closed || serverSocket != null) {
                throw new IllegalStateException("pairing server already started");
            }

            HeldCertificate heldCertificate = new HeldCertificate.Builder()
                    .commonName("BOOP Shield Pairing")
                    .build();
            SSLContext context = createServerContext(heldCertificate);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(0, 8, bindAddress);
            closed = false;

            Endpoint endpoint = new Endpoint(
                    bindAddress.getHostAddress(),
                    serverSocket.getLocalPort(),
                    sha256Hex(heldCertificate.certificate().getEncoded()));

            serverThread = new Thread(
                    () -> acceptLoop(session, listener),
                    "boop-pairing-tls");
            serverThread.setDaemon(true);
            serverThread.start();
            return endpoint;
        }
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        SSLServerSocket socketToClose;
        synchronized (lock) {
            if (closed && serverSocket == null) {
                return;
            }
            closed = true;
            socketToClose = serverSocket;
            serverSocket = null;
        }
        if (socketToClose != null) {
            try {
                socketToClose.close();
            } catch (IOException ignored) {
                // Closing an already-torn-down temporary listener is harmless.
            }
        }
    }

    private void acceptLoop(PairingSession session, Listener listener) {
        try {
            while (!closed) {
                SSLServerSocket listenerSocket;
                synchronized (lock) {
                    listenerSocket = serverSocket;
                }
                if (listenerSocket == null) {
                    return;
                }

                long remainingMs = session.expiresAtMs() - System.currentTimeMillis();
                if (remainingMs <= 0 || !session.isActive(System.currentTimeMillis())) {
                    close();
                    return;
                }
                listenerSocket.setSoTimeout((int) Math.min(Integer.MAX_VALUE, Math.max(1L, remainingMs)));

                try (SSLSocket client = (SSLSocket) listenerSocket.accept()) {
                    long clientRemainingMs = session.expiresAtMs() - System.currentTimeMillis();
                    if (clientRemainingMs <= 0) {
                        writeResponse(client, "rejected");
                        close();
                        return;
                    }
                    client.setSoTimeout((int) Math.min(Integer.MAX_VALUE, Math.max(1L, clientRemainingMs)));
                    client.setUseClientMode(false);
                    client.startHandshake();
                    PairingRelayMessage accepted = handleClient(client, session);
                    if (accepted != null) {
                        close();
                        listener.onAccepted(accepted);
                        return;
                    }
                } catch (SocketTimeoutException expired) {
                    close();
                    return;
                } catch (IOException clientFailure) {
                    if (closed) {
                        return;
                    }
                    // A failed client connection does not consume the one-time session.
                }
            }
        } catch (IOException listenerFailure) {
            close();
        }
    }

    private PairingRelayMessage handleClient(SSLSocket client, PairingSession session) throws IOException {
        String line;
        try {
            line = readLimitedLine(client.getInputStream());
        } catch (IllegalArgumentException invalidMessage) {
            writeResponse(client, "rejected");
            return null;
        }
        if (line == null) {
            writeResponse(client, "rejected");
            return null;
        }

        try {
            JSONObject json = new JSONObject(line);
            String sessionId = requiredString(json, "session_id");
            String candidateSecret = requiredString(json, "secret");
            PairingRelayMessage relay = new PairingRelayMessage(
                    requiredString(json, "authorization_code"),
                    requiredString(json, "client_id"));

            if (!session.sessionId().equals(sessionId)) {
                writeResponse(client, "rejected");
                return null;
            }
            if (!session.consume(candidateSecret, System.currentTimeMillis())) {
                writeResponse(client, "rejected");
                return null;
            }

            writeResponse(client, "accepted");
            return relay;
        } catch (JSONException | IllegalArgumentException invalidMessage) {
            writeResponse(client, "rejected");
            return null;
        }
    }

    private static SSLContext createServerContext(HeldCertificate heldCertificate)
            throws GeneralSecurityException, IOException {
        char[] keyPassword = "boop-pairing".toCharArray();
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);
            keyStore.setKeyEntry(
                    "pairing",
                    heldCertificate.keyPair().getPrivate(),
                    keyPassword,
                    new Certificate[]{heldCertificate.certificate()});

            KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagers.init(keyStore, keyPassword);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers.getKeyManagers(), null, new SecureRandom());
            return context;
        } finally {
            Arrays.fill(keyPassword, '\0');
        }
    }

    private static String requiredString(JSONObject json, String key) throws JSONException {
        if (!json.has(key) || json.isNull(key)) {
            throw new IllegalArgumentException("missing pairing field");
        }
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("empty pairing field");
        }
        return value;
    }

    private static String readLimitedLine(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        while (true) {
            int value = input.read();
            if (value == -1) {
                return bytes.size() == 0 ? null : new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            }
            if (value == '\n') {
                return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
            }
            if (value == '\r') {
                continue;
            }
            if (bytes.size() >= MAX_MESSAGE_BYTES) {
                throw new IllegalArgumentException("pairing message too large");
            }
            bytes.write(value);
        }
    }

    private static void writeResponse(SSLSocket socket, String status) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("{\"status\":\"" + status + "\"}\n");
        writer.flush();
    }

    private static String sha256Hex(byte[] bytes) throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder out = new StringBuilder(digest.length * 2);
        for (byte value : digest) {
            out.append(String.format("%02x", value & 0xff));
        }
        return out.toString();
    }
}
