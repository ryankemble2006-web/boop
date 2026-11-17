package com.boop.alpha1;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public final class BoopPreviewClient {
    private BoopPreviewClient() { }
    public static String send(String host, int port, String pin, String command) throws IOException {
        if (!BoopPreviewProtocol.validPin(pin) || !BoopPreviewProtocol.validCommand(command))
            throw new IllegalArgumentException("Choose a preview and enter the six-digit phone code");
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            socket.setSoTimeout(2500);
            socket.getOutputStream().write((pin + "\t" + command + "\n").getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            StringBuilder reply = new StringBuilder();
            for (int i = 0; i < 96; i++) {
                int c = socket.getInputStream().read();
                if (c == '\n') return reply.toString();
                if (c < 0) throw new IOException("Phone closed the test session");
                if (c < 32 || c > 126) throw new IOException("Invalid phone response");
                reply.append((char)c);
            }
            throw new IOException("Phone response too long");
        }
    }
}
