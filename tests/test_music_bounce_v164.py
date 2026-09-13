"""Non-visual music-bounce gates. Never installs or launches Android."""
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
BASE = '112d09b5b446d6582954a6d89b3700fe16298ecb'
BASE164 = '95e2d153ac6e6b319f50284d4fbdb53bcb83c294'

class MusicBounceTest(unittest.TestCase):
    def test_real_level_and_smoothing_behaviour(self):
        source = HOME / 'MusicBounceEnvelope.java'
        self.assertTrue(source.is_file(), 'Real music bounce envelope is missing')
        with tempfile.TemporaryDirectory() as out:
            subprocess.run(['javac', '-d', out, str(source), str(ROOT/'tests/java/MusicBounceHarness.java')], check=True)
            subprocess.run(['java', '-cp', out, 'com.boop.shieldhome.MusicBounceHarness'], check=True)

    def test_actual_audio_source_and_visible_renderer_are_connected(self):
        source = HOME / 'MusicBounceSource.java'
        self.assertTrue(source.is_file(), 'No Android Visualizer audio source exists')
        text = source.read_text()
        for required in ('new Visualizer(0)', 'getWaveForm(', 'setEnabled(true)', 'release()', 'HandlerThread', 'MusicBounceEnvelope.levelOf'):
            self.assertIn(required, text)
        for forbidden in ('AudioRecord', 'MediaRecorder', 'SpeechRecognizer', 'requestAudioFocus', 'setStreamVolume', 'MediaProjection'):
            self.assertNotIn(forbidden, text)
        view = (HOME/'ShieldNowPlayingPuppetView.java').read_text()
        for required in ('MusicBounceSource', 'musicSource.setActive(', 'musicEnvelope.update(', 'setBounceHeight(', 'new MusicBounceRenderer(eyeRenderer)', 'eyeSurface.setRenderer(musicRenderer)', 'musicSource.stop()'):
            self.assertIn(required, view)
        self.assertIn('speed -> animation.setSpeed(speed, SystemClock.uptimeMillis())', view)
        renderer = (HOME/'MusicBounceRenderer.java').read_text()
        self.assertIn('GLES20.glViewport', renderer)
        self.assertIn('delegate.onDrawFrame', renderer)

    def test_accepted_v162_inputs_are_not_regressed(self):
        allowed = {'unified/app-build.gradle', 'unified/shield-home-manifest.xml',
                   'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java',
                   'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java'}
        # Every previously tracked app/build input is immutable except these four narrow adapters.
        prefixes = ['source', 'source-test', 'unified', 'scripts', 'launcher', 'shield-overlay', 'shield-clean-launcher', 'BOOP-Alpha1-project.zip', 'gradle.properties']
        changed = subprocess.check_output(['git','diff','--name-only','--diff-filter=MDR',BASE,'HEAD','--',*prefixes], cwd=ROOT, text=True).splitlines()
        self.assertFalse(set(changed)-allowed, 'Unexpected change to accepted v162 input: '+str(set(changed)-allowed))
        path = 'unified/app-build.gradle'
        old = subprocess.check_output(['git','show',BASE+':'+path], cwd=ROOT, text=True)
        expected = old.replace('versionCode 162','versionCode 165').replace('1.2.162-native-lyrics','1.2.165-music-bounce-stronger')
        self.assertEqual(expected, (ROOT/path).read_text(), 'Only the two version fields may change in build configuration')

    def test_permission_entry_does_not_use_voice_callback(self):
        activity = HOME/'MusicAudioPermissionActivity.java'
        self.assertTrue(activity.is_file(), 'Conditional audio permission entry is missing')
        self.assertIn('requestPermissions(', activity.read_text())
        self.assertNotIn('SpeechRecognizer', activity.read_text())
        self.assertIn('MusicAudioPermissionActivity.class', (HOME/'ShieldHomeSettingsView.java').read_text())
        self.assertIn('com.boop.shieldhome.MusicAudioPermissionActivity', (ROOT/'unified/shield-home-manifest.xml').read_text())

    def test_v165_changes_only_the_visible_gain_and_version(self):
        renderer_path = 'unified/shield-home/src/main/java/com/boop/shieldhome/MusicBounceRenderer.java'
        allowed = {renderer_path, 'unified/app-build.gradle'}
        prefixes = ['source', 'source-test', 'unified', 'scripts', 'launcher', 'shield-overlay', 'shield-clean-launcher', 'BOOP-Alpha1-project.zip', 'gradle.properties']
        changed = subprocess.check_output(['git','diff','--name-only',BASE164,'HEAD','--',*prefixes], cwd=ROOT, text=True).splitlines()
        self.assertFalse(set(changed)-allowed, 'Height tuning modified unrelated v164 inputs: '+str(set(changed)-allowed))
        original = subprocess.check_output(['git','show',BASE164+':'+renderer_path], cwd=ROOT, text=True)
        needle = 'Math.min(MusicBounceEnvelope.MAX_HEIGHT_FRACTION, value)) : 0f'
        self.assertEqual(original.count(needle), 1)
        self.assertEqual((ROOT/renderer_path).read_text(), original.replace(needle, 'Math.min(MusicBounceEnvelope.MAX_HEIGHT_FRACTION, value)) * 2f : 0f'), 'Renderer must differ only by doubled visible lift')

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

if __name__ == '__main__':
    unittest.main(verbosity=2)
