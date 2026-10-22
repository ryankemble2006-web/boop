package com.boop.alpha1;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class PinnedTlsPairingClient {
    private static final int TIMEOUT_MS = 8000;

    public boolean send(PairingLink link, String authorizationCode, String clientId)
            throws IOException, GeneralSecurityException {
        if (link == null || isEmpty(authorizationCode) || isEmpty(clientId)) {
            throw new IllegalArgumentException("Pairing handoff details are required");
        }

        byte[] expectedPin = decodeHex(link.certificatePinSha256());
        X509TrustManager pinningTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                throw new CertificateException("Client certificates are not accepted");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                if (chain == null || chain.length == 0) {
                    throw new CertificateException("Shield certificate missing");
                }
                try {
                    byte[] actualPin = MessageDigest.getInstance("SHA-256")
                            .digest(chain[0].getEncoded());
                    if (!MessageDigest.isEqual(expectedPin, actualPin)) {
                        throw new CertificateException("Shield certificate did not match the QR code");
                    }
                } catch (GeneralSecurityException e) {
                    throw new CertificateException("Could not verify Shield certificate", e);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{pinningTrustManager}, null);

        try (SSLSocket socket = (SSLSocket) context.getSocketFactory()
                .createSocket(link.host(), link.port())) {
            socket.setSoTimeout(TIMEOUT_MS);
            socket.startHandshake();

            JSONObject json = new JSONObject();
            try {
                json.put("session_id", link.sessionId());
                json.put("secret", link.secret());
                json.put("authorization_code", authorizationCode);
                json.put("client_id", clientId);
            } catch (JSONException e) {
                throw new IOException("Could not encode pairing handoff", e);
            }

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(json.toString());
            writer.write("\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String line = reader.readLine();
            if (line == null || line.length() > 1024) {
                return false;
            }
            try {
                JSONObject response = new JSONObject(line);
                return "accepted".equals(response.optString("status"));
            } catch (JSONException e) {
                throw new IOException("Could not decode Shield pairing response", e);
            }
        }
    }

    private static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static byte[] decodeHex(String value) {
        if (value == null || value.length() != 64) {
            throw new IllegalArgumentException("Invalid Shield certificate pin");
        }
        byte[] out = new byte[value.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int high = Character.digit(value.charAt(i * 2), 16);
            int low = Character.digit(value.charAt(i * 2 + 1), 16);
            if (high < 0 || low < 0) {
                throw new IllegalArgumentException("Invalid Shield certificate pin");
            }
            out[i] = (byte) ((high << 4) | low);
        }
        return out;
    }
}
