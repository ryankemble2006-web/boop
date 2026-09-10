#!/usr/bin/env python3
from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "// BOOP_NATURAL_RUNTIME_DIAGNOSTICS_V89"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("v89 natural runtime diagnostics already materialized")
    raise SystemExit(0)

error_anchor = '''                            naturalStatus.setText("Natural voice " + stage + " failed. Android voice kept.");
                            android.widget.Toast.makeText(
'''
error_replacement = '''                            naturalStatus.setText("Natural voice " + stage + " failed. Android voice kept.");
                            showNaturalVoiceDevDiagnostic(error);
                            android.widget.Toast.makeText(
'''
text = replace_once(
    text,
    error_anchor,
    error_replacement,
    "natural preview diagnostic hook",
)

helper = r'''    // BOOP_NATURAL_RUNTIME_DIAGNOSTICS_V89
    private void showNaturalVoiceDevDiagnostic(Throwable error) {
        if (!BuildConfig.DEBUG || error == null || isFinishing()) return;

        String stage = "unknown";
        if (error instanceof BoopNaturalSpeechBackend.NaturalSpeechException) {
            stage = ((BoopNaturalSpeechBackend.NaturalSpeechException) error).stage();
        }

        String code = "BOOP DEV E899";
        String explanation = "Natural voice failed at an unknown stage.";
        if ("files".equals(stage)) {
            code = "BOOP DEV E890";
            explanation = "Natural voice files are incomplete.";
        } else if ("initialization".equals(stage)) {
            code = "BOOP DEV E891";
            explanation = "Kokoro could not open the voice model.";
        } else if ("synthesis".equals(stage)) {
            code = "BOOP DEV E892";
            explanation = "Kokoro opened, but speech generation failed.";
        } else if ("playback".equals(stage)) {
            code = "BOOP DEV E893";
            explanation = "Speech was generated, but Android audio playback failed.";
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(32), dp(32), dp(32), dp(32));
        panel.setBackgroundColor(Color.BLACK);

        TextView codeView = new TextView(this);
        codeView.setText(code);
        codeView.setTextColor(Color.WHITE);
        codeView.setTextSize(42f);
        codeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        codeView.setGravity(Gravity.CENTER);
        panel.addView(codeView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView explanationView = new TextView(this);
        explanationView.setText(explanation);
        explanationView.setTextColor(Color.WHITE);
        explanationView.setTextSize(25f);
        explanationView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams explanationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        explanationParams.setMargins(0, dp(18), 0, 0);
        panel.addView(explanationView, explanationParams);

        TextView detailView = new TextView(this);
        detailView.setText(naturalVoiceRootCause(error));
        detailView.setTextColor(Color.LTGRAY);
        detailView.setTextSize(18f);
        detailView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.setMargins(0, dp(18), 0, 0);
        panel.addView(detailView, detailParams);

        Button close = new Button(this);
        close.setText("Close");
        close.setTextSize(22f);
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.rgb(42, 42, 42));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        closeParams.setMargins(0, dp(28), 0, 0);
        panel.addView(close, closeParams);

        android.app.Dialog dialog = new android.app.Dialog(
                this,
                android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        dialog.setContentView(panel);
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String naturalVoiceRootCause(Throwable error) {
        Throwable root = error;
        for (int depth = 0; depth < 8 && root.getCause() != null && root.getCause() != root; depth++) {
            root = root.getCause();
        }
        String type = root.getClass().getSimpleName();
        if (type == null || type.isEmpty()) type = "Runtime failure";
        String message = root.getMessage();
        if (message == null || message.trim().isEmpty()) message = "No detail from runtime.";
        message = message.replace('\n', ' ').replace('\r', ' ').trim();
        message = message.replaceAll("https?://\\S+", "[url]");
        message = message.replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "[address]");
        message = message.replaceAll("/data/(?:user/\\d+|data)/[^/\\s]+", "[app storage]");
        if (message.length() > 180) message = message.substring(0, 180) + "…";
        return type + ": " + message;
    }

'''
text = replace_once(
    text,
    "    private void setNaturalVoiceChoicesVisible(Button[] buttons, boolean visible) {\n",
    helper + "    private void setNaturalVoiceChoicesVisible(Button[] buttons, boolean visible) {\n",
    "natural diagnostic helper insertion",
)

MAIN.write_text(text, encoding="utf-8")
print("v89 natural runtime diagnostics materialized")
