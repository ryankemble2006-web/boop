package com.boop.shieldhome;
/** A manual spectrum choice survives track changes and late lyric results. */
final class LyricsSpectrumMode {
 private boolean manual, failed;
 boolean showSpectrum(){return manual||failed;}
 void chooseSpectrum(boolean value){manual=value;if(!value)failed=false;}
 void available(){failed=false;}
 void failed(){failed=true;}
 void newTrack(){failed=false;}
}
