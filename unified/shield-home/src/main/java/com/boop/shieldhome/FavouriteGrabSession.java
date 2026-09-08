package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.List;

/** Pure reorder session used while a favourite card is grabbed with the remote. */
public final class FavouriteGrabSession {
    private final List<String> original;
    private final String component;
    private List<String> current;

    private FavouriteGrabSession(List<String> values, String component) {
        this.original = values == null ? List.of() : List.copyOf(values);
        this.current = new ArrayList<>(this.original);
        this.component = component == null ? "" : component;
    }

    public static FavouriteGrabSession begin(List<String> values, String component) {
        return new FavouriteGrabSession(values, component);
    }

    public String grabbedComponent() {
        return component;
    }

    public int index() {
        return current.indexOf(component);
    }

    public List<String> current() {
        return List.copyOf(current);
    }

    public boolean move(int delta) {
        List<String> next = FavouriteOrder.move(current, component, delta);
        if (next.equals(current)) {
            return false;
        }
        current = next;
        return true;
    }

    public List<String> commit() {
        return List.copyOf(current);
    }

    public List<String> cancel() {
        current = new ArrayList<>(original);
        return List.copyOf(original);
    }
}
