from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, found {count}")
    return text.replace(old, new, 1)


main_path = Path("source/MainActivity.java")
main = main_path.read_text(encoding="utf-8")

main = replace_once(
    main,
    "import java.io.IOException;\nimport java.util.ArrayList;",
    "import java.io.IOException;\nimport java.time.LocalDateTime;\nimport java.util.ArrayList;",
    "LocalDateTime import",
)
main = replace_once(
    main,
    "    private boolean pendingListenAfterPermission = false;\n",
    "    private boolean pendingListenAfterPermission = false;\n"
    "    private boolean listenAfterTts = false;\n",
    "follow-up listen flag",
)
main = replace_once(
    main,
    "    private HomeAssistantDiscovery discovery;\n",
    "    private HomeAssistantDiscovery discovery;\n"
    "    private BoopTimedRoutineFlow timedRoutineFlow;\n",
    "timed flow field",
)
main = replace_once(
    main,
    "        voiceController = new BoopVoiceController(this);\n",
    "        voiceController = new BoopVoiceController(this);\n"
    "        timedRoutineFlow = new BoopTimedRoutineFlow();\n",
    "timed flow initialization",
)

old_tts = '''    private void installTtsListener() {
        if (tts == null) {
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }
        });
    }
'''
new_tts = '''    private void installTtsListener() {
        if (tts == null) {
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> finishTtsUtterance());
            }
        });
    }

    private void finishTtsUtterance() {
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (listenAfterTts) {
            listenAfterTts = false;
            beginTapToSpeak();
        }
    }
'''
main = replace_once(main, old_tts, new_tts, "TTS follow-up seam")

old_handle = '''    private void handleRecognizedSpeech(String transcript) {
        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

        String voiceReply = voiceController.maybeChangeVoice(transcript);
        if (voiceReply != null) {
            speak(voiceReply);
            return;
        }

        if (!tokenStore.hasConnection()) {
            speak("I need to connect to the house first.");
            ensureHouseConnection();
            return;
        }

        executor.execute(() -> {
            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()
                    ? HomeAssistantDeviceSetup.SetupResult.READY
                    : deviceSetup.ensureReady();
            if (setup != HomeAssistantDeviceSetup.SetupResult.READY) {
                runOnUiThread(() -> handleDeviceSetupFailure(setup));
                return;
            }

            setupFailureSpoken = false;
            CommandOutcome outcome = commandRouter.process(transcript);
            runOnUiThread(() -> {
                speak(LocalReply.forOutcome(outcome));
                if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
                    tokenStore.clear();
                    setupFailureSpoken = false;
                    ensureHouseConnection();
                }
            });
        });
    }
'''
new_handle = '''    private void handleRecognizedSpeech(String transcript) {
        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

        String voiceReply = voiceController.maybeChangeVoice(transcript);
        if (voiceReply != null) {
            speak(voiceReply);
            return;
        }

        if (!tokenStore.hasConnection()) {
            speak("I need to connect to the house first.");
            ensureHouseConnection();
            return;
        }

        BoopTimedRoutineFlow.Result timed =
                timedRoutineFlow.process(transcript, LocalDateTime.now());
        switch (timed.kind()) {
            case ASK_ONCE_OR_RECURRING:
                speakThenListen("Once or recurring?");
                return;
            case RECURRING_REQUESTED:
                speak("Recurring routines need setup first.");
                return;
            case RUN_ONCE:
                executeTimedCommand(timed);
                return;
            case NOT_TIMED:
            default:
                break;
        }

        executor.execute(() -> {
            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()
                    ? HomeAssistantDeviceSetup.SetupResult.READY
                    : deviceSetup.ensureReady();
            if (setup != HomeAssistantDeviceSetup.SetupResult.READY) {
                runOnUiThread(() -> handleDeviceSetupFailure(setup));
                return;
            }

            setupFailureSpoken = false;
            CommandOutcome outcome = commandRouter.process(transcript);
            runOnUiThread(() -> handleHouseOutcome(outcome));
        });
    }

    private void executeTimedCommand(BoopTimedRoutineFlow.Result timed) {
        executor.execute(() -> {
            HomeAssistantDeviceSetup.SetupResult setup = tokenStore.hasHaDeviceIdentity()
                    ? HomeAssistantDeviceSetup.SetupResult.READY
                    : deviceSetup.ensureReady();
            if (setup != HomeAssistantDeviceSetup.SetupResult.READY) {
                runOnUiThread(() -> handleDeviceSetupFailure(setup));
                return;
            }

            setupFailureSpoken = false;
            CommandOutcome outcome = haClient.processTimed(timed.haCommand());
            runOnUiThread(() -> handleHouseOutcome(outcome));
        });
    }

    private void handleHouseOutcome(CommandOutcome outcome) {
        speak(LocalReply.forOutcome(outcome));
        if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
            tokenStore.clear();
            setupFailureSpoken = false;
            ensureHouseConnection();
        }
    }
'''
main = replace_once(main, old_handle, new_handle, "timed speech routing")

old_speak = '''    private void speak(String text) {
        wakeFaceForInteraction();
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsStarting();
        }
        if (ttsReady && tts != null) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
            if (result == TextToSpeech.ERROR && wakeCoordinator != null) {
                wakeCoordinator.onTtsFinished();
            }
        } else if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
    }
'''
new_speak = '''    private void speakThenListen(String text) {
        listenAfterTts = true;
        speak(text);
    }

    private void speak(String text) {
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
main = replace_once(main, old_speak, new_speak, "speak-then-listen helper")
main_path.write_text(main, encoding="utf-8")


client_path = Path("source/HomeAssistantClient.java")
client = client_path.read_text(encoding="utf-8")
marker = '''    private static HomeAssistantResponse postConversation(
'''
process_timed = '''    CommandOutcome processTimed(String text) {
        String baseUrl = tokenStore.getBaseUrl();
        if (baseUrl == null) {
            return CommandOutcome.authRequired();
        }

        String deviceId = tokenStore.getHaDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            return CommandOutcome.authRequired();
        }

        try {
            String accessToken = auth.freshAccessToken();
            HomeAssistantResponse response = postConversation(
                    baseUrl, accessToken, text, deviceId);

            switch (response.kind()) {
                case ACTION_DONE:
                    if (!response.successTargets().isEmpty() && response.failedTargets().isEmpty()) {
                        return CommandOutcome.success(response.successTargets().get(0).name());
                    }
                    if (!response.failedTargets().isEmpty()) {
                        HomeAssistantResponse.Target failed = response.failedTargets().get(0);
                        if ("entity".equals(failed.type())
                                && !failed.id().isEmpty()
                                && isUnavailable(baseUrl, accessToken, failed.id())) {
                            return CommandOutcome.targetOffline(
                                    failed.name().isEmpty() ? "device" : failed.name(),
                                    homeArea);
                        }
                    }
                    return CommandOutcome.failed();
                case NO_INTENT_MATCH:
                    return CommandOutcome.noMatch();
                case NO_VALID_TARGETS:
                    return CommandOutcome.noTarget();
                case QUERY_ANSWER:
                case FAILED_TO_HANDLE:
                case UNKNOWN_ERROR:
                default:
                    return CommandOutcome.failed();
            }
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            return CommandOutcome.authRequired();
        } catch (IOException e) {
            return CommandOutcome.unreachable();
        } catch (Exception e) {
            return CommandOutcome.authRequired();
        }
    }

'''
client = replace_once(client, marker, process_timed + marker, "timed Assist-only client method")
client_path.write_text(client, encoding="utf-8")
