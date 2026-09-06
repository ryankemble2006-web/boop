import com.boop.shieldoverlay.MediaPuppetMotion;

public final class ExportMotion {
    private static final long STEP_MS = 40L;

    private ExportMotion() {}

    public static void main(String[] args) {
        StringBuilder json = new StringBuilder(50000);
        json.append('{');
        json.append("\"stepMs\":").append(STEP_MS).append(',');
        json.append("\"musicPeriodMs\":").append(MediaPuppetMotion.MUSIC_PERIOD_MS).append(',');
        json.append("\"cinemaPeriodMs\":").append(MediaPuppetMotion.CINEMA_PERIOD_MS).append(',');
        appendSamples(json, "music", MediaPuppetMotion.MUSIC_PERIOD_MS, true);
        json.append(',');
        appendSamples(json, "cinema", MediaPuppetMotion.CINEMA_PERIOD_MS, false);
        json.append('}');
        System.out.println(json);
    }

    private static void appendSamples(
            StringBuilder json, String name, long periodMs, boolean music) {
        json.append('\"').append(name).append("\":[");
        for (long elapsedMs = 0; elapsedMs < periodMs; elapsedMs += STEP_MS) {
            if (elapsedMs > 0) {
                json.append(',');
            }
            MediaPuppetMotion.Pose pose =
                    music
                            ? MediaPuppetMotion.music(elapsedMs)
                            : MediaPuppetMotion.cinema(elapsedMs);
            appendPose(json, pose);
        }
        json.append(']');
    }

    private static void appendPose(StringBuilder json, MediaPuppetMotion.Pose pose) {
        json.append('[')
                .append(Float.toString(pose.x))
                .append(',')
                .append(Float.toString(pose.y))
                .append(',')
                .append(Float.toString(pose.rotationDegrees))
                .append(',')
                .append(Float.toString(pose.kernelAlpha))
                .append(']');
    }
}
