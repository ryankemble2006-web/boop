"""Use the production resolver and existing sign wording, without Android rendering."""
from pathlib import Path
import subprocess

ROOT=Path(__file__).resolve().parents[1]

def test_exact_notification_identities_keep_existing_sign_words(tmp_path):
    probe=tmp_path/'SignIdentityProbe.java'
    probe.write_text('''package com.boop.alpha1;
import com.boop.eyes.FeltSignRig;
public class SignIdentityProbe {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  String[][] known={
   {"com.whatsapp","0","MESSAGE!"},{"com.whatsapp.w4b","0","MESSAGE!"},
   {"com.google.android.gm","1","MAIL'S HERE!"},{"com.facebook.katana","2","OVER HERE!"},
   {"com.twitter.android","3","SOMETHING NEW!"},{"boop.dev.whatsapp","0","MESSAGE!"},
   {"boop.dev.gmail","1","MAIL'S HERE!"},{"boop.dev.facebook","2","OVER HERE!"},
   {"boop.dev.x","3","SOMETHING NEW!"}};
  for(String[] row:known){int style=BoopNotificationSignIdentity.brandedStyle(row[0]);
   check(style==Integer.parseInt(row[1]),"wrong branded sign for "+row[0]);
   check(FeltSignRig.words(style).equals(row[2]),"existing sign words changed for "+row[0]);}
  for(String pkg:new String[]{null,"","org.example.gmail","com.twitter.android.fake","boop.dev.discord","com.google.android.gm.extra"})
   check(BoopNotificationSignIdentity.brandedStyle(pkg)==-1,"unrelated app must use generic label: "+pkg);
 }
}''',encoding='utf-8')
    subprocess.run(['javac','-encoding','UTF-8','-d',str(tmp_path),
                    str(ROOT/'source/BoopNotificationSignIdentity.java'),
                    str(ROOT/'unified/animation/java/com/boop/eyes/FeltSignRig.java'),str(probe)],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.alpha1.SignIdentityProbe'],check=True)
