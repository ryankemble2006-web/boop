package com.boop.alpha1;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.Toast;

/** Styled system text toast: remains visible after the browser takes focus. */
final class BoopFreeChatNotice {
    private BoopFreeChatNotice() { }

    static CharSequence text(boolean copied) {
        SpannableString message = new SpannableString(copied
                ? "Question copied.\nPaste into Free Chat."
                : "Free Chat is open.\nAsk your question.");
        message.setSpan(new RelativeSizeSpan(1.5f), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.setSpan(new StyleSpan(Typeface.BOLD), 0, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return message;
    }

    static void show(Context context, boolean copied) {
        Toast.makeText(context, text(copied), Toast.LENGTH_LONG).show();
    }
}
