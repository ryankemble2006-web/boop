package com.boop.shieldoverlay;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public final class HaCommand {
    private final int id;
    private final String type;
    private final JSONObject body;

    public HaCommand(int id, String type, JSONObject body) {
        if (id < 1) {
            throw new IllegalArgumentException("command id must be positive");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("command type is required");
        }
        this.id = id;
        this.type = type;
        this.body = body == null ? new JSONObject() : body;
    }

    public int id() {
        return id;
    }

    public String type() {
        return type;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject out = new JSONObject();
        out.put("id", id);
        out.put("type", type);

        Iterator<String> keys = body.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if ("id".equals(key) || "type".equals(key)) {
                continue;
            }
            out.put(key, body.get(key));
        }
        return out;
    }
}
