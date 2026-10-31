package com.boop.shieldhome;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.json.JSONObject;

/** An invalid or failed refresh must not erase the last useful offline row. */
final class SerenNextUpCache {
    private final File file;
    SerenNextUpCache(File file) { this.file = file; }

    synchronized List<SerenEpisode> load() {
        try {
            if (!file.isFile() || file.length() > 4_000_000) return List.of();
            return SerenEpisode.parse(new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)));
        } catch (Exception invalid) { return List.of(); }
    }

    synchronized List<SerenEpisode> accept(JSONObject result) throws IOException {
        List<SerenEpisode> parsed = SerenEpisode.parse(result);
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) throw new IOException("Cannot save Next Up");
        File temp = new File(file.getPath() + ".tmp");
        Files.write(temp.toPath(), result.toString().getBytes(StandardCharsets.UTF_8));
        Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return parsed;
    }
}
