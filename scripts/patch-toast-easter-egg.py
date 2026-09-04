from pathlib import Path


path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = path.read_text(encoding='utf-8')

field_anchor = '    private BoopVoiceController voiceController;\n'
field_line = '    private final BoopToastEgg toastEgg = new BoopToastEgg();\n'
if field_line not in text:
    if field_anchor not in text:
        raise SystemExit('toast patch: voice controller field anchor not found')
    text = text.replace(field_anchor, field_anchor + field_line, 1)

handler_anchor = '        if (!tokenStore.hasConnection()) {\n'
toast_block = '''        if (toastEgg.matches(transcript)) {
            BoopToastEgg.Moment toastMoment = toastEgg.next();
            wakeFaceForInteraction();
            if (face != null) {
                face.playMemberBerry(toastMoment.level());
            }
            speak(toastMoment.line());
            return;
        }

'''
if 'toastEgg.matches(transcript)' not in text:
    if handler_anchor not in text:
        raise SystemExit('toast patch: local-routing anchor not found')
    text = text.replace(handler_anchor, toast_block + handler_anchor, 1)

path.write_text(text, encoding='utf-8')
