package uk.local.casualty;

final class UiPolicy {
    static boolean isProfileChooser(CharSequence description) {
        return description != null && description.toString().startsWith("Select who's watching");
    }

    static boolean isExistingProfile(String viewId, CharSequence text) {
        if (viewId == null || !viewId.startsWith("avatar-") || viewId.startsWith("avatar-add-")) return false;
        if (text == null) return false;
        String label = text.toString().trim();
        return !label.isEmpty() && !label.equalsIgnoreCase("Add child") && !label.equalsIgnoreCase("Add adult");
    }

    static boolean isEpisodeRow(String viewId) {
        return "programme-grid:row_0".equals(viewId);
    }

    static boolean isEpisodeCard(String viewId, CharSequence description) {
        if (viewId == null || viewId.isEmpty() || viewId.startsWith("programme-grid")) return false;
        return description != null && !description.toString().trim().isEmpty();
    }

    static boolean isSkipTrailer(CharSequence text, CharSequence description) {
        String label = text != null ? text.toString().trim() : null;
        String spoken = description != null ? description.toString().trim() : null;
        return "Skip trailer".equals(label) || "Skip trailer".equals(spoken);
    }

    private UiPolicy() {}
}