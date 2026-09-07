#!/usr/bin/env bash
# CI-only observations; never changes a setting or relaxes the startup assertion.
set -u
if [ "$(adb get-serialno 2>/dev/null)" != emulator-5554 ]; then
  echo 'Wake diagnostics require the disposable CI emulator.'
  exit 1
fi
mkdir -p wake-diagnostics
cp wake-activation.log wake-diagnostics/activation.log 2>/dev/null || true
adb logcat -d -v threadtime -s BOOP-Wake:V AndroidRuntime:E RecognitionService:V SpeechRecognizer:V RemoteSpeechRecognitionService:V SpeechRecognitionManagerService:V AudioRecord:V '*:S' > wake-diagnostics/runtime.log 2>&1 || true
{
  echo '=== current BOOP process ==='
  adb shell pidof com.boop.alpha1 || true
  echo '=== current foreground activity ==='
  adb shell dumpsys activity activities | grep -E 'topResumedActivity|mResumedActivity|com\.boop\.alpha1' || true
  echo '=== selected recognition component ==='
  adb shell settings get secure voice_recognition_service || true
  echo '=== speech manager state ==='
  adb shell dumpsys speech_recognition || true
  echo '=== BOOP microphone permission ==='
  adb shell dumpsys package com.boop.alpha1 | grep -E 'RECORD_AUDIO|granted=' || true
  adb shell appops get com.boop.alpha1 RECORD_AUDIO || true
  echo '=== emulator microphone privacy ==='
  adb shell dumpsys sensor_privacy || true
} > wake-diagnostics/state.txt 2>&1
cat wake-diagnostics/activation.log 2>/dev/null || true
tail -n 80 wake-diagnostics/runtime.log
cat wake-diagnostics/state.txt
