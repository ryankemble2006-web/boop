package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class SharedHandColourLinkHarness {
    static int checks;
    static void check(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    static JSONObject helper(String id) throws Exception {
        return SharedHandColourHaProtocol.createBody().put("id", id);
    }
    static final class Events implements SharedHandColourLink.Listener {
        Integer ready, colour, confirmed;
        String failure;
        public void ready(int hue) { ready = hue; }
        public void colour(int hue) { colour = hue; }
        public void confirmed(int hue) { confirmed = hue; }
        public void failed(String message) { failure = message; }
    }
    static final class Fake implements SharedHandColourLink.Channel {
        JSONArray helpers = new JSONArray();
        String value = "BOOP_HAND_V1|220", entity = "input_text.renamed_colour";
        String failType;
        boolean defer, permitCreate = true;
        final List<String> requests = new ArrayList<>();
        final List<Runnable> pending = new ArrayList<>();
        public void request(String type, JSONObject body, SharedHandColourLink.Reply reply) {
            requests.add(type);
            Runnable answer = () -> {
                try {
                    if (type.equals(failType)) { reply.complete(false, null); return; }
                    Object result;
                    switch (type) {
                        case "input_text/list": result = helpers; break;
                        case "input_text/create":
                            check(permitCreate, "Creation was not authorized");
                            check(!body.has("initial"), "Do not reset HA colour at every restart");
                            JSONObject created = helper("owned"); helpers.put(created);
                            value = "unknown"; result = created; break;
                        case "config/entity_registry/list":
                            result = new JSONArray().put(new JSONObject().put("entity_id", entity)
                                    .put("unique_id", "owned").put("platform", "input_text")); break;
                        case "get_states":
                            result = new JSONArray().put(new JSONObject().put("entity_id", entity)
                                    .put("state", value)); break;
                        case "call_service":
                            check("input_text".equals(body.getString("domain")), "Only colour helper service");
                            check("set_value".equals(body.getString("service")), "Never arbitrary HA commands");
                            check(entity.equals(body.getJSONObject("target").getString("entity_id")), "Use discovered identity");
                            value = body.getJSONObject("service_data").getString("value"); result = null; break;
                        default: throw new AssertionError(type);
                    }
                    reply.complete(true, result);
                } catch (Exception error) { throw new AssertionError(error); }
            };
            if (defer) pending.add(answer); else answer.run();
        }
    }
    public static void main(String[] args) throws Exception {
        Fake a = new Fake(); a.helpers.put(helper("owned")); a.permitCreate = false;
        Events e = new Events(); SharedHandColourLink link = new SharedHandColourLink(a, e, () -> 280);
        link.begin(false);
        check(e.ready == 220 && e.failure == null, "Join reads HA, not the cached Wall value");
        check(!a.requests.contains("call_service"), "Joining must not overwrite shared hue");
        link.event("input_text.other", "BOOP_HAND_V1|50");
        check(e.colour == null, "Ignore other entities");
        link.event(a.entity, "BOOP_HAND_V1|120");
        check(e.colour == 120, "Receive shared colour event");
        link.write(300);
        check(e.confirmed == 300, "Reread accepted shared state after write");
        check(a.requests.get(a.requests.size()-1).equals("get_states"), "Transport ACK is not colour truth");
        Fake missing = new Fake(); missing.permitCreate = false;
        Events m = new Events(); new SharedHandColourLink(missing, m, () -> 270).begin(false);
        check(m.failure != null && !missing.requests.contains("input_text/create"), "Startup never creates helpers");
        Fake create = new Fake(); Events c = new Events();
        new SharedHandColourLink(create, c, () -> 270).begin(true);
        check(c.ready == 270 && c.failure == null, "Explicit first setup seeds current hue, not a default");
        check(create.requests.stream().filter("input_text/create"::equals).count() == 1, "Only one helper create");
        Fake duplicate = new Fake(); duplicate.helpers.put(helper("owned")).put(helper("other"));
        Events d = new Events(); new SharedHandColourLink(duplicate, d, () -> 190).begin(true);
        check(d.failure != null && !duplicate.requests.contains("call_service"), "Ambiguity fails closed");
        Fake bad = new Fake(); bad.helpers.put(helper("owned")); bad.value = "invalid";
        Events b = new Events(); new SharedHandColourLink(bad, b, () -> 190).begin(true);
        check(b.failure != null && !bad.requests.contains("call_service"), "Existing bad state is never auto-overwritten");
        Fake late = new Fake(); late.helpers.put(helper("owned")); late.defer = true;
        Events l = new Events(); SharedHandColourLink stopped = new SharedHandColourLink(late, l, () -> 190);
        stopped.begin(false); stopped.close(); late.pending.remove(0).run();
        check(l.ready == null && late.requests.size() == 1, "Late callbacks after stop do nothing");
        for (String step : List.of("input_text/list", "config/entity_registry/list", "get_states")) {
            Fake denied = new Fake(); denied.helpers.put(helper("owned")); denied.failType = step;
            Events no = new Events(); new SharedHandColourLink(denied, no, () -> 190).begin(false);
            check(no.failure != null && no.ready == null, "Failure must not apply a guessed colour");
        }
        a.helpers = new JSONArray();
        link.write(50);
        check(e.failure != null, "A removed/retagged helper cannot receive a write");
        Fake loss = new Fake(); loss.helpers.put(helper("owned")); Events lost = new Events();
        SharedHandColourLink failing = new SharedHandColourLink(loss, lost, () -> 190);
        failing.begin(false); loss.failType = "call_service"; failing.write(75);
        check(lost.failure != null && lost.confirmed == null, "Failed send never reports shared success");
        System.out.println("SharedHandColourLinkHarness: " + checks + " checks passed");
    }
}
