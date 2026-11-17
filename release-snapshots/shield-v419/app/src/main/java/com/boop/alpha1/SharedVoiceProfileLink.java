package com.boop.alpha1;

import org.json.JSONObject;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** HA flow for one bounded BOOP voice profile helper. */
final class SharedVoiceProfileLink {
    interface Reply { void complete(boolean success, Object result); }
    interface Channel { void request(String type, JSONObject body, Reply reply); }
    interface Listener {
        void ready(SharedVoiceProfileProtocol.Profile profile);
        void profile(SharedVoiceProfileProtocol.Profile profile);
        void confirmed(SharedVoiceProfileProtocol.Profile profile);
        void failed(String message);
    }

    private final Channel channel;
    private final Listener listener;
    private final Supplier<SharedVoiceProfileProtocol.Profile> currentProfile;
    private boolean active = true, ready, seeded;
    private String helperId, entityId, createdId;
    private long eventVersion;
    private String lastEvent;

    SharedVoiceProfileLink(Channel channel, Listener listener,
            Supplier<SharedVoiceProfileProtocol.Profile> currentProfile) {
        this.channel = channel;
        this.listener = listener;
        this.currentProfile = currentProfile;
    }

    void begin(boolean mayCreate) {
        ask("input_text/list", new JSONObject(), result -> {
            helperId = SharedVoiceProfileHaProtocol.findHelper(result);
            if (helperId == null && mayCreate) create();
            else if (helperId == null) fail("Voice sharing needs setup. This device's voice is unchanged.");
            else resolve(() -> read(true));
        });
    }

    private void create() {
        try {
            ask("input_text/create", SharedVoiceProfileHaProtocol.createBody(), result -> {
                if (!(result instanceof JSONObject)) throw new IllegalArgumentException("Invalid create receipt");
                createdId = ((JSONObject) result).optString("id", "");
                ask("input_text/list", new JSONObject(), list -> {
                    helperId = SharedVoiceProfileHaProtocol.findHelper(list);
                    if (!createdId.equals(helperId)) throw new IllegalArgumentException("Ambiguous setup");
                    resolve(() -> read(true));
                });
            });
        } catch (Exception invalid) {
            fail("Home Assistant could not create voice sharing. This device's voice is unchanged.");
        }
    }

    private void resolve(Runnable then) {
        ask("config/entity_registry/list", new JSONObject(), list -> {
            String next = SharedVoiceProfileHaProtocol.findEntity(list, helperId);
            if (!next.equals(entityId)) { lastEvent = null; eventVersion++; }
            entityId = next;
            then.run();
        });
    }

    void event(String entity, String value) {
        if (!active || !entity.equals(entityId)) return;
        lastEvent = value;
        eventVersion++;
        if (!ready) return;
        SharedVoiceProfileProtocol.Profile profile = SharedVoiceProfileProtocol.decode(value);
        if (profile == null) fail("Shared voice profile is unavailable. Keeping this device's last voice.");
        else listener.profile(profile);
    }

    void write(SharedVoiceProfileProtocol.Profile profile) {
        if (profile == null || !active || !ready) return;
        ask("input_text/list", new JSONObject(), list -> {
            if (!helperId.equals(SharedVoiceProfileHaProtocol.findHelper(list))) {
                throw new IllegalArgumentException("Voice helper identity changed");
            }
            resolve(() -> setValue(profile, false));
        });
    }

    private void setValue(SharedVoiceProfileProtocol.Profile profile, boolean first) {
        try {
            JSONObject body = new JSONObject().put("domain", "input_text").put("service", "set_value")
                    .put("target", new JSONObject().put("entity_id", entityId))
                    .put("service_data", new JSONObject().put("value", SharedVoiceProfileProtocol.encode(profile)));
            ask("call_service", body, ignored -> read(first));
        } catch (Exception invalid) {
            fail("Could not share this voice profile. The local controls still work.");
        }
    }

    private void read(boolean first) {
        long readAt = eventVersion;
        ask("get_states", new JSONObject(), result -> {
            SharedVoiceProfileProtocol.Profile profile = SharedVoiceProfileHaProtocol.readProfile(result, entityId);
            if (eventVersion != readAt) profile = SharedVoiceProfileProtocol.decode(lastEvent);
            if (profile == null && first && createdId != null && !seeded) {
                seeded = true;
                setValue(currentProfile.get(), true);
                return;
            }
            if (profile == null) {
                fail("Shared voice profile is not ready. This device's voice is unchanged.");
                return;
            }
            if (first) { ready = true; listener.ready(profile); }
            else listener.confirmed(profile);
        });
    }

    private void ask(String type, JSONObject body, Consumer<Object> next) {
        if (!active) return;
        try {
            channel.request(type, body, (success, result) -> {
                if (!active) return;
                if (!success) {
                    fail("Home Assistant could not complete voice sharing. Local voice controls still work.");
                    return;
                }
                try { next.accept(result); }
                catch (RuntimeException invalid) {
                    fail("Voice sharing could not verify its setting. This device's voice is unchanged.");
                }
            });
        } catch (RuntimeException unavailable) {
            fail("Voice sharing is offline. Local voice controls still work.");
        }
    }

    private void fail(String message) {
        if (!active) return;
        active = false;
        ready = false;
        listener.failed(message);
    }

    void close() { active = false; ready = false; }
}
