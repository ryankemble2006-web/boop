package com.boop.shieldoverlay;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class HomeAssistantDiscovery {
    static final String SERVICE_TYPE = "_home-assistant._tcp.";

    public interface Listener {
        void onDiscovered(DiscoveredHomeAssistant homeAssistant);
        void onError(String message);
    }

    private final Context appContext;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private Listener listener;
    private boolean resolving;

    public HomeAssistantDiscovery(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        appContext = context.getApplicationContext();
    }

    public synchronized void start(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        stop();
        this.listener = listener;
        nsdManager = appContext.getSystemService(NsdManager.class);
        if (nsdManager == null) {
            Listener target = this.listener;
            this.listener = null;
            target.onError("I can't look for the house on this network.");
            return;
        }

        discoveryListener = createDiscoveryListener();
        nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener);
    }

    public synchronized void stop() {
        NsdManager manager = nsdManager;
        NsdManager.DiscoveryListener activeDiscovery = discoveryListener;
        discoveryListener = null;
        resolving = false;
        listener = null;
        if (manager != null && activeDiscovery != null) {
            try {
                manager.stopServiceDiscovery(activeDiscovery);
            } catch (IllegalArgumentException ignored) {
                // Discovery had already stopped or never finished starting.
            }
        }
    }

    static DiscoveredHomeAssistant selectLocalEndpoint(
            String serviceName,
            String resolvedHost,
            int port,
            Map<String, String> txt) {
        Map<String, String> attributes = txt == null ? new HashMap<>() : txt;
        String name = firstNonBlank(attributes.get("location_name"), serviceName, "Home Assistant");
        String uuid = trimmedOrEmpty(attributes.get("uuid"));

        String internalUrl = trimmedOrEmpty(attributes.get("internal_url"));
        if (isUsableHttpUrl(internalUrl)) {
            return new DiscoveredHomeAssistant(name, uuid, internalUrl);
        }

        if (resolvedHost == null || resolvedHost.trim().isEmpty()) {
            throw new IllegalArgumentException("resolved host is required when internal_url is unavailable");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("resolved port out of range");
        }

        String host = resolvedHost.trim();
        if (host.indexOf(':') >= 0 && !(host.startsWith("[") && host.endsWith("]"))) {
            host = "[" + host + "]";
        }
        return new DiscoveredHomeAssistant(name, uuid, "http://" + host + ":" + port);
    }

    private NsdManager.DiscoveryListener createDiscoveryListener() {
        return new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) { }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                synchronized (HomeAssistantDiscovery.this) {
                    if (listener == null || resolving || nsdManager == null) {
                        return;
                    }
                    resolving = true;
                    nsdManager.resolveService(serviceInfo, createResolveListener());
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) { }

            @Override
            public void onDiscoveryStopped(String serviceType) { }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                fail("I can't look for the house on this network.");
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                fail("I couldn't finish looking for the house.");
            }
        };
    }

    private NsdManager.ResolveListener createResolveListener() {
        return new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                synchronized (HomeAssistantDiscovery.this) {
                    resolving = false;
                }
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                DiscoveredHomeAssistant found;
                try {
                    String host = serviceInfo.getHost() == null
                            ? null
                            : serviceInfo.getHost().getHostAddress();
                    found = selectLocalEndpoint(
                            serviceInfo.getServiceName(),
                            host,
                            serviceInfo.getPort(),
                            decodeAttributes(serviceInfo.getAttributes()));
                } catch (IllegalArgumentException invalid) {
                    synchronized (HomeAssistantDiscovery.this) {
                        resolving = false;
                    }
                    return;
                }

                Listener target;
                synchronized (HomeAssistantDiscovery.this) {
                    target = listener;
                }
                if (target != null) {
                    stopKeepingTarget();
                    target.onDiscovered(found);
                }
            }
        };
    }

    private synchronized void stopKeepingTarget() {
        Listener target = listener;
        NsdManager manager = nsdManager;
        NsdManager.DiscoveryListener activeDiscovery = discoveryListener;
        discoveryListener = null;
        resolving = false;
        listener = null;
        if (manager != null && activeDiscovery != null) {
            try {
                manager.stopServiceDiscovery(activeDiscovery);
            } catch (IllegalArgumentException ignored) {
                // Discovery was already stopping.
            }
        }
        listener = null;
    }

    private void fail(String message) {
        Listener target;
        synchronized (this) {
            target = listener;
        }
        if (target != null) {
            stopKeepingTarget();
            target.onError(message);
        }
    }

    private static Map<String, String> decodeAttributes(Map<String, byte[]> raw) {
        Map<String, String> decoded = new HashMap<>();
        if (raw == null) {
            return decoded;
        }
        for (Map.Entry<String, byte[]> entry : raw.entrySet()) {
            byte[] value = entry.getValue();
            if (entry.getKey() != null && value != null) {
                decoded.put(entry.getKey(), new String(value, StandardCharsets.UTF_8));
            }
        }
        return decoded;
    }

    private static boolean isUsableHttpUrl(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        String value = trimmedOrEmpty(first);
        if (!value.isEmpty()) {
            return value;
        }
        value = trimmedOrEmpty(second);
        return value.isEmpty() ? fallback : value;
    }

    private static String trimmedOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
