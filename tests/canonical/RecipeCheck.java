package com.boop.alpha1;

public final class RecipeCheck {
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    public static void main(String[] args) {
        BoopRecipeSession s = new BoopRecipeSession();
        check(s.handle("next") == null);
        check(s.handle("I've got eggs, and cheese, and tomatoes") != null);
        check(s.collecting());
        check(s.handle("next") != null);
        check(s.handle("and spinach") != null);
        check(s.handle("actually no tomatoes") != null);
        check(s.prompt() == null);
        s.handle("that's everything");
        check(s.handle("next") != null);
        String prompt = s.prompt();
        check(prompt.contains("and spinach") && prompt.contains("actually no tomatoes"));
        long ticket = s.ticket();
        check(s.accept(ticket, "TITLE|Omelette\nINGREDIENT|2 eggs\nINGREDIENT|Cheese\nSTEP|Beat the eggs.\nSTEP|Cook until set."));
        check(s.handle("what's next").contains("Beat"));
        check(s.handle("done that").contains("Cook"));
        check(s.handle("carry on").contains("last step"));
        check(s.handle("what was the last bit").contains("Beat"));
        s.handle("next step");
        check(s.handle("read me the ingredients").contains("2 eggs"));
        check(s.handle("repeat that").contains("2 eggs"));
        check(s.handle("continue cooking").contains("Cook"));
        check(s.handle("next track") == null);
        check(s.handle("turn off the fan") == null);
        check(s.handle("stop cooking") != null && !s.active());
        check(!s.accept(ticket, "TITLE|Late\nINGREDIENT|egg\nSTEP|Cook"));
        s.handle("give me an easy egg recipe");
        check(s.prompt() != null);
        check(!s.accept(s.ticket(), "Here is a recipe without structure"));
        s.handle("give me an easy egg recipe");
        ticket = s.ticket(); s.cancelPending();
        check(!s.accept(ticket, "TITLE|Late\nINGREDIENT|egg\nSTEP|Cook"));
        s.handle("give me an easy egg recipe");
        check(s.accept(s.ticket(), "ASK|Do you have eggs?"));
        check(s.collecting());
        check(s.handle("go to sleep") == null);
        check(!s.active());
        s.handle("give me an easy egg recipe");
        s.accept(s.ticket(), "ASK|Do you have eggs?");
        s.handle("no");
        s.handle("find a recipe");
        check(s.prompt().contains("Do you have eggs?") && s.prompt().contains("\nno"));
        s.close();
        s.handle("I've got eggs"); s.handle("actually no tomatoes"); s.handle("find a recipe");
        check(s.prompt().contains("actually no tomatoes"));
        System.out.println("Recipe behavior checks passed");
    }
}
