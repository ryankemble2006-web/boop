from pathlib import Path
import subprocess
import tempfile
import unittest
ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
class MusicBounceTest(unittest.TestCase):
    def test_real_level_and_smoothing_behaviour(self):
        source = HOME / 'MusicBounceEnvelope.java'
        self.assertTrue(source.is_file(), 'Real music bounce envelope is missing')
        with tempfile.TemporaryDirectory() as out:
            subprocess.run(['javac', '-d', out, str(source), str(ROOT/'tests/java/MusicBounceHarness.java')], check=True)
            subprocess.run(['java', '-cp', out, 'com.boop.shieldhome.MusicBounceHarness'], check=True)

    def test_actual_renderer_doubles_lift_without_scaling_the_puppet(self):
        # Compile the production renderer. Only the Android/GL boundary is a controlled test double.
        stubs = {
            'javax/microedition/khronos/opengles/GL10.java': 'package javax.microedition.khronos.opengles; public interface GL10 {}',
            'javax/microedition/khronos/egl/EGLConfig.java': 'package javax.microedition.khronos.egl; public interface EGLConfig {}',
            'android/opengl/GLSurfaceView.java': '''package android.opengl;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public class GLSurfaceView { public interface Renderer {
void onSurfaceCreated(GL10 gl,EGLConfig config); void onSurfaceChanged(GL10 gl,int w,int h); void onDrawFrame(GL10 gl);
}}''',
            'android/opengl/GLES20.java': '''package android.opengl;
public class GLES20 { public static int x,y,w,h; public static void glViewport(int a,int b,int c,int d){x=a;y=b;w=c;h=d;} }''',
            'com/boop/shieldhome/MusicBounceRendererHarness.java': '''package com.boop.shieldhome;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
public final class MusicBounceRendererHarness {
 static int checks;
 static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 static final class Delegate implements GLSurfaceView.Renderer {
  int created,changed,drawn;
  public void onSurfaceCreated(GL10 gl,EGLConfig config){created++;}
  public void onSurfaceChanged(GL10 gl,int w,int h){changed++;}
  public void onDrawFrame(GL10 gl){drawn++;}
 }
 public static void main(String[] args){
  Delegate delegate=new Delegate();
  MusicBounceRenderer renderer=new MusicBounceRenderer(delegate);
  renderer.onSurfaceCreated(null,null);
  renderer.onSurfaceChanged(null,180,100);
  float[] levels={0f,0.01f,0.07f,0.14f,1f,-1f,Float.NaN,Float.POSITIVE_INFINITY};
  int[] expected={0,2,14,28,28,0,0,0};
  for(int i=0;i<levels.length;i++){
   renderer.setHeightFraction(levels[i]);renderer.onDrawFrame(null);
   check(GLES20.y==expected[i],"Expected doubled visible lift "+expected[i]+", got "+GLES20.y);
   check(GLES20.x==0&&GLES20.w==180&&GLES20.h==100,"Puppet size and horizontal placement unchanged");
   check(delegate.drawn==i+1,"Every frame still delegates to the existing canonical renderer");
  }
  renderer.onSurfaceChanged(null,360,200);
  renderer.setHeightFraction(0.14f);renderer.onDrawFrame(null);
  check(GLES20.y==56&&GLES20.w==360&&GLES20.h==200,"Resized surface retains 2x lift and original dimensions");
  check(delegate.created==1&&delegate.changed==2,"Surface lifecycle still delegates");
  renderer.setHeightFraction(0f);renderer.onDrawFrame(null);
  check(GLES20.y==0,"Reset restores the exact original viewport");
  System.out.println("PASS: "+checks+" production-renderer amplitude/delegation checks; no physical visual test");
 }
}'''
        }
        with tempfile.TemporaryDirectory() as out:
            paths = []
            for name, text in stubs.items():
                path = Path(out)/name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(text)
                paths.append(str(path))
            subprocess.run(['javac','-d',out,*paths,str(HOME/'MusicBounceRenderer.java'),str(HOME/'MusicBounceEnvelope.java')],check=True)
            result = subprocess.run(['java','-cp',out,'com.boop.shieldhome.MusicBounceRendererHarness'],capture_output=True,text=True)
            self.assertEqual(result.returncode,0,result.stdout+result.stderr)
            print(result.stdout.strip())


if __name__ == '__main__': unittest.main(verbosity=2)
