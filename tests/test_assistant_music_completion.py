"""Execute the real patched outcome callback without any speech-engine completion."""
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
JAVA = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')


def block(text, marker):
    start = text.index(marker)
    opening = text.index('{', start)
    end, depth = opening + 1, 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def test_accepted_music_finishes_one_shot_without_waiting_for_tts(tmp_path):
    generated = ROOT / JAVA
    if generated.exists():
        source = generated.read_text(encoding='utf-8')
    else:
        main = tmp_path / JAVA
        main.parent.mkdir(parents=True)
        main.write_text((ROOT/'source/MainActivity.java').read_text(encoding='utf-8'), encoding='utf-8')
        app = tmp_path/'boop-build/BOOP-Alpha1'
        (app/'app/src/main/AndroidManifest.xml').write_text('    </application>', encoding='utf-8')
        settings = app/'shield-lib/src/main/java/com/boop/shieldoverlay/TvSettingsView.java'
        settings.parent.mkdir(parents=True)
        settings.write_text('import android.content.Context;\n'
            'content.addView(section("VOICE")); wakeNameCard=card("BOOP\'s name",wakeName(),"Spoken wake name only. BOOP always works too.",left); wakeNameCard.setOnClickListener(v->showWakeNameDialog()); content.addView(wakeNameCard,spaced());\n', encoding='utf-8')
        subprocess.run([sys.executable, str(ROOT/'scripts/patch-unified-assistant-button.py')], cwd=tmp_path, check=True)
        source = main.read_text(encoding='utf-8')
    handler = block(source, 'private void handleRecognizedSpeech(')
    callback = block(handler[handler.index('CommandOutcome outcome ='):], 'runOnUiThread(() ->')
    body = callback[callback.index('{')+1:-1]
    finish = block(source, 'private void finishAssistantOneShot()')
    completion = block(source, 'private void finishTtsUtterance()')
    harness = r'''
package com.boop.alpha1;
public class AssistantMusicProbe {
 boolean assistantOneShot=true,assistantOneShotStarted=true,voiceSettingsOpen;
 boolean assistantFollowUpAfterTts,assistantFollowUpListening,setupFailureSpoken,sleepFaceAfterTts;
 String latestAssistantFollowUpPartial;
 enum RecognitionMode { TAP }
 RecognitionMode recognitionMode;
 int listens;
 static class Wake {void onTapStarted(){} void onTtsFinished(){}}
 Wake wakeCoordinator=new Wake();
 void startListening(){listens++;} void scheduleAssistantFollowUpSilenceTimeout(){} void sleepFaceImmediately(){}
 boolean activityInForeground=true,chatModeOpen,finishing,destroyed;
 int requestChatRevision,chatModeRevision,finished,spoken,followups;
 String transcript="music",lastSpeech="";
 enum BoopChatMode { FREE_CHAT, OPENCODE }
 BoopChatMode requestChatMode=BoopChatMode.OPENCODE;
 static class TokenStore {void clear(){}}
 TokenStore tokenStore=new TokenStore();
 boolean isFinishing(){return finishing;} boolean isDestroyed(){return destroyed;}
 void ensureHouseConnection(){} void openFreeChat(String s){}
 void finish(){finished++;finishing=true;}
 void overridePendingTransition(int a,int b){}
 void speak(String s){spoken++;lastSpeech=s;}
 void speakThenOpenAssistantFollowUp(String s){assistantFollowUpAfterTts=true;followups++;speak(s);}
 __FINISH__
 __COMPLETION__
 void deliver(CommandOutcome outcome) {__BODY__}
 static void check(boolean yes,String why){if(!yes)throw new AssertionError(why);}
 public static void main(String[] args) {
  AssistantMusicProbe a=new AssistantMusicProbe();
  a.assistantFollowUpAfterTts=true;a.assistantFollowUpListening=true;
  a.deliver(CommandOutcome.musicPlaybackAccepted());
  check(a.finished==1 && a.spoken==0,"accepted music must finish synchronously without calling speech");
  check(!a.assistantOneShot && !a.assistantOneShotStarted && !a.assistantFollowUpAfterTts && !a.assistantFollowUpListening,"completion must clear one-shot and follow-up state");
  a.finishAssistantOneShot();check(a.finished==1,"completion is idempotent");
  a=new AssistantMusicProbe();a.assistantOneShot=false;a.deliver(CommandOutcome.musicPlaybackAccepted());
  check(a.finished==0 && "Done".equals(a.lastSpeech),"normal Wall face retains its response and stays open");
  a=new AssistantMusicProbe();a.voiceSettingsOpen=true;a.deliver(CommandOutcome.musicPlaybackAccepted());
  check(a.finished==0,"late music result must not close voice settings");
  a=new AssistantMusicProbe();a.deliver(CommandOutcome.localReply("Failed"));
  check(a.finished==0 && "Failed".equals(a.lastSpeech),"failure must retain its spoken reply");
  a=new AssistantMusicProbe();a.deliver(CommandOutcome.localReply("Done"));
  check(a.finished==0 && a.spoken==1,"unrelated Done is not a music completion");
  a=new AssistantMusicProbe();a.deliver(CommandOutcome.localQuestion("Artist or song?"));
  check(a.finished==0 && a.spoken==1,"question must remain available");
  a=new AssistantMusicProbe();a.deliver(CommandOutcome.assistantReply("Which one?"));a.finishTtsUtterance();
  check(a.finished==0 && a.listens==1 && a.assistantFollowUpListening,"requested follow-up must listen after speech finishes");
  a.deliver(CommandOutcome.musicPlaybackAccepted());
  check(a.finished==1 && !a.assistantFollowUpListening,"successful answer must close the same one-shot session");
  __LIFECYCLE__
  System.out.println("Assistant music completion scenarios passed");
 }
}
'''.replace('__FINISH__', finish).replace('__BODY__', body).replace('__COMPLETION__', completion)
    lifecycle = ''
    if 'requestChatRevision != chatModeRevision' in body:
        for condition in ('activityInForeground=false', 'chatModeOpen=true', 'finishing=true', 'destroyed=true', 'chatModeRevision=1'):
            lifecycle += f'a=new AssistantMusicProbe();a.{condition};a.deliver(CommandOutcome.musicPlaybackAccepted());check(a.finished==0 && a.spoken==0,"stale result: {condition}");\n'
        lifecycle += 'a=new AssistantMusicProbe();a.deliver(CommandOutcome.localQuestion("Artist or song?"));a.finishTtsUtterance();check(a.followups==1 && a.listens==1 && a.finished==0,"music question opens spoken follow-up after TTS");'
    harness = harness.replace('__LIFECYCLE__', lifecycle)
    (tmp_path/'AssistantMusicProbe.java').write_text(harness, encoding='utf-8')
    (tmp_path/'Log.java').write_text('package android.util; public class Log {public static int i(String t,String s){return 0;}}', encoding='utf-8')
    (tmp_path/'BoopState.java').write_text('package com.boop.shared; public class BoopState {public static final BoopState INSTANCE=new BoopState();public void speech(boolean a,boolean b){}}', encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),str(ROOT/'source/CommandOutcome.java'),str(ROOT/'source/LocalReply.java'),str(tmp_path/'Log.java'),str(tmp_path/'BoopState.java'),str(tmp_path/'AssistantMusicProbe.java')], check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.alpha1.AssistantMusicProbe'], check=True)
