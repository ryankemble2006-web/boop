package com.boop.shieldhome;

import java.io.IOException;

/** Wait for Seren startup; never dispatch a second Trakt mutation. */
final class SerenTraktSync {
    interface Port {
        boolean ready() throws Exception;
        boolean launchKodi() throws Exception;
        void returnToLauncher();
        void execute(String requestId) throws Exception;
        String status(String requestId) throws Exception;
        long nowMs();
        void sleep(long ms) throws Exception;
        boolean cancelled();
    }

    static String run(Port port, String requestId) throws Exception {
        if (port.cancelled()) throw new InterruptedException();
        if (!port.ready()) {
            if (!port.launchKodi()) throw new IOException("Kodi is not installed");
            try {
                long deadline = port.nowMs() + 30000;
                while (!port.ready()) {
                    if (port.cancelled()) throw new InterruptedException();
                    if (port.nowMs() >= deadline) throw new IOException("Kodi did not become ready");
                    port.sleep(250);
                }
            } finally { port.returnToLauncher(); }
        }
        if (port.cancelled()) throw new InterruptedException();
        long earliest = port.nowMs() + 5000;
        long deadline = port.nowMs() + 180000;
        while (!port.cancelled()) {
            String status = port.status(requestId);
            if (port.nowMs() >= earliest && ("done".equals(status) || "unchanged".equals(status) || "no_auth".equals(status) || "failed".equals(status))) return status;
            if (port.nowMs() >= deadline) throw new IOException("Trakt sync timed out; saved episodes kept");
            port.sleep(500);
        }
        throw new InterruptedException();
    }
}
