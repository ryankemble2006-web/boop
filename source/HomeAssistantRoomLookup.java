package com.boop.alpha1;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/** Resolve a user-entered room against HA; never invent a registry ID. */
final class HomeAssistantRoomLookup {
    static BoopRoom find(String baseUrl, String token, String name) throws Exception {
        String template = "{% set selected = area_id(" + JSONObject.quote(name)
                + ") %}{{ {'id': selected, 'name': area_name(selected)} | to_json }}";
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl+"/api/template").openConnection();
        connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
        connection.setRequestMethod("POST"); connection.setDoOutput(true);
        connection.setRequestProperty("Authorization","Bearer "+token);
        connection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        try {
            byte[] bytes = new JSONObject().put("template",template).toString().getBytes(StandardCharsets.UTF_8);
            try(OutputStream out=connection.getOutputStream()) { out.write(bytes); }
            if(connection.getResponseCode()!=200) throw new IOException("Room lookup unavailable");
            StringBuilder body=new StringBuilder();
            try(BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8))) {
                String line; while((line=reader.readLine())!=null) body.append(line);
            }
            return parse(new JSONObject(body.toString()));
        } finally { connection.disconnect(); }
    }
    static BoopRoom parse(JSONObject value) {
        if(value == null || value.isNull("id") || value.isNull("name")) return null;
        String id=value.optString("id","").trim(); String name=value.optString("name","").trim();
        return id.isEmpty() || name.isEmpty() ? null : new BoopRoom(id,name);
    }
}
