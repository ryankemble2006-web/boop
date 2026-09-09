#!/usr/bin/env python3
from pathlib import Path
import shutil

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
ASSET_SOURCE = Path("natural-voices/manifest.json")
ASSET_TARGET = Path("boop-build/BOOP-Alpha1/app/src/main/assets/boop-natural-voices/manifest.json")
MARKER = "// BOOP_NATURAL_VOICES_V70"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


if not ASSET_SOURCE.is_file():
    raise SystemExit("Natural voice manifest source is missing")
ASSET_TARGET.parent.mkdir(parents=True, exist_ok=True)
shutil.copyfile(ASSET_SOURCE, ASSET_TARGET)

text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("Natural voices already materialized")
    raise SystemExit(0)
if "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX" not in text:
    raise SystemExit("Natural voices must run after the v70 Voice Settings scroll repair")

text = replace_once(
    text,
    "    private BoopVoiceController voiceController;\n",
    """    private BoopVoiceController voiceController;
    // BOOP_NATURAL_VOICES_V70
    private BoopAndroidSpeechBackend androidSpeechBackend;
    private BoopNaturalVoiceManifest naturalVoiceManifest;
    private BoopNaturalVoicePack naturalVoicePack;
    private BoopNaturalVoiceDownloader naturalVoiceDownloader;
    private BoopNaturalSpeechBackend naturalSpeechBackend;
""",
    "natural voice fields",
)

text = replace_once(
    text,
    """        voiceController = new BoopVoiceController(this);
        tts = new TextToSpeech(this, this);
        installTtsListener();
        createRecognizer();
""",
    """        voiceController = new BoopVoiceController(this);
        try {
            naturalVoiceManifest = BoopNaturalVoiceManifest.load(this);
            naturalVoicePack = new BoopNaturalVoicePack(this, naturalVoiceManifest);
            boolean naturalPackReady = naturalVoicePack.isInstalled();
            if (naturalPackReady) {
                voiceController.onNaturalPackVerified(naturalVoiceManifest.version());
            } else {
                voiceController.setNaturalPackUsable(false);
            }
            naturalSpeechBackend = new BoopNaturalSpeechBackend(naturalVoicePack);
            naturalVoiceDownloader = new BoopNaturalVoiceDownloader(
                    new okhttp3.OkHttpClient.Builder().build(),
                    naturalVoiceManifest,
                    naturalVoicePack);
        } catch (Throwable unavailable) {
            android.util.Log.w("BOOP-NaturalVoice", "Natural voice layer unavailable; Android TTS remains active", unavailable);
            naturalVoiceManifest = null;
            naturalVoicePack = null;
            naturalVoiceDownloader = null;
            naturalSpeechBackend = null;
            voiceController.setNaturalPackUsable(false);
        }
        tts = new TextToSpeech(this, this);
        androidSpeechBackend = new BoopAndroidSpeechBackend(tts);
        createRecognizer();
""",
    "speech backend initialization",
)

text = replace_once(
    text,
    """        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);

        TextView wakeSensitivityLabel = voiceSettingLabel("Wake sensitivity", 22f, false);
""",
    """        voiceSettingsOverlay.addView(cadenceSlider, cadenceParams);

        addNaturalVoiceSettings();

        TextView wakeSensitivityLabel = voiceSettingLabel("Wake sensitivity", 22f, false);
""",
    "Voice Settings natural section",
)

helper = r'''    private void addNaturalVoiceSettings() {
        TextView naturalTitle = new TextView(this);
        naturalTitle.setText("Natural voices");
        naturalTitle.setTextColor(Color.WHITE);
        naturalTitle.setTextSize(22f);
        naturalTitle.setGravity(Gravity.CENTER);
        naturalTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams naturalTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        naturalTitleParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalTitle, naturalTitleParams);

        TextView naturalStatus = new TextView(this);
        naturalStatus.setTextColor(Color.LTGRAY);
        naturalStatus.setTextSize(16f);
        naturalStatus.setGravity(Gravity.CENTER);
        naturalStatus.setPadding(0, 0, 0, dp(10));
        voiceSettingsOverlay.addView(naturalStatus);

        Button naturalDownload = new Button(this);
        naturalDownload.setText("Download natural voices");
        naturalDownload.setTextSize(18f);
        naturalDownload.setTextColor(Color.WHITE);
        naturalDownload.setBackgroundColor(Color.rgb(42, 42, 42));
        naturalDownload.setContentDescription("Download natural voices for offline use");
        LinearLayout.LayoutParams naturalButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(60));
        naturalButtonParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalDownload, naturalButtonParams);

        Button naturalCancel = new Button(this);
        naturalCancel.setText("Cancel download");
        naturalCancel.setTextSize(17f);
        naturalCancel.setTextColor(Color.WHITE);
        naturalCancel.setBackgroundColor(Color.rgb(42, 42, 42));
        naturalCancel.setContentDescription("Cancel natural voice download");
        naturalCancel.setVisibility(View.GONE);
        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56));
        cancelParams.setMargins(0, 0, 0, dp(8));
        voiceSettingsOverlay.addView(naturalCancel, cancelParams);

        Button emma = new Button(this);
        emma.setText("Emma");
        prepareNaturalVoiceButton(emma, "bf_emma", "Hello. I'm Emma.", naturalStatus);
        voiceSettingsOverlay.addView(emma, naturalVoiceButtonParams());

        Button isabella = new Button(this);
        isabella.setText("Isabella");
        prepareNaturalVoiceButton(isabella, "bf_isabella", "Hello. I'm Isabella.", naturalStatus);
        voiceSettingsOverlay.addView(isabella, naturalVoiceButtonParams());

        Button george = new Button(this);
        george.setText("George");
        prepareNaturalVoiceButton(george, "bm_george", "Hello. I'm George.", naturalStatus);
        voiceSettingsOverlay.addView(george, naturalVoiceButtonParams());

        Button fable = new Button(this);
        fable.setText("Fable");
        prepareNaturalVoiceButton(fable, "bm_fable", "Hello. I'm Fable.", naturalStatus);
        voiceSettingsOverlay.addView(fable, naturalVoiceButtonParams());

        Button[] naturalChoices = {emma, isabella, george, fable};
        boolean installed = naturalVoicePack != null && naturalVoicePack.isInstalled();
        if (voiceController != null) voiceController.setNaturalPackUsable(installed);
        setNaturalVoiceChoicesVisible(naturalChoices, installed);
        naturalDownload.setVisibility(installed ? View.GONE : View.VISIBLE);
        naturalStatus.setText(installed
                ? "Downloaded. Pick a voice below."
                : "Optional. Downloads once, then speaks locally on this device.");

        naturalCancel.setOnClickListener(v -> {
            if (naturalVoiceDownloader != null) naturalVoiceDownloader.cancel();
        });

        naturalDownload.setOnClickListener(v -> {
            if (naturalVoiceDownloader == null || naturalVoiceManifest == null || naturalVoicePack == null) {
                naturalStatus.setText("Natural voices aren't available on this device.");
                return;
            }
            naturalDownload.setEnabled(false);
            naturalCancel.setVisibility(View.VISIBLE);
            boolean started = naturalVoiceDownloader.start(new BoopNaturalVoiceDownloader.Listener() {
                @Override
                public void onStatus(String status) {
                    runOnUiThread(() -> naturalStatus.setText(status));
                }

                @Override
                public void onProgress(long downloadedBytes, long totalBytes) {
                    long safeTotal = Math.max(1L, totalBytes);
                    int percent = (int) Math.max(0L, Math.min(100L, downloadedBytes * 100L / safeTotal));
                    runOnUiThread(() -> naturalStatus.setText("Downloading natural voices… " + percent + "%"));
                }

                @Override
                public void onReady() {
                    runOnUiThread(() -> {
                        if (voiceController != null && naturalVoiceManifest != null) {
                            voiceController.onNaturalPackVerified(naturalVoiceManifest.version());
                            voiceController.setNaturalPackUsable(true);
                        }
                        naturalStatus.setText("Natural voices ready. Pick one below.");
                        naturalDownload.setVisibility(View.GONE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, true);
                    });
                }

                @Override
                public void onCancelled() {
                    runOnUiThread(() -> {
                        naturalStatus.setText("Download cancelled. Android voice is unchanged.");
                        naturalDownload.setEnabled(true);
                        naturalDownload.setVisibility(View.VISIBLE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, false);
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        naturalStatus.setText(message);
                        naturalDownload.setEnabled(true);
                        naturalDownload.setVisibility(View.VISIBLE);
                        naturalCancel.setVisibility(View.GONE);
                        setNaturalVoiceChoicesVisible(naturalChoices, false);
                        if (voiceController != null) voiceController.setNaturalPackUsable(false);
                    });
                }
            });
            if (!started) {
                naturalDownload.setEnabled(true);
                naturalCancel.setVisibility(View.GONE);
            }
        });
    }

    private LinearLayout.LayoutParams naturalVoiceButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58));
        params.setMargins(0, 0, 0, dp(6));
        return params;
    }

    private void prepareNaturalVoiceButton(Button button, String key, String preview, TextView naturalStatus) {
        button.setTextSize(18f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(42, 42, 42));
        button.setVisibility(View.GONE);
        button.setOnClickListener(v -> {
            if (voiceController == null || !voiceController.selectNaturalVoice(key)) return;
            naturalStatus.setText("Selected: " + button.getText());
            previewNaturalVoice(preview);
        });
    }

    private void previewNaturalVoice(String text) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (voiceController == null
                || !voiceController.naturalBackendSelectedAndUsable()
                || naturalSpeechBackend == null) {
            finishTtsUtterance();
            android.widget.Toast.makeText(
                    this,
                    "Natural voice isn't ready.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        BoopVoiceController.NaturalVoice voice = voiceController.selectedNaturalVoice();
        boolean started = naturalSpeechBackend.speak(
                text,
                voice.sid(),
                voiceController.pitch(),
                voiceController.speechRate(),
                new BoopSpeechBackend.Callback() {
                    @Override
                    public void onDone() {
                        runOnUiThread(() -> finishTtsUtterance());
                    }

                    @Override
                    public void onError(Throwable error) {
                        android.util.Log.w(
                                "BOOP-NaturalVoice",
                                "Natural voice preview failed; not substituting Android TTS",
                                error);
                        runOnUiThread(() -> {
                            finishTtsUtterance();
                            android.widget.Toast.makeText(
                                    MainActivity.this,
                                    "Natural voice preview failed.",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                });
        if (!started) {
            finishTtsUtterance();
            android.widget.Toast.makeText(
                    this,
                    "Natural voice preview failed.",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void setNaturalVoiceChoicesVisible(Button[] buttons, boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        for (Button button : buttons) button.setVisibility(visibility);
    }

'''
text = replace_once(
    text,
    "    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {\n",
    helper + "    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {\n",
    "natural voice settings helpers",
)

hide_start = text.find("    private void hideVoiceSettings() {")
hide_end = text.find("    private int dp(", hide_start)
if hide_start < 0 or hide_end < 0:
    raise SystemExit("Voice Settings dismissal bounds not found")
hide = text[hide_start:hide_end]
hide = replace_once(
    hide,
    """        if (!voiceSettingsOpen) {
            return;
        }
""",
    """        if (!voiceSettingsOpen) {
            return;
        }
        if (naturalVoiceDownloader != null && naturalVoiceDownloader.isRunning()) {
            naturalVoiceDownloader.cancel();
        }
""",
    "Voice Settings download cancellation",
)
text = text[:hide_start] + hide + text[hide_end:]

text = replace_once(
    text,
    """        if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
""",
    """        if (naturalSpeechBackend != null) {
            naturalSpeechBackend.stop();
        }
        if (androidSpeechBackend != null) {
            androidSpeechBackend.stop();
        } else if (tts != null) {
            tts.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
""",
    "speech stop before recognition",
)

old_speak = '''    private void speak(String text) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (ttsReady && tts != null) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
            if (result == TextToSpeech.ERROR) {
                finishTtsUtterance();
            }
        } else {
            finishTtsUtterance();
        }
    }

'''
new_speak = '''    private void speak(String text) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (voiceController != null
                && voiceController.naturalBackendSelectedAndUsable()
                && naturalSpeechBackend != null) {
            BoopVoiceController.NaturalVoice voice = voiceController.selectedNaturalVoice();
            boolean started = naturalSpeechBackend.speak(
                    text,
                    voice.sid(),
                    voiceController.pitch(),
                    voiceController.speechRate(),
                    new BoopSpeechBackend.Callback() {
                        @Override
                        public void onDone() {
                            runOnUiThread(() -> finishTtsUtterance());
                        }

                        @Override
                        public void onError(Throwable error) {
                            android.util.Log.w(
                                    "BOOP-NaturalVoice",
                                    "Natural voice failed; using Android TTS for the same utterance",
                                    error);
                            runOnUiThread(() -> speakWithAndroidTts(text));
                        }
                    });
            if (started) return;
        }
        speakWithAndroidTts(text);
    }

    private void speakWithAndroidTts(String text) {
        if (ttsReady && androidSpeechBackend != null && voiceController != null) {
            boolean started = androidSpeechBackend.speak(
                    text,
                    -1,
                    voiceController.pitch(),
                    voiceController.speechRate(),
                    new BoopSpeechBackend.Callback() {
                        @Override
                        public void onDone() {
                            runOnUiThread(() -> finishTtsUtterance());
                        }

                        @Override
                        public void onError(Throwable error) {
                            runOnUiThread(() -> finishTtsUtterance());
                        }
                    });
            if (started) return;
        }
        finishTtsUtterance();
    }

'''
text = replace_once(text, old_speak, new_speak, "single-mouth speech routing")

on_destroy_anchor = '''        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        voiceController = null;
'''
on_destroy_replacement = '''        if (naturalVoiceDownloader != null) {
            naturalVoiceDownloader.cancel();
            naturalVoiceDownloader = null;
        }
        if (naturalSpeechBackend != null) {
            naturalSpeechBackend.release();
            naturalSpeechBackend = null;
        }
        if (androidSpeechBackend != null) {
            androidSpeechBackend.release();
            androidSpeechBackend = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        naturalVoicePack = null;
        naturalVoiceManifest = null;
        voiceController = null;
'''
text = replace_once(text, on_destroy_anchor, on_destroy_replacement, "speech backend shutdown")

MAIN.write_text(text, encoding="utf-8")
print("Natural voice download, selection and local speech routing materialized")