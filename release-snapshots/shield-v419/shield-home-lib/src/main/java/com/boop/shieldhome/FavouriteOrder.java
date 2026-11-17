package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FavouriteOrder {
    private FavouriteOrder() {}

    public static List<String> reconcile(List<String> saved, List<TvAppEntry> installed) {
        Set<String> available = new HashSet<>();
        if (installed != null) {
            for (TvAppEntry entry : installed) {
                if (entry != null && !entry.component().isEmpty()) {
                    available.add(entry.component());
                }
            }
        }

        ArrayList<String> out = new ArrayList<>();
        if (saved != null) {
            for (String component : saved) {
                if (component != null && available.contains(component) && !out.contains(component)) {
                    out.add(component);
                }
            }
        }
        return out;
    }

    public static List<String> add(List<String> current, String component) {
        ArrayList<String> out = copy(current);
        if (component != null && !component.isEmpty() && !out.contains(component)) {
            out.add(component);
        }
        return out;
    }

    /** Available choices are installed components, never a stock launcher's database. */
    public static List<TvAppEntry> availableToAdd(List<TvAppEntry> installed, List<String> current) {
        Set<String> seen = new HashSet<>(current == null ? List.of() : current);
        ArrayList<TvAppEntry> choices = new ArrayList<>();
        if (installed != null) {
            for (TvAppEntry entry : installed) {
                if (entry != null && !entry.component().isEmpty() && seen.add(entry.component())) {
                    choices.add(entry);
                }
            }
        }
        return choices;
    }

    /** Add only. A cancelled/stale choice must never remove or reorder favourites. */
    public static List<String> addInstalled(
            List<String> current, List<TvAppEntry> installed, String component) {
        if (component != null && !component.isEmpty() && installed != null) {
            for (TvAppEntry entry : installed) {
                if (entry != null && component.equals(entry.component())) return add(current, component);
            }
        }
        return copy(current);
    }

    public static List<String> remove(List<String> current, String component) {
        ArrayList<String> out = copy(current);
        out.removeIf(value -> value != null && value.equals(component));
        return out;
    }

    public static List<String> move(List<String> current, String component, int delta) {
        ArrayList<String> out = copy(current);
        int from = out.indexOf(component);
        if (from < 0 || delta == 0) {
            return out;
        }
        int to = from + delta;
        if (to < 0 || to >= out.size()) {
            return out;
        }
        String value = out.remove(from);
        out.add(to, value);
        return out;
    }

    private static ArrayList<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
