package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class TvRoutinesView extends ScrollView {
    public interface Listener {
        void onRun(String entityId);
        void onContentLeft();
    }

    private final Listener listener;
    private final LinearLayout content;
    private final LinearLayout rows;
    private final LinkedHashMap<String, FocusCardView> cards = new LinkedHashMap<>();
    private FocusCardView statusCard;

    public TvRoutinesView(Context context, Listener listener) {
        super(context);
        if (listener == null) {
            throw new IllegalArgumentException("routines listener is required");
        }
        this.listener = listener;

        setFillViewport(true);
        setBackgroundColor(Color.BLACK);
        setClipToPadding(false);

        content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.TOP);
        content.setPadding(dp(36), dp(34), dp(44), dp(34));

        TextView heading = title("Routines", 42f);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        headingParams.bottomMargin = dp(22);
        content.addView(heading, headingParams);

        rows = new LinearLayout(context);
        rows.setOrientation(LinearLayout.VERTICAL);
        content.addView(rows, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        render(RoutinesController.ViewState.loading());
    }

    public void render(RoutinesController.ViewState state) {
        RoutinesController.ViewState safe = state == null
                ? RoutinesController.ViewState.loading()
                : state;
        List<RoutinesController.RowState> nextRows = safe.rows();
        if (nextRows.isEmpty()) {
            showStatus(statusText(safe.pageStatus()));
            return;
        }

        List<String> nextOrder = new ArrayList<>();
        for (RoutinesController.RowState row : nextRows) {
            nextOrder.add(row.routine().entityId());
        }

        List<String> currentOrder = new ArrayList<>(cards.keySet());
        boolean sameOrder = statusCard == null && currentOrder.equals(nextOrder);
        if (!sameOrder) {
            rebuildRows(nextRows);
            return;
        }

        for (RoutinesController.RowState row : nextRows) {
            FocusCardView card = cards.get(row.routine().entityId());
            if (card != null) {
                bind(card, row);
            }
        }
    }

    public View firstFocusable() {
        if (!cards.isEmpty()) {
            return cards.values().iterator().next();
        }
        return statusCard;
    }

    private void rebuildRows(List<RoutinesController.RowState> stateRows) {
        rows.removeAllViews();
        cards.clear();
        statusCard = null;

        for (RoutinesController.RowState row : stateRows) {
            FocusCardView card = routineCard();
            bind(card, row);
            cards.put(row.routine().entityId(), card);
            rows.addView(card, cardParams());
        }
    }

    private void showStatus(String text) {
        cards.clear();
        if (statusCard == null || statusCard.getParent() != rows) {
            rows.removeAllViews();
            statusCard = statusCard(text);
            rows.addView(statusCard, cardParams());
        } else {
            statusCard.label(text);
        }
    }

    private FocusCardView routineCard() {
        FocusCardView card = new FocusCardView(getContext());
        card.setSingleLine(false);
        card.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                listener.onContentLeft();
                return true;
            }
            return false;
        });
        return card;
    }

    private void bind(FocusCardView card, RoutinesController.RowState row) {
        card.setText(cardText(row));
        card.setOnClickListener(view -> {
            if (row.enabled()) {
                listener.onRun(row.routine().entityId());
            }
        });
    }

    private CharSequence cardText(RoutinesController.RowState row) {
        String secondLine;
        if (row.status() == RoutinesController.RowStatus.RUNNING) {
            secondLine = "Running…";
        } else if (row.status() == RoutinesController.RowStatus.DONE) {
            secondLine = "Done";
        } else if (row.status() == RoutinesController.RowStatus.FAILED) {
            secondLine = "Didn’t run";
        } else {
            secondLine = row.routine().typeLabel();
        }

        String text = row.routine().displayName() + "\n" + secondLine;
        SpannableString styled = new SpannableString(text);
        int start = row.routine().displayName().length() + 1;
        styled.setSpan(
                new RelativeSizeSpan(0.72f),
                start,
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styled.setSpan(
                new StyleSpan(Typeface.NORMAL),
                start,
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styled;
    }

    private FocusCardView statusCard(String text) {
        FocusCardView card = new FocusCardView(getContext()).label(text);
        card.setClickable(false);
        card.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                listener.onContentLeft();
                return true;
            }
            return false;
        });
        return card;
    }

    private static String statusText(RoutinesController.PageStatus status) {
        if (status == RoutinesController.PageStatus.OFFLINE) {
            return "Routines unavailable right now";
        }
        if (status == RoutinesController.PageStatus.LIVE) {
            return "No routines found";
        }
        return "Finding routines…";
    }

    private TextView title(String text, float size) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.START);
        return view;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        return params;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
