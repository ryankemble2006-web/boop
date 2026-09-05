package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public final class ShieldPairingActivity extends Activity {
    private TextView status;
    private HaLoopbackAuthServer callbackServer;
    private PairingLink pairingLink;
    private volatile boolean relayAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        status = new TextView(this);
        status.setBackgroundColor(Color.BLACK);
        status.setTextColor(Color.WHITE);
        status.setTextSize(30f);
        status.setGravity(Gravity.CENTER);
        status.setPadding(48, 48, 48, 48);
        status.setText("BOOP is ready to pair.");
        setContentView(status);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_VIEW.equals(intent.getAction())) {
            show("Scan the Shield QR code to pair BOOP.");
            return;
        }
        Uri uri = intent.getData();
        if (uri == null || !"boop".equals(uri.getScheme())) {
            show("That pairing link isn't for BOOP.");
            return;
        }

        if ("shield-pair".equals(uri.getHost())) {
            beginPairing(uri);
            return;
        }
        if ("shield-pair-return".equals(uri.getHost())) {
            show(relayAccepted ? "Found it." : "Almost there…");
            return;
        }
        show("That pairing link isn't for BOOP.");
    }

    private void beginPairing(Uri uri) {
        closeCallbackServer();
        relayAccepted = false;

        try {
            pairingLink = PairingLink.parse(uri);
            callbackServer = new HaLoopbackAuthServer();
            HaLoopbackAuthServer.Endpoint endpoint = callbackServer.start(
                    pairingLink.sessionId(),
                    new HaLoopbackAuthServer.Listener() {
                        @Override
                        public void onAuthorizationCode(String code) {
                            relayToShield(code, endpointHolder.clientId);
                        }

                        @Override
                        public void onFailure(String message) {
                            runOnUiThread(() -> show(message));
                        }
                    });
            endpointHolder.clientId = endpoint.clientId();

            String base = pairingLink.homeAssistantBaseUrl();
            while (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            Uri authorize = Uri.parse(base + "/auth/authorize")
                    .buildUpon()
                    .appendQueryParameter("client_id", endpoint.clientId())
                    .appendQueryParameter("redirect_uri", endpoint.redirectUri())
                    .appendQueryParameter("state", endpoint.state())
                    .build();

            show("BOOP is asking Home Assistant…");
            startActivity(new Intent(Intent.ACTION_VIEW, authorize));
        } catch (Exception e) {
            closeCallbackServer();
            show("That pairing request didn't start cleanly. Scan the Shield again.");
        }
    }

    private final EndpointHolder endpointHolder = new EndpointHolder();

    private static final class EndpointHolder {
        volatile String clientId;
    }

    private void relayToShield(String authorizationCode, String clientId) {
        PairingLink link = pairingLink;
        if (link == null || clientId == null) {
            runOnUiThread(() -> show("That pairing request went stale. Scan again."));
            return;
        }

        Thread relay = new Thread(() -> {
            try {
                boolean accepted = new PinnedTlsPairingClient()
                        .send(link, authorizationCode, clientId);
                relayAccepted = accepted;
                runOnUiThread(() -> show(
                        accepted
                                ? "Found it."
                                : "The Shield didn't accept that pairing. Try again."));
            } catch (Exception e) {
                runOnUiThread(() -> show(
                        "I couldn't reach that Shield securely. Scan its QR again."));
            }
        }, "boop-shield-pair-relay");
        relay.setDaemon(true);
        relay.start();
    }

    private void show(String text) {
        if (status != null) {
            status.setText(text);
        }
    }

    private void closeCallbackServer() {
        if (callbackServer != null) {
            callbackServer.close();
            callbackServer = null;
        }
        endpointHolder.clientId = null;
    }

    @Override
    protected void onDestroy() {
        closeCallbackServer();
        super.onDestroy();
    }
}
