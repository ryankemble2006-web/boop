from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_complete_poster_windows_at_different_widths(tmp_path):
    source = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/SerenPosterLayout.java'
    assert source.exists(), 'Whole-poster viewport sizing is missing'
    probe = tmp_path / 'PosterProbe.java'
    probe.write_text('''package com.boop.shieldhome;
public class PosterProbe {
 public static void main(String[] args) {
  for(int width=180;width<=2200;width++) for(int height:new int[]{100,150,210,320}) {
   SerenPosterLayout p=SerenPosterLayout.fit(width,height*2/3,12,5,24);
   if(p.viewportWidth>width || p.visibleCount<1) throw new AssertionError("overflow");
   if(p.viewportWidth != p.visibleCount*p.posterWidth+(p.visibleCount-1)*12+10)
    throw new AssertionError("partial poster window");
   for(int index=0;index<24;index++) {
    int offset=p.offsetForFocus(index,0,24);
    int left=index*(p.posterWidth+12)-offset+5;
    if(left<5 || left+p.posterWidth>p.viewportWidth-5) throw new AssertionError("clipped focus");
    if(p.snapOffset(offset+3,24)!=offset) throw new AssertionError("not snapped");
   }
  }
 }
}''', encoding='utf-8')
    subprocess.run(['javac','-d',str(tmp_path),str(source),str(probe)],check=True)
    subprocess.run(['java','-cp',str(tmp_path),'com.boop.shieldhome.PosterProbe'],check=True)
