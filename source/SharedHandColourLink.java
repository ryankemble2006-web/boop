package com.boop.alpha1;

import org.json.JSONObject;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/** HA protocol flow. All calls/callbacks are serialized by the Android owner. */
final class SharedHandColourLink {
    interface Reply { void complete(boolean success, Object result); }
    interface Channel { void request(String type, JSONObject body, Reply reply); }
    interface Listener {
        void ready(int hue);
        void colour(int hue);
        void confirmed(int hue);
        void failed(String message);
    }
    private final Channel channel;
    private final Listener listener;
    private final IntSupplier currentHue;
    private boolean active = true, ready, seeded;
    private String helperId, entityId, createdId;
    private long eventVersion;
    private String lastEvent;
    SharedHandColourLink(Channel channel, Listener listener, IntSupplier currentHue) {
        this.channel = channel; this.listener = listener; this.currentHue = currentHue;
    }
    void begin(boolean mayCreate) {
        ask("input_text/list", new JSONObject(), result -> {
            helperId = SharedHandColourHaProtocol.findHelper(result);
            if (helperId == null && mayCreate) create();
            else if (helperId == null) fail("Sharing needs setup. Your local hand colour is unchanged.");
            else resolve(() -> read(true));
        });
    }
    private void create() {
        try {
            ask("input_text/create", SharedHandColourHaProtocol.createBody(), result -> {
                if (!(result instanceof JSONObject)) throw new IllegalArgumentException("Invalid create receipt");
                createdId = ((JSONObject) result).optString("id", "");
                ask("input_text/list", new JSONObject(), list -> {
                    helperId = SharedHandColourHaProtocol.findHelper(list);
                    if (!createdId.equals(helperId)) throw new IllegalArgumentException("Ambiguous setup");
                    resolve(() -> read(true));
                });
            });
        } catch (Exception invalid) { fail("Home Assistant could not create colour sharing. Local colour is unchanged."); }
    }
    private void resolve(Runnable then) {
        ask("config/entity_registry/list", new JSONObject(), list -> {
            String next = SharedHandColourHaProtocol.findEntity(list, helperId);
            if (!next.equals(entityId)) { lastEvent = null; eventVersion++; }
            entityId = next;
            then.run();
        });
    }
    void event(String entity, String value) {
        if (!active || !entity.equals(entityId)) return;
        lastEvent = value; eventVersion++;
        if (!ready) return;
        Integer hue = SharedHandColourProtocol.decode(value);
        if (hue == null) fail("Shared hand colour is unavailable. Keeping this device's last colour.");
        else listener.colour(hue);
    }
    void write(int hue) {
        SharedHandColourProtocol.requireHue(hue);
        if (!active || !ready) return;
        ask("input_text/list", new JSONObject(), list -> {
            if (!helperId.equals(SharedHandColourHaProtocol.findHelper(list))) {
                throw new IllegalArgumentException("Colour helper identity changed");
            }
            resolve(() -> setValue(hue, false));
        });
    }
    private void setValue(int hue, boolean first) {
        try {
            JSONObject body = new JSONObject().put("domain", "input_text").put("service", "set_value")
                    .put("target", new JSONObject().put("entity_id", entityId))
                    .put("service_data", new JSONObject().put("value", SharedHandColourProtocol.encode(hue)));
            ask("call_service", body, ignored -> read(first));
        } catch (Exception invalid) { fail("Could not share this colour. The local setting still works."); }
    }
    private void read(boolean first) {
        long readAt = eventVersion;
        ask("get_states", new JSONObject(), result -> {
            Integer hue = SharedHandColourHaProtocol.readHue(result, entityId);
            if (eventVersion != readAt) hue = SharedHandColourProtocol.decode(lastEvent);
            if (hue == null && first && createdId != null && !seeded) {
                seeded = true; setValue(currentHue.getAsInt(), true); return;
            }
            if (hue == null) { fail("Shared hand colour is not ready. Your local colour is unchanged."); return; }
            if (first) { ready = true; listener.ready(hue); }
            else listener.confirmed(hue);
        });
    }
    private void ask(String type, JSONObject body, Consumer<Object> next) {
        if (!active) return;
        try {
            channel.request(type, body, (success, result) -> {
                if (!active) return;
                if (!success) { fail("Home Assistant could not complete colour sharing. Local colour still works."); return; }
                try { next.accept(result); }
                catch (RuntimeException invalid) { fail("Colour sharing could not verify its setting. Local colour is unchanged."); }
            });
        } catch (RuntimeException unavailable) { fail("Colour sharing is offline. Local colour still works."); }
    }
    private void fail(String message) {
        if (!active) return;
        active = false; ready = false;
        listener.failed(message);
    }
    void close() { active = false; ready = false; }
}
