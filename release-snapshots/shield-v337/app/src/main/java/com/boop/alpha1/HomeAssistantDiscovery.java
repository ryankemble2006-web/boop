package com.boop.alpha1;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

final class HomeAssistantDiscovery {
    interface Listener {
        void onFound(String displayName, String baseUrl);
        void onUnavailable(String reason);
    }

    private static final String SERVICE_TYPE = "_home-assistant._tcp.";

    private final NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private boolean discovering;

    HomeAssistantDiscovery(Context context) {
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    void start(Listener listener) {
        stop();
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override public void onDiscoveryStarted(String serviceType) {
                discovering = true;
            }

            @Override public void onServiceFound(NsdServiceInfo serviceInfo) {
                if (!serviceInfo.getServiceType().contains("_home-assistant._tcp")) {
                    return;
                }
                resolve(serviceInfo, listener);
            }

            @Override public void onServiceLost(NsdServiceInfo serviceInfo) { }

            @Override public void onDiscoveryStopped(String serviceType) {
                discovering = false;
            }

            @Override public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                discovering = false;
                listener.onUnavailable("Discovery failed: " + errorCode);
            }

            @Override public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                discovering = false;
            }
        };

        try {
            nsdManager.discoverServices(
                    SERVICE_TYPE,
                    NsdManager.PROTOCOL_DNS_SD,
                    discoveryListener);
        } catch (RuntimeException e) {
            discoveryListener = null;
            listener.onUnavailable("Discovery unavailable");
        }
    }

    @SuppressWarnings("deprecation")
    private void resolve(NsdServiceInfo found, Listener listener) {
        try {
            nsdManager.resolveService(found, new NsdManager.ResolveListener() {
                @Override public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) { }

                @Override public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    String baseUrl = preferredBaseUrl(serviceInfo);
                    if (baseUrl != null) {
                        listener.onFound(serviceInfo.getServiceName(), baseUrl);
                    }
                }
            });
        } catch (RuntimeException ignored) { }
    }

    void stop() {
        if (discoveryListener != null && discovering) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
            } catch (RuntimeException ignored) { }
        }
        discovering = false;
        discoveryListener = null;
    }

    private static String preferredBaseUrl(NsdServiceInfo info) {
        byte[] internal = info.getAttributes().get("internal_url");
        if (internal != null) {
            String value = new String(internal, StandardCharsets.UTF_8).trim();
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return HomeAssistantAuthUrls.trim(value);
            }
        }

        InetAddress host = info.getHost();
        if (host == null || info.getPort() <= 0) {
            return null;
        }
        String address = host.getHostAddress();
        if (host instanceof Inet6Address) {
            address = "[" + address + "]";
        }
        return "http://" + address + ":" + info.getPort();
    }
}
