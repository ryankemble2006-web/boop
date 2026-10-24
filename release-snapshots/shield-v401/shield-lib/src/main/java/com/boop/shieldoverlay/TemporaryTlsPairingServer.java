package com.boop.shieldoverlay;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TemporaryTlsPairingServer implements AutoCloseable {
    private static final int MAX_REQUEST_LINE_BYTES = 8192;
    private static final String DIRECT_HTTP_PIN = "0000000000000000000000000000000000000000000000000000000000000000";

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
    private ServerSocket serverSocket;
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

            serverSocket = new ServerSocket(0, 16, bindAddress);
            closed = false;

            final String hostAddress = bindAddress.getHostAddress();
            final int localPort = serverSocket.getLocalPort();
            Endpoint endpoint = new Endpoint(
                    hostAddress,
                    localPort,
                    DIRECT_HTTP_PIN);

            serverThread = new Thread(
                    () -> acceptLoop(hostAddress, localPort, session, listener),
                    "boop-pairing-http");
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
        ServerSocket socketToClose;
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

    private void acceptLoop(
            String hostAddress,
            int localPort,
            PairingSession session,
            Listener listener) {
        final String clientId = "http://" + PairingQrPayload.formatUrlHost(hostAddress) + ":" + localPort + "/";
        final String redirectUri = clientId + "auth_callback";

        try {
            while (!closed) {
                ServerSocket listenerSocket;
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

                try (Socket client = listenerSocket.accept()) {
                    long clientRemainingMs = session.expiresAtMs() - System.currentTimeMillis();
                    if (clientRemainingMs <= 0) {
                        writeHtml(client, "410 Gone", "Pairing Link Expired",
                                "This QR code has expired. Tap Try Again on your Shield TV to generate a fresh code.");
                        close();
                        return;
                    }
                    client.setSoTimeout((int) Math.min(10_000L, Math.max(1_000L, clientRemainingMs)));
                    PairingRelayMessage accepted = handleHttpClient(
                            client,
                            clientId,
                            redirectUri,
                            session);
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
                    // A failed or aborted browser preconnect does not consume the session.
                }
            }
        } catch (IOException listenerFailure) {
            close();
        }
    }

    private PairingRelayMessage handleHttpClient(
            Socket client,
            String clientId,
            String redirectUri,
            PairingSession session) throws IOException {
        InputStream input = client.getInputStream();
        String requestLine;
        try {
            requestLine = readRequestLine(input);
        } catch (IllegalArgumentException tooLarge) {
            writeHtml(client, "400 Bad Request", "Invalid Request", "The pairing request was invalid.");
            return null;
        }
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }
        drainHeaders(input);

        int firstSpace = requestLine.indexOf(' ');
        int secondSpace = firstSpace > 0 ? requestLine.indexOf(' ', firstSpace + 1) : -1;
        if (firstSpace <= 0 || secondSpace <= firstSpace + 1) {
            writeHtml(client, "400 Bad Request", "Invalid Request", "Malformed HTTP request line.");
            return null;
        }

        String method = requestLine.substring(0, firstSpace).trim();
        String target = requestLine.substring(firstSpace + 1, secondSpace).trim();
        if (!("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method))) {
            writeHtml(client, "405 Method Not Allowed", "Method Not Allowed", "Use GET to pair with BOOP Shield.");
            return null;
        }

        String path = target;
        String rawQuery = "";
        int qIndex = target.indexOf('?');
        if (qIndex >= 0) {
            path = target.substring(0, qIndex);
            rawQuery = target.substring(qIndex + 1);
        }

        if ("/favicon.ico".equals(path)) {
            writeEmpty(client, "204 No Content", null);
            return null;
        }

        // IndieAuth client_id metadata endpoint
        if ("/".equals(path) || path.isEmpty()) {
            writeIndieAuthClientPage(client, redirectUri, "HEAD".equalsIgnoreCase(method));
            return null;
        }

        Map<String, String> query = parseQuery(rawQuery);

        // Step 1: Phone camera scans QR code -> GET /pair?v=1&sid=...&secret=...&ha=...
        if ("/pair".equals(path)) {
            String sid = query.get("sid");
            String secret = query.get("secret");
            String haBase = query.get("ha");
            if (sid == null
                    || !session.sessionId().equals(sid)
                    || !session.matchesSecret(secret, System.currentTimeMillis())
                    || !isUsableHttpUrl(haBase)) {
                writeHtml(client, "400 Bad Request", "Invalid or Expired QR Code",
                        "Please scan the current QR code shown on your NVIDIA Shield TV.");
                return null;
            }

            String cleanHa = trimTrailingSlash(haBase.trim());
            String authorizeUrl = cleanHa + "/auth/authorize"
                    + "?response_type=code"
                    + "&client_id=" + encode(clientId)
                    + "&redirect_uri=" + encode(redirectUri)
                    + "&state=" + encode(secret);
            writeEmpty(client, "302 Found", authorizeUrl);
            return null;
        }

        // Step 2: Home Assistant redirects back -> GET /auth_callback?code=...&state=...
        if ("/auth_callback".equals(path)) {
            String state = query.get("state");
            String code = query.get("code");
            if (code == null
                    || code.trim().isEmpty()
                    || state == null
                    || !session.consume(state, System.currentTimeMillis())) {
                writeHtml(client, "400 Bad Request", "Approval Not Completed",
                        "Home Assistant did not return a valid authorization code. Try scanning the Shield QR code again.");
                return null;
            }

            writeHtml(client, "200 OK", "BOOP Shield Connected",
                    "Home Assistant has linked your NVIDIA Shield. You can close this browser tab and look at your TV.");
            return new PairingRelayMessage(code.trim(), clientId);
        }

        writeHtml(client, "404 Not Found", "Not Found", "Unknown BOOP Shield pairing path.");
        return null;
    }

    private static String readRequestLine(InputStream input) throws IOException {
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
            if (bytes.size() >= MAX_REQUEST_LINE_BYTES) {
                throw new IllegalArgumentException("HTTP request line too large");
            }
            bytes.write(value);
        }
    }

    private static void drainHeaders(InputStream input) throws IOException {
        int consecutiveNewlines = 0;
        int totalRead = 0;
        while (totalRead < 16384) {
            int b = input.read();
            if (b == -1) {
                return;
            }
            totalRead++;
            if (b == '\r') {
                continue;
            }
            if (b == '\n') {
                consecutiveNewlines++;
                if (consecutiveNewlines >= 2) {
                    return;
                }
            } else {
                consecutiveNewlines = 0;
            }
        }
    }

    private static void writeEmpty(Socket socket, String status, String location) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        writer.write("HTTP/1.1 " + status + "\r\n");
        if (location != null) {
            writer.write("Location: " + location + "\r\n");
        }
        writer.write("Cache-Control: no-store\r\n");
        writer.write("Connection: close\r\n");
        writer.write("Content-Length: 0\r\n\r\n");
        writer.flush();
    }

    private static void writeIndieAuthClientPage(Socket socket, String redirectUri, boolean headOnly)
            throws IOException {
        String html = "<!DOCTYPE html><html><head><meta charset=\"utf-8\">"
                + "<title>BOOP Shield</title>"
                + "<link rel=\"redirect_uri\" href=\"" + escapeHtml(redirectUri) + "\">"
                + "</head><body style=\"background:#000;color:#fff;font-family:sans-serif;text-align:center;padding:48px;\">"
                + "<h1>BOOP Shield</h1><p>Home Assistant Local Pairing Service</p></body></html>";
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        OutputStream out = socket.getOutputStream();
        String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Cache-Control: no-store\r\n"
                + "Connection: close\r\n"
                + "Content-Length: " + body.length + "\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        if (!headOnly) {
            out.write(body);
        }
        out.flush();
    }

    private static void writeHtml(Socket socket, String status, String title, String detail)
            throws IOException {
        String html = "<!DOCTYPE html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>" + escapeHtml(title) + "</title></head>"
                + "<body style=\"margin:0;background:#000000;color:#FFFFFF;font-family:system-ui,-apple-system,sans-serif;"
                + "display:flex;align-items:center;justify-content:center;min-height:100vh;padding:24px;box-sizing:border-box;\">"
                + "<div style=\"max-width:420px;background:#15181E;border:1px solid #2A2F39;border-radius:18px;padding:32px;text-align:center;\">"
                + "<h1 style=\"margin:0 0 12px 0;font-size:24px;font-weight:600;color:#4DB8FF;\">" + escapeHtml(title) + "</h1>"
                + "<p style=\"margin:0;font-size:16px;line-height:1.5;color:#B8C0CC;\">" + escapeHtml(detail) + "</p>"
                + "</div></body></html>";
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        OutputStream out = socket.getOutputStream();
        String headers = "HTTP/1.1 " + status + "\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Cache-Control: no-store\r\n"
                + "Connection: close\r\n"
                + "Content-Length: " + body.length + "\r\n\r\n";
        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(body);
        out.flush();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> out = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return out;
        }
        for (String part : rawQuery.split("&")) {
            int eq = part.indexOf('=');
            if (eq > 0) {
                String k = decode(part.substring(0, eq));
                String v = decode(part.substring(eq + 1));
                out.put(k, v);
            }
        }
        return out;
    }

    private static boolean isUsableHttpUrl(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String trimTrailingSlash(String url) {
        String out = url;
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 unavailable", impossible);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 unavailable", impossible);
        }
    }
}
