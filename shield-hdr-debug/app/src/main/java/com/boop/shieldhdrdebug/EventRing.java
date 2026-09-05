package com.boop.shieldhdrdebug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class EventRing {
    private EventRing() { }

    static String append(String existing, String event, int maxEvents) {
        List<String> lines = new ArrayList<>();
        if (existing != null && !existing.isEmpty()) {
            lines.addAll(Arrays.asList(existing.split("\\n")));
        }
        lines.add(event);
        int from = Math.max(0, lines.size() - Math.max(1, maxEvents));
        StringBuilder out = new StringBuilder();
        for (int i = from; i < lines.size(); i++) {
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(lines.get(i));
        }
        return out.toString();
    }
}
