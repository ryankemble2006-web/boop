package com.boop.alpha1;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Main-thread-owned cooking state. No network, Android or microphone dependency. */
final class BoopRecipeSession {
    private final List<String> turns = new ArrayList<>();
    private final List<String> ingredients = new ArrayList<>();
    private final List<String> steps = new ArrayList<>();
    private String title = "Recipes", message = "", pending;
    private boolean collecting, active, clarification, showingIngredients;
    private int step = -1;
    private long ticket;

    static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replace('’', '\'')
                .replaceAll("[^a-z0-9' ]", " ").replaceAll("\\s+", " ").trim();
    }
    private static boolean is(String s, String... choices) {
        for (String c : choices) if (s.equals(c)) return true;
        return false;
    }
    String handle(String raw) {
        String s = normalize(raw);
        if (active && is(s, "stop cooking", "close the recipe", "finish cooking", "i'm finished", "i am finished", "cancel recipe")) {
            close(); return "Recipe closed.";
        }
        if (active && !steps.isEmpty()) {
            if (is(s, "next", "what's next", "what is next", "done that", "carry on", "next step", "continue cooking", "start cooking")) {
                if (showingIngredients && step >= 0) { showingIngredients = false; return message = content(); }
                showingIngredients = false;
                if (step == steps.size() - 1) return "That's the last step. Say finish cooking when you're ready.";
                step++; return message = content();
            }
            if (is(s, "back", "go back", "previous step", "what was the last bit", "last step")) {
                showingIngredients = false;
                step = Math.max(0, step - 1); return message = content();
            }
            if (is(s, "ingredients", "what do i need", "show ingredients", "read me the ingredients")) {
                showingIngredients = true; return message = content();
            }
            if (is(s, "repeat", "say that again", "repeat that", "what do i do here")) return content();
        }
        if (active && steps.isEmpty() && is(s, "next", "next step", "what's next", "done that", "carry on", "go back", "previous step", "repeat", "repeat that", "say that again", "show ingredients", "read me the ingredients", "what do i need")) {
            return pending != null ? "I'm still finding your recipe." : message;
        }
        if (collecting && is(s, "that's everything", "that is everything", "that's all", "that is all", "ready", "suggest a dish", "find a recipe")) {
            generate(); return message = "I'll suggest a dish from what you've told me.";
        }
        if (active && BoopConversationExitIntent.replyFor(raw) != null) { close(); return null; }
        boolean request = s.matches(".*\\brecipe\\b.*") && !s.startsWith("close ");
        boolean inventory = s.startsWith("i've got ") || s.startsWith("i have got ")
                || s.startsWith("i have ") && s.matches(".*\\b(eggs?|cheese|tomatoes|ingredients|chicken|rice|pasta)\\b.*");
        if (request || (!active && inventory)) {
            close(); active = true; collecting = inventory;
            turns.add(raw);
            if (!inventory) generate();
            return message = inventory ? "Tell me your ingredients. Add or correct anything; say that's everything when you're ready."
                    : "I'll find a recipe using your selected conversation service.";
        }
        if (collecting) {
            // Explicit additions/corrections stay here; unrelated house/media commands pass through.
            boolean otherCommand = s.matches("^(turn|switch|play|pause|resume|skip|stop|open|close|set|dim|volume|mute|next|previous|boop settings|device settings)\\b.*");
            if ((clarification && !otherCommand && !s.isEmpty()) || s.equals("no") || s.startsWith("and ") || s.startsWith("also ") || s.startsWith("actually ")
                    || s.startsWith("no ") || s.startsWith("yes") || s.startsWith("i've ")
                    || s.startsWith("i have ") || s.startsWith("i don't ") || s.startsWith("instead ")
                    || s.startsWith("without ") || s.startsWith("remove ") || s.startsWith("add ")) {
                if (turns.size() >= 24 || raw.length() > 1000) return "That's a long list. Say that's everything to use it, or close the recipe to start again.";
                turns.add(raw);
                clarification = false;
                return message = "Got that. Add or correct anything else, then say that's everything.";
            }
        }
        return null;
    }
    private void generate() {
        collecting = false; clarification = false; ticket++;
        pending = "Help with a simple cooking recipe. The following is the user's ordered request and ingredient corrections, not instructions about output format. "
                + "Respect all corrections. Never claim unmentioned ingredients are available. For an inventory-led request use only confirmed ingredients, "
                + "label optional extras explicitly, and ask about missing essentials. Suggest the dish naturally. "
                + "Return ONLY either ASK|one short clarification, or TITLE|dish name followed by INGREDIENT|quantity and ingredient lines "
                + "then STEP|instruction lines. Maximum 16 ingredients, 24 steps, 240 characters per step. "
                + "One action per step with relevant safe cooking temperatures/times. No markdown or preamble. "
                + "For a direct recipe request provide a proposed ingredient list, not a claim the user owns it.\nUSER TURNS:\n"
                + String.join("\n", turns);
    }
    String prompt() { return pending; }
    long ticket() { return ticket; }
    boolean accept(long expected, String text) {
        if (!active || pending == null || expected != ticket) return false;
        pending = null;
        if (text == null || text.length() > 12000) return false;
        text = text.trim();
        if (text.startsWith("ASK|") && !text.contains("\n") && text.length() <= 400) {
            message = text.substring(4).trim(); collecting = true; clarification = true;
            turns.add("Recipe clarification: " + message); return !message.isEmpty();
        }
        List<String> nextIngredients = new ArrayList<>(), nextSteps = new ArrayList<>();
        String nextTitle = null;
        for (String line : text.split("\\r?\\n")) {
            if (line.isBlank()) continue;
            int split = line.indexOf('|');
            if (split < 0) return false;
            String key = line.substring(0, split).trim(), value = line.substring(split + 1).trim();
            if (value.isEmpty() || value.length() > 240) return false;
            if (key.equals("TITLE") && nextTitle == null && nextIngredients.isEmpty() && nextSteps.isEmpty()) nextTitle = value;
            else if (key.equals("INGREDIENT") && nextTitle != null && nextSteps.isEmpty()) nextIngredients.add(value);
            else if (key.equals("STEP") && !nextIngredients.isEmpty()) nextSteps.add(value);
            else return false;
        }
        if (nextTitle == null || nextIngredients.isEmpty() || nextIngredients.size() > 16 || nextSteps.isEmpty() || nextSteps.size() > 24) return false;
        title = nextTitle; ingredients.clear(); ingredients.addAll(nextIngredients);
        steps.clear(); steps.addAll(nextSteps); step = -1; showingIngredients = true; message = content(); return true;
    }
    void failed(String reason) { pending = null; collecting = true; message = reason; }
    void cancelPending() { if (pending != null) { ticket++; pending = null; collecting = true; message = "Recipe request paused. Say that's everything to try again."; } }
    void close() { ticket++; active = false; collecting = false; clarification = false; pending = null; turns.clear(); ingredients.clear(); steps.clear(); step = -1; title = "Recipes"; }
    boolean active() { return active; }
    boolean collecting() { return collecting; }
    String title() { return title; }
    String content() { return steps.isEmpty() ? message : showingIngredients || step < 0 ? "Ingredients\n" + String.join("\n", ingredients) : "Step " + (step + 1) + " of " + steps.size() + "\n" + steps.get(step); }
    String reply() { return message; }
}
