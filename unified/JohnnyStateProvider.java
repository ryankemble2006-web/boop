package com.boop.alpha1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemClock;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;

/** Read-only, fixed-area bridge. Credentials and arbitrary HA access never leave BOOP. */
public final class JohnnyStateProvider extends ContentProvider {
    private String token, base, credential;
    private long tokenTime, snapshotTime;
    private String cached;
    @Override public boolean onCreate() { return true; }

    private void enforceCaller() {
        try {
            PackageManager pm = getContext().getPackageManager();
            String[] names = pm.getPackagesForUid(Binder.getCallingUid());
            if (names == null || names.length != 1 || !JohnnyStatePolicy.PACKAGE.equals(names[0]))
                throw new SecurityException("Johnny HA identity required");
            PackageInfo info = pm.getPackageInfo(names[0], PackageManager.GET_SIGNING_CERTIFICATES);
            Signature[] signatures = info.signingInfo == null ? null : info.signingInfo.getApkContentsSigners();
            if (signatures == null || signatures.length != 1) throw new SecurityException("Johnny signer required");
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray());
            StringBuilder digest = new StringBuilder();
            for (byte b : bytes) digest.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
            if (!JohnnyStatePolicy.trusted(names[0], digest.toString())) throw new SecurityException("Johnny signer mismatch");
        } catch (SecurityException e) { throw e;
        } catch (Exception e) { throw new SecurityException("Johnny identity unavailable"); }
    }

    @Override public Bundle call(String method, String arg, Bundle extras) {
        enforceCaller();
        if (!"snapshot".equals(method) || arg != null || (extras != null && !extras.isEmpty()))
            throw new IllegalArgumentException("Only the fixed snapshot is supported");
        // Binder caller identity is checked first; all HA work then uses BOOP's own identity.
        long identity = Binder.clearCallingIdentity();
        try {
            Bundle out = new Bundle();
            out.putString("json", snapshot());
            return out;
        } finally { Binder.restoreCallingIdentity(identity); }
    }

    private synchronized String snapshot() {
        long now = SystemClock.elapsedRealtime();
        if (cached != null && now - snapshotTime < 1500) return cached;
        try {
            SecureTokenStore store = BoopVoiceTokenStore.create(getContext());
            if (!store.hasConnection()) { token = null; credential = null; throw new IllegalStateException(); }
            String currentCredential = store.getRefreshToken();
            String currentBase = HomeAssistantAuthUrls.trim(store.getBaseUrl());
            if (!currentBase.equals(base) || !java.util.Objects.equals(currentCredential, credential)) {
                base = currentBase; credential = currentCredential; token = null;
            }
            if (token == null || now - tokenTime > 20 * 60 * 1000) {
                token = new HomeAssistantAuth(getContext(), store).freshAccessToken();
                tokenTime = now;
            }
            // Fixed template: caller cannot supply entities, area, paths or service commands.
            JSONObject template = new JSONObject().put("template",
                    "{{ area_entities('Living Room') | list | to_json }}");
            JSONArray area = new JSONArray(request("/api/template", template.toString()));
            HashSet<String> ids = new HashSet<>();
            for (int i = 0; i < area.length(); i++) ids.add(area.getString(i));
            JSONArray all = new JSONArray(request("/api/states", null));
            java.util.HashMap<String, String> lightStates = new java.util.HashMap<>();
            for (String id : ids) if (id.startsWith("light.")) lightStates.put(id, "unknown");
            JSONArray lights = new JSONArray(), fans = new JSONArray();
            String fan = "unknown";
            for (int i = 0; i < all.length(); i++) {
                JSONObject entity = all.getJSONObject(i);
                String id = entity.optString("entity_id");
                if (!ids.contains(id)) continue;
                JSONObject attrs = entity.optJSONObject("attributes");
                String name = attrs == null ? id : attrs.optString("friendly_name", id);
                String state = JohnnyStatePolicy.state(entity.optString("state"));
                if (id.startsWith("light.")) {
                    lightStates.put(id, state);
                    lights.put(new JSONObject().put("id", id).put("state", state));
                } else if (JohnnyStatePolicy.isFan(id, name)) {
                    fan = state;
                    fans.put(new JSONObject().put("id", id).put("state", state));
                }
            }
            cached = new JSONObject().put("status", "ok")
                    .put("lights", JohnnyStatePolicy.lights(lightStates.values().toArray(new String[0])))
                    .put("fan", fans.length() == 1 ? fan : "unknown")
                    .put("lightEntities", lights).put("fanEntities", fans).toString();
        } catch (Exception ignored) {
            cached = "{\"status\":\"unavailable\",\"lights\":\"unknown\",\"fan\":\"unknown\"}";
        }
        snapshotTime = SystemClock.elapsedRealtime();
        return cached;
    }

    private String request(String path, String body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(base + path).openConnection();
        try {
            connection.setConnectTimeout(4000); connection.setReadTimeout(4000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Authorization", "Bearer " + token);
            if (body != null) {
                connection.setRequestMethod("POST"); connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try (java.io.OutputStream stream = connection.getOutputStream()) {
                    stream.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            if (status == 401) token = null;
            if (status < 200 || status >= 300) throw new java.io.IOException("State read unavailable");
            try (InputStream in = connection.getInputStream();
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) {
                    if (out.size() + count > 8 * 1024 * 1024) throw new java.io.IOException("State limit");
                    out.write(buffer, 0, count);
                }
                return out.toString("UTF-8");
            }
        } finally { connection.disconnect(); }
    }

    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { throw new SecurityException("Unsupported"); }
    @Override public String getType(Uri u) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { throw new SecurityException("Read only"); }
    @Override public int delete(Uri u, String s, String[] a) { throw new SecurityException("Read only"); }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { throw new SecurityException("Read only"); }
}
