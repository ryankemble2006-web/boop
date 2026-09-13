package com.boop.shieldhome;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Exercises the real favourite model, without Android UI or a stock launcher. */
public final class FavouritePickerHarness {
    private static int checks;
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
    }
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Method available;
        Method add;
        try {
            available = FavouriteOrder.class.getMethod("availableToAdd", List.class, List.class);
            add = FavouriteOrder.class.getMethod("addInstalled", List.class, List.class, String.class);
        } catch (NoSuchMethodException missing) {
            throw new AssertionError("BOOP has no add-only installed-app selector", missing);
        }
        TvAppEntry a = new TvAppEntry("a/Main", "a", "Alpha");
        TvAppEntry b = new TvAppEntry("b/Main", "b", "Beta");
        TvAppEntry c = new TvAppEntry("c/Main", "c", "Charlie");
        List<TvAppEntry> installed = Arrays.asList(a, b, b, null,
                new TvAppEntry("", "empty", "Invalid"), c);
        ArrayList<String> saved = new ArrayList<>(List.of("c/Main", "a/Main"));
        List<TvAppEntry> options = (List<TvAppEntry>) available.invoke(null, installed, saved);
        check(options.equals(List.of(b)), "Hide existing favourites and duplicate candidates");
        check(saved.equals(List.of("c/Main", "a/Main")), "Opening picker must not mutate order");
        check(((List<?>) available.invoke(null, null, saved)).isEmpty(), "Missing inventory is empty");
        check(((List<?>) available.invoke(null, installed, null)).equals(List.of(a, b, c)),
                "Empty favourites retain discovery order and valid unique candidates");
        List<String> added = (List<String>) add.invoke(null, saved, installed, "b/Main");
        check(added.equals(List.of("c/Main", "a/Main", "b/Main")), "Append new favourite only");
        check(saved.equals(List.of("c/Main", "a/Main")), "Inputs remain immutable");
        check(add.invoke(null, added, installed, "b/Main").equals(added), "No duplicate insertion");
        check(add.invoke(null, saved, installed, "gone/Main").equals(saved), "Reject removed app");
        check(add.invoke(null, saved, installed, null).equals(saved), "Cancel changes nothing");
        check(add.invoke(null, saved, installed, "").equals(saved), "Reject empty selection");
        check(add.invoke(null, saved, null, "b/Main").equals(saved), "No inventory cannot add");
        check(add.invoke(null, null, installed, "b/Main").equals(List.of("b/Main")), "First favourite");
        check(((List<?>) available.invoke(null, installed, added)).isEmpty(), "All-added empty state");
        check(add.invoke(null, added, List.of(a, c), "b/Main").equals(added),
                "Stale selection cannot remove an already saved favourite");
        FavouriteGrabSession grab = FavouriteGrabSession.begin(added, "a/Main");
        check(grab.move(1) && grab.current().equals(List.of("c/Main", "b/Main", "a/Main")),
                "Existing reordering is preserved; add tile is not stored as an app");
        check(!grab.move(1), "Cannot drag beyond the last real app");
        check(grab.cancel().equals(added), "Cancel restores original favourite order");
        System.out.println("Favourite picker: " + checks + " behavioural checks passed");
    }
}
