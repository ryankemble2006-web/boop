package com.boop.shieldhome;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** Keeps Seren's opaque episode URL intact; never reconstructs an episode from a title. */
final class SerenEpisode {
    final String title, detail, file, poster;
    SerenEpisode(String title, String detail, String file, String poster) {
        this.title = title; this.detail = detail; this.file = file; this.poster = poster;
    }

    static JSONObject directoryParams() throws org.json.JSONException {
        return new JSONObject().put("directory", "plugin://plugin.video.seren/?action=showsNextUp")
                .put("media", "video").put("properties", new JSONArray()
                        .put("title").put("showtitle").put("season").put("episode").put("art").put("thumbnail"));
    }

    static List<SerenEpisode> parse(JSONObject result) throws IOException {
        JSONArray files = result.optJSONArray("files");
        if (files == null) throw new IOException("Kodi did not return the Next Up list");
        ArrayList<SerenEpisode> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < files.length() && out.size() < 200; i++) {
            JSONObject item = files.optJSONObject(i);
            if (item == null || !"file".equals(item.optString("filetype"))) continue;
            String path = item.optString("file");
            if (!playable(path) || !seen.add(path)) continue;
            String label = clean(item.optString("label", item.optString("title")));
            String show = clean(item.optString("showtitle"));
            if (show.isEmpty()) show = label;
            String detail = label.startsWith(show + ":") ? label.substring(show.length() + 1).trim() : label;
            if (detail.isEmpty()) detail = String.format(Locale.ROOT, "S%02d E%02d",
                    item.optInt("season"), item.optInt("episode"));
            JSONObject art = item.optJSONObject("art");
            String poster = art == null ? "" : artwork(art.optString("tvshow.poster"));
            if (poster.isEmpty() && art != null) poster = artwork(art.optString("poster"));
            if (poster.isEmpty()) poster = artwork(item.optString("thumbnail"));
            out.add(new SerenEpisode(show, detail, path, poster));
        }
        if (files.length() > 0 && out.isEmpty()) throw new IOException("Kodi returned no usable Next Up entries");
        return List.copyOf(out);
    }

    static JSONObject playbackParams(SerenEpisode episode) throws IOException, org.json.JSONException {
        if (episode == null || !playable(episode.file)) throw new IOException("This episode cannot be played");
        // Seren handles partial episodes itself; skip its optional resume question.
        String file = episode.file + (episode.file.contains("forceresumeon=") ? "" : "&forceresumeon=true");
        return new JSONObject().put("item", new JSONObject().put("file", file));
    }

    static boolean playable(String value) {
        try {
            URI uri = new URI(value);
            if (!"plugin".equals(uri.getScheme()) || !"plugin.video.seren".equals(uri.getRawAuthority())
                    || !("/".equals(uri.getPath()) || "".equals(uri.getPath())) || uri.getFragment() != null) return false;
            String action = null, args = null;
            for (String part : uri.getRawQuery().split("&")) {
                int split = part.indexOf('=');
                if (split < 0) continue;
                String key = part.substring(0, split);
                if ("action".equals(key)) { if (action != null) return false; action = part.substring(split + 1); }
                if ("action_args".equals(key)) args = part.substring(split + 1);
            }
            return "getSources".equals(action) && args != null && !args.isEmpty();
        } catch (Exception invalid) { return false; }
    }

    static String artwork(String value) {
        try {
            if (value.startsWith("image://")) {
                value = value.substring(8);
                if (value.endsWith("/")) value = value.substring(0, value.length() - 1);
                value = URLDecoder.decode(value.replace("+", "%2B"), "UTF-8");
            }
            URI uri = new URI(value);
            if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) return "";
            return value;
        } catch (Exception invalid) { return ""; }
    }

    private static String clean(String text) {
        return text.replaceAll("(?i)\\[/?(?:COLOR(?: [^\\]]*)?|B|I|LIGHT|UPPERCASE|LOWERCASE)\\]", "").trim();
    }
}
