package com.boop.alpha1;

import android.net.Uri;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public final class HaLoopbackAuthServer implements AutoCloseable {
    private static final int TIMEOUT_MS = 120_000;
    private static final int MAX_REQUEST_LINE = 4096;

    public interface Listener {
        void onAuthorizationCode(String code);
        void onFailure(String message);
    }

    public static final class Endpoint {
        private final String clientId;
        private final String redirectUri;
        private final String state;

        private Endpoint(String clientId, String redirectUri, String state) {
            this.clientId = clientId;
            this.redirectUri = redirectUri;
            this.state = state;
        }

        public String clientId() { return clientId; }
        public String redirectUri() { return redirectUri; }
        public String state() { return state; }
    }

    private final Object lock = new Object();
    private ServerSocket serverSocket;
    private Thread thread;
    private volatile boolean closed = true;

    public Endpoint start(String sessionId, Listener listener) throws IOException {
        if (sessionId == null || sessionId.trim().isEmpty() || listener == null) {
            throw new IllegalArgumentException("Pairing callback details are required");
        }

        synchronized (lock) {
            if (!closed || serverSocket != null) {
                throw new IllegalStateException("Callback server already started");
            }
            serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
            serverSocket.setSoTimeout(TIMEOUT_MS);
            closed = false;
        }

        String base = "http://127.0.0.1:" + serverSocket.getLocalPort() + "/";
        String state = randomUrlSafe(24);
        Endpoint endpoint = new Endpoint(base, base + "auth_callback", state);

        thread = new Thread(() -> acceptOnce(sessionId, endpoint, listener), "boop-ha-loopback");
        thread.setDaemon(true);
        thread.start();
        return endpoint;
    }

    @Override
    public void close() {
        ServerSocket socket;
        synchronized (lock) {
            if (closed && serverSocket == null) {
                return;
            }
            closed = true;
            socket = serverSocket;
            serverSocket = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Closing an already-finished temporary callback listener is harmless.
            }
        }
    }

    private void acceptOnce(String sessionId, Endpoint endpoint, Listener listener) {
        try {
            ServerSocket listenerSocket;
            synchronized (lock) {
                listenerSocket = serverSocket;
            }
            if (listenerSocket == null) {
                return;
            }

            try (Socket socket = listenerSocket.accept()) {
                socket.setSoTimeout(5000);
                String requestLine = readRequestLine(socket);
                Uri callback = parseCallbackTarget(requestLine);
                String state = callback.getQueryParameter("state");
                String code = callback.getQueryParameter("code");
                if (!"/auth_callback".equals(callback.getPath())
                        || state == null
                        || !endpoint.state().equals(state)
                        || code == null
                        || code.trim().isEmpty()) {
                    writeResponse(socket, "400 Bad Request", null);
                    listener.onFailure("Home Assistant did not return a valid approval.");
                    return;
                }

                String location = "boop://shield-pair-return?sid="
                        + Uri.encode(sessionId);
                writeResponse(socket, "302 Found", location);
                listener.onAuthorizationCode(code);
            }
        } catch (SocketTimeoutException expired) {
            listener.onFailure("That pairing request went stale.");
        } catch (IOException | IllegalArgumentException e) {
            if (!closed) {
                listener.onFailure("The Home Assistant approval did not come back cleanly.");
            }
        } finally {
            close();
        }
    }

    private static Uri parseCallbackTarget(String requestLine) {
        if (requestLine == null || !requestLine.startsWith("GET ")) {
            throw new IllegalArgumentException("Invalid callback request");
        }
        int secondSpace = requestLine.indexOf(' ', 4);
        if (secondSpace <= 4) {
            throw new IllegalArgumentException("Invalid callback request");
        }
        String target = requestLine.substring(4, secondSpace);
        return Uri.parse("http://127.0.0.1" + target);
    }

    private static String readRequestLine(Socket socket) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String line = reader.readLine();
        if (line != null && line.length() > MAX_REQUEST_LINE) {
            throw new IllegalArgumentException("Callback request too large");
        }
        return line;
    }

    private static void writeResponse(Socket socket, String status, String location) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 " + status + "\r\n");
        if (location != null) {
            writer.write("Location: " + location + "\r\n");
        }
        writer.write("Connection: close\r\n");
        writer.write("Content-Length: 0\r\n\r\n");
        writer.flush();
    }

    private static String randomUrlSafe(int bytes) {
        byte[] value = new byte[bytes];
        new SecureRandom().nextBytes(value);
        return Base64.encodeToString(value, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
    }
}
