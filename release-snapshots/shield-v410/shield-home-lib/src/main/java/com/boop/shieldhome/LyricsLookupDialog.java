package com.boop.shieldhome;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.function.Consumer;

/** Edits search metadata only. Does not own or control the media session. */
final class LyricsLookupDialog extends Dialog {
    private final Activity activity;
    private final NowPlayingSnapshot original;
    private final String message;
    private final Consumer<NowPlayingSnapshot> search;
    private EditText title, artist;
    private boolean keyboardShowing = false;

    LyricsLookupDialog(Activity activity, NowPlayingSnapshot track, String message,
            Consumer<NowPlayingSnapshot> search) {
        super(activity);
        this.activity = activity;
        this.original = track;
        this.message = message;
        this.search = search;
    }

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout panel = new LinearLayout(getContext());
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(18), dp(24), dp(18));

        panel.addView(label("Lyrics lookup", 25), wrap());
        TextView help = label(message.isEmpty()
                ? "Edit the title or artist, then Search."
                : message + " Edit the title or artist and try again.", 15);
        help.setTextColor(Color.LTGRAY);
        panel.addView(help, wrap());

        // Track Title label
        LinearLayout.LayoutParams titleLabelParams = wrap();
        titleLabelParams.topMargin = dp(12);
        panel.addView(label("Track title", 16), titleLabelParams);

        // Track Title Row: [ title (weight 1) ] [ ⌫ Del ] [ Clean Suffix ]
        LinearLayout titleRow = new LinearLayout(getContext());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleRowParams = wrap();
        titleRowParams.topMargin = dp(4);

        title = field(original.title());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
        titleRow.addView(title, titleParams);

        TextView titleDel = miniButton("⌫ Del", v -> deleteLastChar(title));
        titleDel.setOnLongClickListener(v -> {
            deleteLastWord(title);
            return true;
        });
        LinearLayout.LayoutParams delParams = new LinearLayout.LayoutParams(dp(76), dp(44));
        delParams.leftMargin = dp(8);
        titleRow.addView(titleDel, delParams);

        TextView titleClean = miniButton("Clean Suffix", v -> cleanSuffix(title));
        LinearLayout.LayoutParams cleanParams = new LinearLayout.LayoutParams(dp(118), dp(44));
        cleanParams.leftMargin = dp(8);
        titleRow.addView(titleClean, cleanParams);

        panel.addView(titleRow, titleRowParams);

        // Artist label
        LinearLayout.LayoutParams artistLabelParams = wrap();
        artistLabelParams.topMargin = dp(12);
        panel.addView(label("Artist", 16), artistLabelParams);

        // Artist Row: [ artist (weight 1) ] [ ⌫ Del ] [ Clean ]
        LinearLayout artistRow = new LinearLayout(getContext());
        artistRow.setOrientation(LinearLayout.HORIZONTAL);
        artistRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams artistRowParams = wrap();
        artistRowParams.topMargin = dp(4);

        artist = field(original.subtitle());
        LinearLayout.LayoutParams artistParams = new LinearLayout.LayoutParams(0, dp(44), 1.0f);
        artistRow.addView(artist, artistParams);

        TextView artistDel = miniButton("⌫ Del", v -> deleteLastChar(artist));
        artistDel.setOnLongClickListener(v -> {
            deleteLastWord(artist);
            return true;
        });
        LinearLayout.LayoutParams aDelParams = new LinearLayout.LayoutParams(dp(76), dp(44));
        aDelParams.leftMargin = dp(8);
        artistRow.addView(artistDel, aDelParams);

        TextView artistClean = miniButton("Clean", v -> cleanSuffix(artist));
        LinearLayout.LayoutParams aCleanParams = new LinearLayout.LayoutParams(dp(86), dp(44));
        aCleanParams.leftMargin = dp(8);
        artistRow.addView(artistClean, aCleanParams);

        panel.addView(artistRow, artistRowParams);

        title.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        artist.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        title.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_NEXT) {
                artist.requestFocus();
                requestKeyboard(artist);
                return true;
            }
            return false;
        });

        artist.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) {
                submit();
                return true;
            }
            return false;
        });

        LinearLayout actions = new LinearLayout(getContext());
        actions.setGravity(Gravity.END);
        TextView cancel = button("Cancel", v -> dismiss());
        TextView find = button("Search", v -> submit());

        actions.addView(cancel, new LinearLayout.LayoutParams(dp(112), dp(44)));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(dp(112), dp(44));
        searchParams.leftMargin = dp(12);
        actions.addView(find, searchParams);

        LinearLayout.LayoutParams actionParams = wrap();
        actionParams.topMargin = dp(16);
        panel.addView(actions, actionParams);

        // Directional focus mapping
        title.setNextFocusRightId(titleDel.getId());
        title.setNextFocusDownId(artist.getId());

        titleDel.setNextFocusLeftId(title.getId());
        titleDel.setNextFocusRightId(titleClean.getId());
        titleDel.setNextFocusDownId(artistDel.getId());

        titleClean.setNextFocusLeftId(titleDel.getId());
        titleClean.setNextFocusDownId(artistClean.getId());

        artist.setNextFocusUpId(title.getId());
        artist.setNextFocusRightId(artistDel.getId());
        artist.setNextFocusDownId(cancel.getId());

        artistDel.setNextFocusUpId(titleDel.getId());
        artistDel.setNextFocusLeftId(artist.getId());
        artistDel.setNextFocusRightId(artistClean.getId());
        artistDel.setNextFocusDownId(find.getId());

        artistClean.setNextFocusUpId(titleClean.getId());
        artistClean.setNextFocusLeftId(artistDel.getId());
        artistClean.setNextFocusDownId(find.getId());

        cancel.setNextFocusUpId(artist.getId());
        cancel.setNextFocusRightId(find.getId());

        find.setNextFocusUpId(artistClean.getId());
        find.setNextFocusLeftId(cancel.getId());

        title.setOnKeyListener((v, key, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (key == KeyEvent.KEYCODE_DPAD_CENTER
                        || key == KeyEvent.KEYCODE_ENTER
                        || key == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    requestKeyboard(title);
                    return true;
                }
            }
            return false;
        });

        artist.setOnKeyListener((v, key, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (key == KeyEvent.KEYCODE_DPAD_CENTER
                        || key == KeyEvent.KEYCODE_ENTER
                        || key == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    requestKeyboard(artist);
                    return true;
                }
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
            surface.setColor(Color.rgb(24, 24, 24));
            surface.setCornerRadius(dp(14));
            window.setBackgroundDrawable(surface);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(.65f);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            window.getDecorView().setOnApplyWindowInsetsListener((v, insets) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    keyboardShowing = insets.isVisible(WindowInsets.Type.ime());
                }
                return v.onApplyWindowInsets(insets);
            });

            window.getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                Rect r = new Rect();
                window.getDecorView().getWindowVisibleDisplayFrame(r);
                int height = window.getDecorView().getRootView().getHeight();
                if (height > 0) {
                    int keypadHeight = height - r.bottom;
                    if (keypadHeight > height * 0.15) {
                        keyboardShowing = true;
                    } else if (keypadHeight < dp(50)) {
                        keyboardShowing = false;
                    }
                }
            });
        }

        title.requestFocus();
        title.setSelection(title.length());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    Math.min(dp(680), activity.getResources().getDisplayMetrics().widthPixels - dp(48)),
                    Math.min(dp(410), activity.getResources().getDisplayMetrics().heightPixels - dp(48)));
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isKeyboardVisible()) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    hideKeyboard();
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isKeyboardVisible()) {
            hideKeyboard();
            return;
        }
        super.onBackPressed();
    }

    private boolean isKeyboardVisible() {
        Window window = getWindow();
        if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsets insets = window.getDecorView().getRootWindowInsets();
            if (insets != null) {
                return insets.isVisible(WindowInsets.Type.ime());
            }
        }
        return keyboardShowing;
    }

    private void submit() {
        String t = title.getText().toString().trim(), a = artist.getText().toString().trim();
        if (t.isEmpty()) {
            title.setError("Enter a track title");
            title.requestFocus();
            requestKeyboard(title);
            return;
        }
        if (a.isEmpty()) {
            artist.setError("Enter an artist");
            artist.requestFocus();
            requestKeyboard(artist);
            return;
        }
        NowPlayingSnapshot query = LyricsLookupQuery.edit(original, t, a);
        hideKeyboard();
        dismiss();
        search.accept(query);
    }

    private void hideKeyboard() {
        keyboardShowing = false;
        InputMethodManager keyboard = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) {
            View focused = getCurrentFocus();
            IBinder token = focused != null ? focused.getWindowToken()
                    : (title != null ? title.getWindowToken() : null);
            if (token != null) keyboard.hideSoftInputFromWindow(token, 0);
        }
    }

    private void requestKeyboard(EditText field) {
        if (field == null) return;
        keyboardShowing = true;
        field.requestFocus();
        field.setSelection(field.length());
        field.post(() -> {
            if (!isShowing() || !field.hasFocus()) return;
            InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) {
                keyboard.showSoftInput(field, InputMethodManager.SHOW_FORCED);
            }
        });
    }

    static String stripSuffix(String text) {
        if (text == null) return "";
        String s = text.trim();
        String cleaned = s;
        for (int i = 0; i < 3; i++) {
            String prev = cleaned;
            cleaned = cleaned.replaceAll("(?i)\\s*\\([^(]*\\)\\s*$", "")
                             .replaceAll("(?i)\\s*\\[[^\\]]*\\]\\s*$", "")
                             .replaceAll("(?i)\\s*-\\s*(acoustic|live|remaster|radio|edit|deluxe|version|orchestral|instrumental|bonus|mono|stereo|anniversary).*$", "")
                             .replaceAll("(?i)\\s+(feat\\.|ft\\.|featuring)\\s+.*$", "")
                             .trim();
            if (cleaned.equals(prev)) break;
        }
        return cleaned.isEmpty() ? s : cleaned;
    }

    private void cleanSuffix(EditText field) {
        if (field == null) return;
        String cur = field.getText().toString();
        String cleaned = stripSuffix(cur);
        field.setText(cleaned);
        field.setSelection(cleaned.length());
    }

    private void deleteLastChar(EditText field) {
        if (field == null) return;
        String cur = field.getText().toString();
        if (!cur.isEmpty()) {
            int sel = field.getSelectionEnd();
            if (sel > 0 && sel <= cur.length()) {
                String updated = cur.substring(0, sel - 1) + cur.substring(sel);
                field.setText(updated);
                field.setSelection(sel - 1);
            } else {
                String updated = cur.substring(0, cur.length() - 1);
                field.setText(updated);
                field.setSelection(updated.length());
            }
        }
    }

    private void deleteLastWord(EditText field) {
        if (field == null) return;
        String cur = field.getText().toString();
        int sel = field.getSelectionEnd();
        if (sel <= 0) sel = cur.length();
        if (sel > 0 && sel <= cur.length()) {
            int i = sel - 1;
            while (i > 0 && Character.isWhitespace(cur.charAt(i))) i--;
            while (i > 0 && !Character.isWhitespace(cur.charAt(i - 1))) i--;
            String updated = cur.substring(0, i) + cur.substring(sel);
            field.setText(updated);
            field.setSelection(i);
        }
    }

    private EditText field(String value) {
        EditText field = new EditText(getContext());
        field.setId(View.generateViewId());
        field.setSingleLine(true);
        field.setTextColor(Color.WHITE);
        field.setTextSize(18);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(256)});
        field.setText(value);
        field.setFocusable(true);
        field.setFocusableInTouchMode(true);
        field.setBackground(FocusChrome.filled(getContext(), Color.rgb(32, 32, 32), 8, false));
        field.setOnFocusChangeListener((v, focused) -> {
            field.setBackground(FocusChrome.filled(getContext(), Color.rgb(32, 32, 32), 8, focused));
        });
        field.setOnClickListener(v -> requestKeyboard(field));
        field.setPadding(dp(12), dp(8), dp(12), dp(8));
        return field;
    }

    private TextView miniButton(String text, View.OnClickListener action) {
        TextView view = label(text, 13);
        view.setId(View.generateViewId());
        view.setGravity(Gravity.CENTER);
        view.setFocusable(true);
        view.setClickable(true);
        view.setOnClickListener(action);
        view.setBackground(FocusChrome.filled(getContext(), Color.rgb(38, 38, 38), 8, false));
        view.setOnFocusChangeListener((v, focused) ->
                view.setBackground(FocusChrome.filled(getContext(), Color.rgb(38, 38, 38), 8, focused)));
        return view;
    }

    private TextView button(String text, View.OnClickListener action) {
        TextView view = label(text, 17);
        view.setId(View.generateViewId());
        view.setGravity(Gravity.CENTER);
        view.setFocusable(true);
        view.setClickable(true);
        view.setOnClickListener(action);
        view.setBackground(FocusChrome.filled(getContext(), Color.rgb(38, 38, 38), 8, false));
        view.setOnFocusChangeListener((v, focused) ->
                view.setBackground(FocusChrome.filled(getContext(), Color.rgb(38, 38, 38), 8, focused)));
        return view;
    }

    private TextView label(String text, int size) {
        TextView v = new TextView(getContext());
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        return v;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getContext().getResources().getDisplayMetrics().density);
    }
}
