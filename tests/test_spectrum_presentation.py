from pathlib import Path
import subprocess, tempfile
ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/'unified/shield-home/src/main/java/com/boop/shieldhome'
def test_spectrum_palette_geometry_and_lyrics_modes():
    with tempfile.TemporaryDirectory() as temp:
        check=Path(temp)/'SpectrumPresentationCheck.java'
        check.write_text("""package com.boop.shieldhome;
public class SpectrumPresentationCheck {
 static void yes(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  yes(Math.abs(SpectrumPresentation.hue(18,0,0)-42)<.01);
  yes(Math.abs(SpectrumPresentation.hue(18,1,0)-354)<.01);
  yes(Math.abs(SpectrumPresentation.hue(120,.5f,0)-120)<.01);
  yes(SpectrumPresentation.hue(18,.5f,2)!=18);
  yes(SpectrumPresentation.halfHeight(200,4,1)==96);
  yes(SpectrumPresentation.halfHeight(200,4,0)==0);
  LyricsSpectrumMode m=new LyricsSpectrumMode();yes(!m.showSpectrum());
  m.failed();yes(m.showSpectrum());m.available();yes(!m.showSpectrum());
  m.chooseSpectrum(true);m.available();yes(m.showSpectrum());
  m.newTrack();yes(m.showSpectrum());m.chooseSpectrum(false);yes(!m.showSpectrum());
  m.failed();yes(m.showSpectrum());m.newTrack();yes(!m.showSpectrum());
  System.out.println("PASS spectrum palette geometry and lyrics selection");
 }
}""")
        sources=[SRC/'SpectrumPresentation.java',SRC/'LyricsSpectrumMode.java']
        assert all(p.exists() for p in sources), 'Spectrum presentation and lyrics mode are not implemented'
        subprocess.run(['javac','-d',temp,*map(str,sources),str(check)],check=True)
        subprocess.run(['java','-cp',temp,'com.boop.shieldhome.SpectrumPresentationCheck'],check=True)
