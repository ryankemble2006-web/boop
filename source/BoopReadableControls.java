package com.boop.alpha1;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/** Keep existing touch targets while allowing enlarged text to occupy more lines. */
final class BoopReadableControls {
    private BoopReadableControls() { }

    static void fitText(View view) {
        if (view instanceof Button || view instanceof EditText) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null && params.height > 0) {
                view.setMinimumHeight(Math.max(view.getMinimumHeight(), params.height));
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                view.setLayoutParams(params);
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) fitText(group.getChildAt(i));
        }
    }
}
