package com.boop.shieldhdrdebug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DiagnosticPanelView extends View {
    private static final String TITLE = "BOOP HDR DEBUG";
    private static final String CURRENT = "CURRENT SESSION";
    private static final String PREVIOUS = "PREVIOUS SESSION";
    private static final String DISPLAY = "DISPLAY";
    private static final String OVERLAY = "OVERLAY";
    private static final String EVENTS = "EVENT JOURNAL";

    private final DiagnosticJournal journal;
    private final Paint background = new Paint();
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint heading = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);

    private DisplaySnapshot displaySnapshot;
    private boolean eyesAttached;
    private boolean panelAttached;

    DiagnosticPanelView(Context context, DiagnosticJournal journal) {
        super(context);
        this.journal = journal;
        setBackgroundColor(Color.TRANSPARENT);
        background.setColor(0xE6000000);
        border.setColor(Color.WHITE);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(3f);
        heading.setColor(0xFFFFD54F);
        heading.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.MONOSPACE);
    }

    void update(DisplaySnapshot snapshot, boolean eyesAreAttached, boolean panelIsAttached) {
        displaySnapshot = snapshot;
        eyesAttached = eyesAreAttached;
        panelAttached = panelIsAttached;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);
        canvas.drawRect(2, 2, getWidth() - 2, getHeight() - 2, border);

        float textSize = Math.max(24f, getHeight() / 30f);
        float headingSize = textSize * 1.08f;
        float lineHeight = textSize * 1.25f;
        float x = textSize * 0.55f;
        float y = lineHeight;
        text.setTextSize(textSize);
        heading.setTextSize(headingSize);

        List<Line> lines = buildLines();
        int maxChars = Math.max(24, (int) (getWidth() / (textSize * 0.60f)));
        for (Line line : lines) {
            if (y > getHeight() - lineHeight * 0.3f) break;
            canvas.drawText(fit(line.value, maxChars), x, y, line.heading ? heading : text);
            y += lineHeight;
        }
    }

    private List<Line> buildLines() {
        DiagnosticJournal.Snapshot state = journal.snapshot();
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(TITLE, true));
        lines.add(new Line(CURRENT, true));
        lines.add(new Line(String.format(Locale.US, "session #%d   PID %d   heartbeat age %dms",
                state.session, state.pid, state.heartbeatAgeMs()), false));
        lines.add(new Line("device " + (displaySnapshot == null ? "waiting" : displaySnapshot.device), false));
        lines.add(new Line(PREVIOUS, true));
        lines.add(new Line(state.previousSession, false));
        lines.add(new Line(DISPLAY, true));
        if (displaySnapshot != null) {
            lines.add(new Line(displaySnapshot.display, false));
            lines.add(new Line(displaySnapshot.mode, false));
            lines.add(new Line(displaySnapshot.window, false));
            lines.add(new Line(displaySnapshot.hdr + "  " + displaySnapshot.luminance, false));
            lines.add(new Line(displaySnapshot.supportedModes, false));
        } else {
            lines.add(new Line("waiting for display snapshot", false));
        }
        lines.add(new Line(OVERLAY, true));
        lines.add(new Line("eyes=" + (eyesAttached ? "ATTACHED" : "DETACHED")
                + "  panel=" + (panelAttached ? "ATTACHED" : "DETACHED"), false));
        lines.add(new Line(EVENTS, true));
        for (String event : state.recentEvents(12)) {
            lines.add(new Line(event, false));
        }
        return lines;
    }

    private static String fit(String value, int maxChars) {
        if (value == null) return "";
        if (value.length() <= maxChars) return value;
        return value.substring(0, Math.max(1, maxChars - 1)) + "…";
    }

    private static final class Line {
        final String value;
        final boolean heading;

        Line(String value, boolean heading) {
            this.value = value;
            this.heading = heading;
        }
    }
}
