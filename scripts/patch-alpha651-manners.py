from pathlib import Path

path = Path("source/MainActivity.java")
text = path.read_text(encoding="utf-8")
marker = "BoopConversationExitIntent.replyFor(transcript)"
if marker not in text:
    old = '''        String voiceReply = voiceController.maybeChangeVoice(transcript);\n        if (voiceReply != null) {\n            speak(voiceReply);\n            return;\n        }\n\n        if (!tokenStore.hasConnection()) {\n'''
    new = '''        String voiceReply = voiceController.maybeChangeVoice(transcript);\n        if (voiceReply != null) {\n            speak(voiceReply);\n            return;\n        }\n\n        String exitReply = BoopConversationExitIntent.replyFor(transcript);\n        if (exitReply != null) {\n            speak(exitReply);\n            return;\n        }\n\n        if (!tokenStore.hasConnection()) {\n'''
    if old not in text:
        raise SystemExit("Alpha 6.5 handleRecognizedSpeech seam not found")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")
