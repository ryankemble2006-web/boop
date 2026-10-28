package uk.local.eastenders;

public final class WatchNowPolicy {
    private WatchNowPolicy() {}

    /**
     * Determines whether the current screen is the BBC iPlayer profile selector ("Select who's watching").
     */
    public static boolean isProfileChooser(CharSequence text, CharSequence description, String viewId) {
        if (description != null) {
            String lower = description.toString().toLowerCase();
            if (lower.contains("who's watching") || lower.contains("whos watching") || lower.contains("select who")) {
                return true;
            }
        }
        if (text != null) {
            String lower = text.toString().toLowerCase();
            if (lower.contains("who's watching") || lower.contains("whos watching") || lower.contains("select who")) {
                return true;
            }
        }
        return "user-switcher".equals(viewId) || "account-layout-title".equals(viewId) || "user-switcher_container".equals(viewId);
    }

    /**
     * Determines whether a node is a valid profile avatar button to click (e.g. "Polly").
     * Excludes "Add child", "Add adult", "Language", and "Edit" buttons.
     */
    public static boolean isProfileButton(String viewId, CharSequence text, CharSequence description) {
        if (viewId != null) {
            if (viewId.startsWith("avatar-add") || viewId.startsWith("avatar-label") || viewId.startsWith("edit-") || viewId.equals("language-switcher")) {
                return false;
            }
            if (viewId.startsWith("avatar-")) {
                return true;
            }
        }
        String label = text != null ? text.toString().trim() : (description != null ? description.toString().trim() : "");
        if (label.isEmpty()) return false;
        String lower = label.toLowerCase();
        return !lower.contains("add child") && !lower.contains("add adult") && !lower.contains("language") && !lower.contains("edit") && !lower.contains("settings") && !lower.contains("back");
    }

    /**
     * Determines whether the current screen is the EastEnders programme page.
     */
    public static boolean isProgrammePage(String viewId, CharSequence text) {
        if (viewId != null && (viewId.startsWith("b006m86d") || viewId.equals("unsliced") || viewId.equals("programme-grid") || viewId.equals("b006m86d-header") || viewId.equals("b006m86d-title"))) {
            return true;
        }
        return text != null && text.toString().contains("EastEnders");
    }

    /**
     * Determines whether a node is the featured "Watch now" Hero CTA button or a playable episode card.
     */
    public static boolean isWatchNowButton(String viewId, CharSequence text, CharSequence description) {
        if (viewId != null && viewId.startsWith("cta:")) {
            return true;
        }
        String label = text != null ? text.toString().trim() : (description != null ? description.toString().trim() : "");
        if (label.isEmpty()) return false;
        String lower = label.toLowerCase();
        if (lower.contains("watchlist") || lower.contains("more like this") || lower.contains("episodes") || lower.contains("back")) {
            return false;
        }
        return lower.contains("watch now") 
                || lower.contains("resume") 
                || lower.contains("watch from start") 
                || lower.contains("play episode")
                || (lower.startsWith("play") && !lower.contains("player"));
    }

    /**
     * Determines whether a node is a "Skip trailer" / "Skip pre-roll" button.
     */
    public static boolean isSkipTrailer(String viewId, CharSequence text, CharSequence description) {
        if (viewId != null && (viewId.contains("skip-trailer") || viewId.contains("skip-preroll") || viewId.equals("skip-prerolls") || viewId.contains("skip-promo"))) {
            return true;
        }
        String label = text != null ? text.toString().trim() : (description != null ? description.toString().trim() : "");
        if (label.isEmpty()) return false;
        String lower = label.toLowerCase();
        return lower.contains("skip trailer") || lower.contains("skip pre-roll") || lower.contains("skip preroll") || lower.contains("skip promo");
    }

    /**
     * Determines whether a node is a "Skip intro" button.
     */
    public static boolean isSkipIntro(String viewId, CharSequence text, CharSequence description) {
        String label = text != null ? text.toString().trim() : (description != null ? description.toString().trim() : "");
        return !label.isEmpty() && label.toLowerCase().contains("skip intro");
    }

    /**
     * Determines whether a node indicates active playback in BBC iPlayer.
     */
    public static boolean isPlaybackIndicator(String viewId) {
        if (viewId == null) return false;
        return viewId.startsWith("playback-") 
                || viewId.equals("playback-controls") 
                || viewId.equals("playback-time") 
                || viewId.equals("playback-time-vod")
                || viewId.equals("playback-scrubbar") 
                || viewId.equals("playback-lower");
    }
}
