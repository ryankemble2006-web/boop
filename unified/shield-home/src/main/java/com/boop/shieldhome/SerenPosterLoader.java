package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small artwork cache, independent of Kodi's authenticated image web server. */
final class SerenPosterLoader implements AutoCloseable {
    private final File directory;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final LruCache<String, Bitmap> memory = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap image) { return image.getByteCount(); }
    };
    private volatile boolean closed;
    SerenPosterLoader(Context context) {
        directory = new File(context.getCacheDir(), "seren-posters");
        directory.mkdirs();
    }
    void load(String url, ImageView view) {
        if (closed || url.isEmpty()) return;
        view.setTag(url);
        Bitmap cached = memory.get(url);
        if (cached != null) { view.setImageBitmap(cached); return; }
        workers.execute(() -> {
            Bitmap image = read(url);
            if (closed) return;
            if (image == null) {
                // A later visibility pass or feed refresh can retry a transient failure.
                main.post(() -> { if (!closed && url.equals(view.getTag())) view.setTag(null); });
                return;
            }
            memory.put(url, image);
            main.post(() -> { if (!closed && url.equals(view.getTag()) && view.isAttachedToWindow()) view.setImageBitmap(image); });
        });
    }
    private Bitmap read(String url) {
        try {
            StringBuilder name = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(url.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                name.append(String.format(java.util.Locale.ROOT, "%02x", b & 255));
            File file = new File(directory, name.toString());
            byte[] bytes;
            if (file.isFile() && file.length() < 2_000_000) bytes = Files.readAllBytes(file.toPath());
            else {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(4000); connection.setReadTimeout(5000);
                try (InputStream in = connection.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[8192]; int count;
                    while ((count = in.read(buffer)) != -1) {
                        if (closed || out.size() + count > 2_000_000) return null;
                        out.write(buffer, 0, count);
                    }
                    bytes = out.toByteArray();
                } finally { connection.disconnect(); }
            }
            BitmapFactory.Options options = new BitmapFactory.Options(); options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (options.outWidth <= 0 || options.outHeight <= 0) { file.delete(); return null; }
            options.inSampleSize = 1;
            while (options.outWidth / options.inSampleSize > 400 || options.outHeight / options.inSampleSize > 600) options.inSampleSize *= 2;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            if (bitmap != null && !file.exists()) {
                Files.write(file.toPath(), bytes);
                File[] files = directory.listFiles();
                if (files != null && files.length > 160) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    for (int i = 0; i < files.length - 160; i++) files[i].delete();
                }
            }
            return bitmap;
        } catch (Exception unavailable) { return null; }
    }
    @Override public void close() { closed = true; workers.shutdownNow(); main.removeCallbacksAndMessages(null); }
}
