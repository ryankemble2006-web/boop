package com.boop.alpha1;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Activity-owned server. One bounded request per connection, no persistence or background service. */
public final class BoopPreviewServer implements AutoCloseable {
    public interface Delivery { boolean deliver(String command); }
    private final String pin;
    private final Delivery delivery;
    private final ServerSocket listener;
    private volatile boolean running = true;
    private volatile Socket active;

    public BoopPreviewServer(String pin, Delivery delivery) throws IOException {
        if (!BoopPreviewProtocol.validPin(pin) || delivery == null) throw new IllegalArgumentException("Invalid session");
        this.pin = pin;
        this.delivery = delivery;
        listener = new ServerSocket(0, 4);
        Thread worker = new Thread(this::serve, "boop-preview-server");
        worker.setDaemon(true);
        worker.start();
    }
    public int port() { return listener.getLocalPort(); }
    public boolean isRunning() { return running; }
    private void serve() {
        while (running) {
            try (Socket socket = listener.accept()) {
                active = socket;
                if (!running) break;
                socket.setSoTimeout(1200);
                String request = readLine(socket.getInputStream());
                String response = respond(request);
                socket.getOutputStream().write((response + "\n").getBytes(StandardCharsets.US_ASCII));
                socket.getOutputStream().flush();
            } catch (IOException ignored) {
                // A disconnected/slow sender cannot terminate the session or replay its request.
            } finally { active = null; }
        }
    }
    private String respond(String request) {
        if (request == null) return "ERROR request";
        String[] fields = request.split("\t", -1);
        if (fields.length != 2) return "ERROR request";
        if (!pin.equals(fields[0])) return "ERROR auth";
        String command = fields[1];
        if (!BoopPreviewProtocol.validCommand(command)) return "ERROR command";
        if (!running) return "ERROR inactive";
        if ("PING".equals(command)) return "OK PING";
        try { return delivery.deliver(command) ? "OK " + command : "ERROR inactive"; }
        catch (RuntimeException failure) { return "ERROR preview"; }
    }
    private static String readLine(InputStream in) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            int c = in.read();
            if (c == '\n') return line.toString();
            if (c < 0 || (c != '\t' && (c < 32 || c > 126))) return null;
            line.append((char)c);
        }
        return null;
    }
    @Override public void close() {
        running = false;
        try { listener.close(); } catch (IOException ignored) { }
        Socket socket = active;
        if (socket != null) try { socket.close(); } catch (IOException ignored) { }
    }
}
