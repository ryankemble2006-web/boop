package com.boop.shieldhome;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/** Edits search metadata only. Does not own or control the media session. */
final class LyricsLookupDialog extends Dialog {
    private final Activity activity;
    private final NowPlayingSnapshot original;
    private final String message;
    private final Consumer<NowPlayingSnapshot> search;
    private final List<TextView> firstKeyboardRow = new ArrayList<>();
    private final List<TextView> lastKeyboardRow = new ArrayList<>();
    private EditText title, artist, activeField;
    private TextView firstKeyboardKey, lastKeyboardKey, shiftKey, find;
    private boolean shiftEnabled = true;

    LyricsLookupDialog(Activity activity, NowPlayingSnapshot track, String message,
            Consumer<NowPlayingSnapshot> search) {
        super(activity);
        this.activity = activity;
        this.original = track;
        this.message = message;
        this.search = search;
    }

    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(18), dp(24), dp(18));
        panel.addView(label("Lyrics lookup", 25), wrap());

        TextView help = label(message.isEmpty()
                ? "Edit the title or artist, then Search."
                : message + " Edit the title or artist and try again.", 16);
        help.setTextColor(Color.LTGRAY);
        panel.addView(help, wrap());

        panel.addView(label("Track title", 16), wrap());
        title = field(original.title());
        panel.addView(title, wrap());
        panel.addView(label("Artist", 16), wrap());
        artist = field(original.subtitle());
        panel.addView(artist, wrap());
        activeField = title;
        updateFieldStyles();

        TextView keyboardLabel = label("On-screen keyboard", 14);
        keyboardLabel.setTextColor(Color.LTGRAY);
        LinearLayout.LayoutParams keyboardLabelParams = wrap();
        keyboardLabelParams.topMargin = dp(8);
        panel.addView(keyboardLabel, keyboardLabelParams);

        LinearLayout keyboard = new LinearLayout(getContext());
        keyboard.setOrientation(LinearLayout.VERTICAL);
        addKeyboardRow(keyboard, new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"}, 48, true, false);
        addKeyboardRow(keyboard, new String[]{"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"}, 48, false, false);
        addKeyboardRow(keyboard, new String[]{"a", "s", "d", "f", "g", "h", "j", "k", "l"}, 48, false, false);
        addKeyboardRow(keyboard, new String[]{"⇧", "z", "x", "c", "v", "b", "n", "m", "-", "/", "⌫"},
                new int[]{56, 44, 44, 44, 44, 44, 44, 44, 42, 42, 56}, false, false);
        addKeyboardRow(keyboard, new String[]{"Space", ".", ",", "?", "!", "(", ")", "&", ":", "Clear"},
                new int[]{116, 36, 36, 36, 36, 36, 36, 36, 36, 64}, false, true);
        panel.addView(keyboard, wrap());

        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END);
        TextView cancel = button("Cancel", v -> dismiss());
        find = button("Search", v -> submit());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(112), dp(44)));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(dp(112), dp(44));
        searchParams.leftMargin = dp(12);
        actions.addView(find, searchParams);
        LinearLayout.LayoutParams actionParams = wrap();
        actionParams.topMargin = dp(14);
        panel.addView(actions, actionParams);

        title.setNextFocusDownId(artist.getId());
        artist.setNextFocusUpId(title.getId());
        artist.setNextFocusDownId(firstKeyboardKey.getId());
        for (TextView key : firstKeyboardRow) key.setNextFocusUpId(artist.getId());
        for (TextView key : lastKeyboardRow) key.setNextFocusDownId(find.getId());
        find.setNextFocusUpId(lastKeyboardKey.getId());
        cancel.setNextFocusUpId(artist.getId());
        cancel.setNextFocusRightId(find.getId());
        find.setNextFocusLeftId(cancel.getId());

        title.setOnKeyListener((v, key, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_DOWN) {
                artist.requestFocus();
                return true;
            }
            return false;
        });
        artist.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                firstKeyboardKey.requestFocus();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                title.requestFocus();
                return true;
            }
            return false;
        });

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.addView(panel);
        setContentView(scroll);
        setCanceledOnTouchOutside(true);

        Window window = getWindow();
        if (window != null) {
            GradientDrawable surface = new GradientDrawable();
            surface.setColor(BoopTvChrome.SURFACE_PANEL);
            surface.setCornerRadius(dp(16));
            surface.setStroke(dp(1), BoopTvChrome.BORDER_SUBTLE);
            window.setBackgroundDrawable(surface);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(.72f);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        title.requestFocus();
        title.setSelection(title.length());
    }

    @Override protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    Math.min(dp(620), activity.getResources().getDisplayMetrics().widthPixels - dp(48)),
                    Math.min(dp(640), activity.getResources().getDisplayMetrics().heightPixels - dp(48)));
        }
    }

    private void addKeyboardRow(LinearLayout keyboard, String[] labels, int keyWidth,
            boolean firstRow, boolean lastRow) {
        int[] widths = new int[labels.length];
        for (int i = 0; i < widths.length; i++) widths[i] = keyWidth;
        addKeyboardRow(keyboard, labels, widths, firstRow, lastRow);
    }

    private void addKeyboardRow(LinearLayout keyboard, String[] labels, int[] widths,
            boolean firstRow, boolean lastRow) {
        LinearLayout row = new LinearLayout(getContext());
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        for (int i = 0; i < labels.length; i++) {
            TextView key = keyboardKey(row, labels[i], widths[i]);
            if (firstKeyboardKey == null) firstKeyboardKey = key;
            if (firstRow) firstKeyboardRow.add(key);
            if (lastRow) {
                lastKeyboardRow.add(key);
                lastKeyboardKey = key;
            }
        }
        LinearLayout.LayoutParams rowParams = wrap();
        rowParams.topMargin = dp(2);
        rowParams.bottomMargin = dp(2);
        keyboard.addView(row, rowParams);
    }

    private TextView keyboardKey(LinearLayout row, String text, int width) {
        TextView key = label(text, 14);
        key.setId(View.generateViewId());
        key.setGravity(Gravity.CENTER);
        key.setFocusable(true);
        key.setClickable(true);
        key.setBackground(FocusChrome.filled(getContext(), BoopTvChrome.SURFACE_CARD, 8, false));
        key.setOnFocusChangeListener((v, focused) -> updateKeyboardKeyStyle(key, focused));
        key.setOnClickListener(v -> activateKey(text));
        if ("⇧".equals(text)) shiftKey = key;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(width), dp(44));
        params.leftMargin = dp(2);
        params.rightMargin = dp(2);
        row.addView(key, params);
        return key;
    }

    private void activateKey(String key) {
        if ("⇧".equals(key)) {
            shiftEnabled = !shiftEnabled;
            updateShiftKeyStyle();
        } else if ("⌫".equals(key)) {
            deletePreviousCharacter();
        } else if ("Clear".equals(key)) {
            EditText target = editingField();
            target.setText("");
            target.setSelection(0);
            target.setError(null);
        } else if ("Space".equals(key)) {
            insertText(" ");
        } else if (key.length() == 1 && Character.isLetter(key.charAt(0))) {
            insertText(shiftEnabled ? key.toUpperCase(Locale.ROOT) : key.toLowerCase(Locale.ROOT));
            shiftEnabled = false;
            updateShiftKeyStyle();
        } else {
            insertText(key);
        }
    }

    private void insertText(String value) {
        EditText target = editingField();
        Editable text = target.getText();
        int start = target.getSelectionStart();
        int end = target.getSelectionEnd();
        if (start < 0 || end < 0) start = end = text.length();
        int low = Math.min(start, end);
        int high = Math.max(start, end);
        text.replace(low, high, value);
        target.setSelection(low + value.length());
        target.setError(null);
    }

    private void deletePreviousCharacter() {
        EditText target = editingField();
        Editable text = target.getText();
        int start = target.getSelectionStart();
        int end = target.getSelectionEnd();
        if (start < 0 || end < 0) start = end = text.length();
        int low = Math.min(start, end);
        int high = Math.max(start, end);
        if (low == high && low > 0) low--;
        if (low < high) {
            text.delete(low, high);
            target.setSelection(low);
        }
        target.setError(null);
    }

    private EditText editingField() {
        return activeField == null ? title : activeField;
    }

    private void updateFieldStyles() {
        updateFieldStyle(title);
        updateFieldStyle(artist);
    }

    private void updateFieldStyle(EditText field) {
        if (field == null) return;
        boolean selected = field.hasFocus() || field == activeField;
        field.setBackground(FocusChrome.filled(getContext(),
                selected ? BoopTvChrome.SURFACE_ELEVATED : BoopTvChrome.SURFACE_CARD, 10, selected));
    }

    private void updateKeyboardKeyStyle(TextView key, boolean focused) {
        boolean selected = key == shiftKey && shiftEnabled;
        key.setBackground(FocusChrome.filled(getContext(),
                focused || selected ? BoopTvChrome.SURFACE_ELEVATED : BoopTvChrome.SURFACE_CARD,
                8, focused || selected));
    }

    private void updateShiftKeyStyle() {
        if (shiftKey != null) updateKeyboardKeyStyle(shiftKey, shiftKey.hasFocus());
    }

    private void submit() {
        String t = title.getText().toString().trim();
        String a = artist.getText().toString().trim();
        if (t.isEmpty()) {
            title.setError("Enter a track title");
            title.requestFocus();
            return;
        }
        if (a.isEmpty()) {
            artist.setError("Enter an artist");
            artist.requestFocus();
            return;
        }
        NowPlayingSnapshot query = LyricsLookupQuery.edit(original, t, a);
        dismiss();
        search.accept(query);
    }

    private EditText field(String value) {
        EditText field = new EditText(getContext());
        field.setId(View.generateViewId());
        field.setSingleLine(true);
        field.setTextColor(Color.WHITE);
        field.setTextSize(17);
        field.setTypeface(BoopTvChrome.FONT_REGULAR);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setShowSoftInputOnFocus(false);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        field.setText(value);
        field.setBackground(FocusChrome.filled(getContext(), BoopTvChrome.SURFACE_CARD, 10, false));
        field.setOnFocusChangeListener((v, focused) -> {
            if (focused) activeField = field;
            updateFieldStyles();
        });
        field.setPadding(dp(12), dp(8), dp(12), dp(8));
        return field;
    }

    private TextView button(String text, View.OnClickListener action) {
        TextView view = label(text, 16);
        view.setId(View.generateViewId());
        view.setGravity(Gravity.CENTER);
        view.setTypeface(BoopTvChrome.FONT_MEDIUM);
        view.setFocusable(true);
        view.setClickable(true);
        view.setOnClickListener(action);
        view.setBackground(FocusChrome.filled(getContext(), BoopTvChrome.SURFACE_CARD, 10, false));
        view.setOnFocusChangeListener((v, focused) -> view.setBackground(FocusChrome.filled(getContext(),
                focused ? BoopTvChrome.SURFACE_ELEVATED : BoopTvChrome.SURFACE_CARD, 10, focused)));
        return view;
    }

    private TextView label(String text, int size) {
        TextView v = new TextView(getContext());
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        v.setTypeface(size >= 22 ? BoopTvChrome.FONT_MEDIUM : BoopTvChrome.FONT_REGULAR);
        return v;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }
}
